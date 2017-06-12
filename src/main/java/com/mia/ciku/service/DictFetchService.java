package com.mia.ciku.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mia.ciku.download.HttpClientUtil;
import com.mia.ciku.mapper.CityCikuMapper;
import com.mia.ciku.model.CityCiku;

@Service
public class DictFetchService {
	
	//词长度大于1小于5
	public static Map<String,String> map = new HashMap<String,String>();
	//词长度等于1大于等于5
	public static Map<String,String> noSuitMap = new HashMap<String,String>();
	
	private final static String host = "http://dict.qq.pinyin.cn";
	
	@Autowired
	private CityCikuMapper cityCikuMapper;
	
	private final static List<String> provinces = Arrays.asList("北京","上海","天津","重庆","河北","山西","河南","辽宁","吉林","黑龙江","内蒙古","江苏","山东","安徽","浙江","福建","湖北","湖南","广东","广西","江西","四川","海南","贵州","云南","西藏","陕西","甘肃","青海","宁夏","新疆","台湾","香港","澳门","钓鱼岛");
	
	public static void main(String[] args) throws Exception {
		String title = "城市信息(保定)";
		String city = title.replaceAll("城市信息|\\(|\\)", "");
		System.out.println(city);
		//入口地址
		String currPage = host + "/dict_list?sort1=%B3%C7%CA%D0%B5%D8%C7%F8";
//		getProvinceInfo(currPage);
//		getCityInfo("澳门",host+"/dict_list?sort1=%E5%9F%8E%E5%B8%82%E5%9C%B0%E5%8C%BA&sort2=%E6%BE%B3%E9%97%A8");
//		getPageSize(host+"/dict_list?sort1=%B3%C7%CA%D0%B5%D8%C7%F8&sort2=%B9%E3%B6%AB");
	}
	
	public void getProvinceInfo(String url) throws Exception{
		String contents = getCurrPageContent(url);
		Map<String,String> provinceMap = getProvinceLink(contents);
		Set<Map.Entry<String, String>> set = provinceMap.entrySet();
		for (Iterator<Map.Entry<String, String>> it = set.iterator(); it.hasNext();) {
		Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
//			System.out.println(entry.getKey()+":"+entry.getValue());
			String provinceUrl = entry.getValue();
			int pageSize = getPageSize(host+provinceUrl);
			
			Map<String, CityCiku> cityMap = new HashMap<String, CityCiku>();
			
			for(int i=0;i<pageSize;i++){
				if(i>0){
					cityMap = getCityInfo(entry.getKey(),host + provinceUrl+"&page="+(i+1),cityMap);
				}else{
					cityMap = getCityInfo(entry.getKey(),host + provinceUrl+"",cityMap);
				}
			}
			List<CityCiku> cityCikuInfoList = new ArrayList<CityCiku>();
			Set<Map.Entry<String, CityCiku>> newSet = cityMap.entrySet();
			for (Iterator<Map.Entry<String, CityCiku>> newIt = newSet.iterator(); newIt.hasNext();) {
				Map.Entry<String, CityCiku> newEntry = (Map.Entry<String, CityCiku>) newIt.next();
				cityCikuInfoList.add(newEntry.getValue());
			}
			
			if(CollectionUtils.isNotEmpty(cityCikuInfoList)){
				cityCikuMapper.saveCityCikuInfoList(cityCikuInfoList);
			}
		}
	}
	
	private int getPageSize(String url) throws Exception{
		int result=1;
		String contents = getCurrPageContent(url);
		if (StringUtils.isEmpty(contents)) {
			return result;
		}
		//获取页面<table class="catewords2">元素下的内容
		Parser htmlParser = Parser.createParser(contents, "utf-8");
		AndFilter filter =  new AndFilter( 
	         new TagNameFilter("span"), 
	         new HasAttributeFilter("class","page-skip") 
	    ); 
		NodeList nodes = htmlParser.parse(filter);
		String tempContent = nodes.toHtml();
		String regex = "共\\d+页";
		Pattern pt = Pattern.compile(regex);
		Matcher mt = pt.matcher(tempContent.toString());
		while (mt.find()) {
			result = Integer.parseInt(mt.group().replaceAll("共|页", ""));
		}
		return result;
	}
	
