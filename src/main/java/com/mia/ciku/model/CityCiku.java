package com.mia.ciku.model;

import java.util.Date;

public class CityCiku {
	private int id;
	private String province;//省
	private String city;//市
	private String cityInfoUrl;
	private String cityAddressUrl;
	private Date createTime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getCityInfoUrl() {
		return cityInfoUrl;
	}
	public void setCityInfoUrl(String cityInfoUrl) {
		this.cityInfoUrl = cityInfoUrl;
	}
	public String getCityAddressUrl() {
		return cityAddressUrl;
	}
	public void setCityAddressUrl(String cityAddressUrl) {
		this.cityAddressUrl = cityAddressUrl;
	}
}
