package com.mia.ciku.vo;

public class CityInfo {
	private String cityAdreess;
	private String cityInfo;
	public String getCityAdreess() {
		return cityAdreess;
	}
	public void setCityAdreess(String cityAdreess) {
		this.cityAdreess = cityAdreess;
	}
	public String getCityInfo() {
		return cityInfo;
	}
	public void setCityInfo(String cityInfo) {
		this.cityInfo = cityInfo;
	}
	@Override
	public String toString() {
		return "CityInfo [cityAdreess=" + cityAdreess + ", cityInfo="
				+ cityInfo + "]";
	}
}