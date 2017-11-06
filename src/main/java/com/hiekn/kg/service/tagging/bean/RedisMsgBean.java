package com.hiekn.kg.service.tagging.bean;

public class RedisMsgBean {
	
	private int msgType;
	private Object msgBody;
	
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	public Object getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(Object msgBody) {
		this.msgBody = msgBody;
	}
	
}
