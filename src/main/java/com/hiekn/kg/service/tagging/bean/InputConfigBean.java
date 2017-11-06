package com.hiekn.kg.service.tagging.bean;

import java.util.List;

public class InputConfigBean {
	
	/**
	 * 用戶
	 */
	private String user;
	/**
	 * 密碼
	 */
	private String password;
	/**
	 * 源数据库类型 mongo 
	 * TODO ES
	 */
	private String sourceType;
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * 源数据库地址
	 */
	private String sourceHost;
	/**
	 * 源数据库端口
	 */
	private int sourcePort;
	/**
	 * 源数据库DB
	 */
	private String sourceDB;
	/**
	 * 源数据库表
	 */
	private String sourceCollection;
	
	/**
	 * 参与下一步操作的数据字段
	 */
	private List<String> sourceFields;
	
	private String startTime;
	
	private String endTime;
	
	/**
	 * 限制
	 */
	private Integer	limit;
	
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceHost() {
		return sourceHost;
	}

	public void setSourceHost(String sourceHost) {
		this.sourceHost = sourceHost;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	public String getSourceDB() {
		return sourceDB;
	}

	public void setSourceDB(String sourceDB) {
		this.sourceDB = sourceDB;
	}

	public String getSourceCollection() {
		return sourceCollection;
	}

	public void setSourceCollection(String sourceCollection) {
		this.sourceCollection = sourceCollection;
	}

	public List<String> getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(List<String> sourceFields) {
		this.sourceFields = sourceFields;
	}

}
