package com.hiekn.kg.service.tagging.bean;

public class ThirdPartyMsgBean {
	
	private String channel;
	private String taskId;
	private String subTaskId;
	private Integer status;
	private String config;
	private Integer action;
	public ThirdPartyMsgBean() {
		super();
	}
	public ThirdPartyMsgBean(String taskId, String subTaskId,
			Integer status) {
		super();
		this.taskId = taskId;
		this.subTaskId = subTaskId;
		this.status = status;
	}
	public ThirdPartyMsgBean(String taskId, String subTaskId, Integer action, String config) {
		super();
		this.taskId = taskId;
		this.subTaskId = subTaskId;
		this.config = config;
		this.action = action;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getSubTaskId() {
		return subTaskId;
	}
	public void setSubTaskId(String subTaskId) {
		this.subTaskId = subTaskId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	public Integer getAction() {
		return action;
	}
	public void setAction(Integer action) {
		this.action = action;
	}


}
