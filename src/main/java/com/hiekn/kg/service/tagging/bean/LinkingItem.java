package com.hiekn.kg.service.tagging.bean;


/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2015-1-11
 * 
 */
public class LinkingItem implements java.io.Serializable {
	private static final long serialVersionUID = -1804094251536808140L;
	
	private String id;
	private long entityId;
	private String corpusId;
	private float score;
	private int sourceType;
	private String info;
	
	public long getEntityId() {
		return entityId;
	}
	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(String corpusId) {
		this.corpusId = corpusId;
	}


}
