package com.mia.ciku.download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DownloadHelper{
	private static Log log = LogFactory.getLog(DownloadHelper.class);
	
	public static void main(String[] args){
		DownloadHelper.save("D:/dic/", "贵阳市", "http://dict.qq.pinyin.cn/download?dict_id=1290");
	}
	
	public static boolean save(String path,String name, String sourceFilePath){
		if(StringUtils.isEmpty(name)){
			log.error("file name is empty");
			return false;
		}
		
		if(StringUtils.isEmpty(path)){
			log.error("file path is empty!!");
			return false;
		}
		
		HttpURLConnection connection = null;
		BufferedWriter bw = null;
		BufferedWriter noSuitBw = null;
		try{
			URL url = new URL(sourceFilePath);
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; CIBA; Alexa Toolbar)");
			connection.setRequestProperty("Host", "dict.qq.pinyin.cn");
			connection.setRequestProperty("Referer", "http://dict.qq.pinyin.cn");
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			
			QQScelMdel model = new QQPinyinQpydReader().read(connection.getInputStream());
			List<String> words = model.getWordList(); //词<拼音,词>
			
			File file = new File(path+name+".dic");
			StringBuffer contents = new StringBuffer();
			for(String item:words){
				item = item.replaceAll(" ", "");
				contents.append(item+"\n");
			}
					
			FileWriter fw = new FileWriter(file, true);
		    bw = new BufferedWriter(fw);
		    bw.write(contents.toString());
		    
		}catch(MalformedURLException me){
			me.printStackTrace();
			return false;
		}catch(IOException ioe){
			ioe.printStackTrace();
			return false;
		}finally{
			try{
				if(bw!=null){
					bw.close();
				}
				if(noSuitBw!=null){
					noSuitBw.close();
				}
				if(connection!=null){
					connection.disconnect();
				}
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}