	private Map<String,String> getProvinceLink(String contents){
		Map<String, String> result = new LinkedHashMap<String, String>();
		if (StringUtils.isEmpty(contents)) {
			return result;
		}
		Parser htmlParser = Parser.createParser(contents, "utf-8");
		NodeList dictList = null;
		try {
			dictList = htmlParser.extractAllNodesThatMatch(new NodeFilter() {
				private static final long serialVersionUID = -505172714433102811L;
				public boolean accept(Node node) {
					if (node instanceof LinkTag) {
						LinkTag linkTag = (LinkTag) node;
						String attr = linkTag.getAttribute("href");
						if (StringUtils.isNotBlank(attr) && attr.contains("城市地区")) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			});
		} catch (ParserException e) {
			e.printStackTrace();
			return null;
		}

		if (dictList != null && dictList.size() > 0) {
			for(int i=0;i<dictList.size();i++){
				LinkTag linkTag = (LinkTag) dictList.elementAt(i);
				String content = linkTag.getLink();
//				String attr = linkTag.getAttribute("herf");
				//根据正则表达式获取类似于  <a href="/dict_list?sort1=城市地区&sort2=贵州">自由幻想(5)</a> 的内容
				String regex = "/dict_list\\?sort1=城市地区&sort2=\\S+";
//				/dict_list?sort1=城市地区&sort2=贵州
				Pattern pt = Pattern.compile(regex);
				Matcher mt = pt.matcher(content.toString());
				while (mt.find()) {
					String province = content.substring("/dict_list?sort1=城市地区&sort2=".length()).trim();
					if(provinces.contains(province)){
						
						if(province.equals("北京")
								|| province.equals("上海")
								|| province.equals("天津")
								|| province.equals("重庆")){
							province += "市";
						}else if (province.equals("内蒙古") || province.equals("西藏")){
							province += "自治区";
						} else if (province.equals("广西")){
							province += "壮族自治区";
						} else if(province.equals("宁夏")){
							province += "回族自治区";
						}else if(province.equals("新疆")){
							province += "维吾尔自治区";
						}else if (province.equals("台湾") || province.equals("香港") || province.equals("澳门")){
							
						}else{
							province += "省";
						}
						
						String url = content;
						result.put(province, url);
					}
				}
			}
		}
		return result;
	}

	private Map<String,CityCiku> getCityInfo(String provinceName,String provinceUrl,Map<String,CityCiku> cityMap) throws Exception{
		String contents = getCurrPageContent(provinceUrl);
		if (StringUtils.isEmpty(contents)) {
			return null;
		}
		//获取页面<table class="catewords2">元素下的内容
		Parser htmlParser = Parser.createParser(contents, "utf-8");
		AndFilter filter =  new AndFilter( 
	         new TagNameFilter("div"), 
	         new HasAttributeFilter("id","cikuCategoryPage") 
	    ); 
	    NodeList nodes = htmlParser.parse(filter);
	    NodeList dictList = null;
	    try {
	    	htmlParser = Parser.createParser(nodes.toHtml(), "utf-8");
			dictList = htmlParser.extractAllNodesThatMatch(new NodeFilter() {
				private static final long serialVersionUID = -505172714433102811L;
				public boolean accept(Node node) {
					if (node instanceof LinkTag) {
						LinkTag linkTag = (LinkTag) node;
						String attr = linkTag.getAttribute("href");
						String title = linkTag.getLinkText();
						if (StringUtils.isNotBlank(attr)
								&& attr.contains("dict_detail")
								&& StringUtils.isNotBlank(title)
								&& (title.endsWith("地名") || title.startsWith("城市信息"))) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	    
	    if (dictList != null && dictList.size() > 0) {
			for(int i=0;i<dictList.size();i++){
				LinkTag linkTag = (LinkTag) dictList.elementAt(i);
				String url = linkTag.getLink();
				String title = linkTag.getLinkText();
//				String attr = linkTag.getAttribute("herf");
				//根据正则表达式获取类似于   <a href="/dict_detail?dict_id=687">澳门地名</a> 的内容
				//根据正则表达式获取类似于   <a href="/dict_detail?dict_id=1459">城市信息(澳门)</a> 的内容
				String city = "";
				boolean isAddress = false;
				if(title.endsWith("地名")){
					city = title.replaceAll("地名", "");
					isAddress = true;
				}else if(title.startsWith("城市信息")){
					city = title.replaceAll("城市信息|\\(|\\)", "");
					isAddress = false;
				}
				if(StringUtils.isBlank(city)){
					continue;
				}				
				String downLoadUrl = host+getDownLoadUrl(host+url);
				CityCiku cityInof = cityMap.get(city);
				if(cityInof == null){
					cityInof = new CityCiku();
					cityInof.setProvince(provinceName);
					cityInof.setCity(city);
				}
				
				if(isAddress){
					cityInof.setCityAddressUrl(downLoadUrl);
				}else{
					cityInof.setCityInfoUrl(downLoadUrl);
				}
				cityMap.put(city, cityInof);
			}
		}
		return cityMap;
	}
	
	private String getDownLoadUrl(String url){
		String contents = getCurrPageContent(url);
		if (StringUtils.isEmpty(contents)) {
			return null;
		}
		String downLoadUrl = null;
		Parser htmlParser = Parser.createParser(contents, "utf-8");
		NodeList downLoadList = null;
		try {
			downLoadList = htmlParser.extractAllNodesThatMatch(new NodeFilter() {
				private static final long serialVersionUID = -505172714433102811L;

				public boolean accept(Node node) {
					if (node instanceof LinkTag) {
						LinkTag linkTag = (LinkTag) node;
						String attr = linkTag.getAttribute("class");
						if ("downloadICO".equals(attr)) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				}
			});
		} catch (ParserException e) {
			e.printStackTrace();
			return null;
		}
		
		if (downLoadList != null && downLoadList.size() > 0) {
			//遍历所有的下载链接
			for(int i=0;i<downLoadList.size();i++){
				LinkTag link = (LinkTag) downLoadList.elementAt(i);
				downLoadUrl = link.getLink().trim();
				if(StringUtils.isNotBlank(downLoadUrl)){
					downLoadUrl = downLoadUrl.replaceAll(" ", "");
					break;
				}
			}
		}
		return downLoadUrl;
	}
	
	/**
	 * 获取当前页的html内容
	 * @param page
	 * @return
	 */
	public String getCurrPageContent(String page) {
		return HttpClientUtil.createPost(page, null);
	}
}