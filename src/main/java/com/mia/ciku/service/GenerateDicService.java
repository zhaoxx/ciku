package com.mia.ciku.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mia.ciku.download.DownloadHelper;
import com.mia.ciku.mapper.CityCikuMapper;
import com.mia.ciku.model.CityCiku;

@Service
public class GenerateDicService {

	@Autowired
	private CityCikuMapper cityCikuMapper;
	
	public void GenerateDicByQQ() {
		List<CityCiku> cikuList = cityCikuMapper.listCityCiku();
		for(CityCiku item:cikuList){
			if(StringUtils.isNotBlank(item.getCityAddressUrl())){
				DownloadHelper.save("D:/dic/", item.getCity(), item.getCityAddressUrl());
			}
			if(StringUtils.isNotBlank(item.getCityInfoUrl())){
				DownloadHelper.save("D:/dic/", item.getCity(), item.getCityInfoUrl());
			}
		}
	}
}
