package com.mia.ciku.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mia.ciku.model.CityCiku;

public interface CityCikuMapper {

	public int saveCityCikuInfoList(@Param("cityCikuInfoList")List<CityCiku> cityCikuInfoList);
	
	public List<CityCiku> listCityCiku();
}
