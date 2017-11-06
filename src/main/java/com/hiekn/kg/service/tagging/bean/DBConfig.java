package com.hiekn.kg.service.tagging.bean;

public class DBConfig {
	/**
	 * 源数据库类型 mongo 
	 * TODO ES
	 */
	private String sourceType;
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
	 * 目标数据库类型
	 */
	private String targetType;
	/**
	 * 目标数据库地址
	 */
	private String targetHost;
	/**
	 * 目标数据库端口
	 */
	private int targetPort;
	/**
	 * 目标数据库DB
	 */
	private String targetDB;
	/**
	 * 目标数据库表
	 */
	private String targetCollection;
	/**
	 * 标注操作 0:update 1:insert
	 */
	private int option;
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
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getTargetHost() {
		return targetHost;
	}
	public void setTargetHost(String targetHost) {
		this.targetHost = targetHost;
	}
	public int getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	public String getTargetDB() {
		return targetDB;
	}
	public void setTargetDB(String targetDB) {
		this.targetDB = targetDB;
	}
	public String getTargetCollection() {
		return targetCollection;
	}
	public void setTargetCollection(String targetCollection) {
		this.targetCollection = targetCollection;
	}
	public int getOption() {
		return option;
	}
	public void setOption(int option) {
		this.option = option;
	}
}
