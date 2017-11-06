package com.hiekn.kg.service.tagging.bean;

import java.util.Map;

public class OutputConfigBean {
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
	 * 集群名
	 */
	private String clusterName;
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
	/**
	 * 字段映射
	 */
	private Map<String,String> mappingField;
	
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
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
	public Map<String, String> getMappingField() {
		return mappingField;
	}
	public void setMappingField(Map<String, String> mappingField) {
		this.mappingField = mappingField;
	}
	
}
