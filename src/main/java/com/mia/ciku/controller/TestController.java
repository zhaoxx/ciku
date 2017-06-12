/*
 * 类名 OrderController.java
 *
 * 版本信息 
 *
 * 日期 2017年1月10日
 *
 * 版权声明Copyright (C) 2017 mia Information Technology Co.,Ltd
 * All Rights Reserved.
 */
package com.mia.ciku.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mia.ciku.service.DictFetchService;
import com.mia.ciku.service.GenerateDicService;

/**
 * Class description goes here.
 *
 * @author  zhaoxianxing
 */
@Controller
public class TestController {
	
	@Autowired
	private DictFetchService dictFetchService;
	
	@Autowired
	private GenerateDicService generateDicService;
	
    @RequestMapping("/test")
    @ResponseBody
    public String test(){
    	return "success";
    }
    
    @RequestMapping("/getProvinceInfo")
    @ResponseBody
    public String getProvinceInfo() throws Exception{
    	String currPage = "http://dict.qq.pinyin.cn/dict_list?sort1=%B3%C7%CA%D0%B5%D8%C7%F8";
    	dictFetchService.getProvinceInfo(currPage);
    	return "success";
    }
    
    @RequestMapping("/generateDicByQQ")
    @ResponseBody
    public String generateDicByQQ() throws Exception{
    	generateDicService.GenerateDicByQQ();
    	return "success";
    }
    
}