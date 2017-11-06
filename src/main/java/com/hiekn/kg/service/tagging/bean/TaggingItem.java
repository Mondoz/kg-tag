package com.hiekn.kg.service.tagging.bean;

import java.util.Set;

/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2015-1-11
 * 
 */
public class TaggingItem implements java.io.Serializable {
	private static final long serialVersionUID = -1804094251536808140L;
	

	private long id;
	private String name;
	private double score;
	private long classId;
	private Set<String> refs;
	
	public TaggingItem(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public TaggingItem(long id, String name,long classId) {
		this.id = id;
		this.name = name;
		this.classId = classId;
	}
	public long getClassId() {
		return classId;
	}
	public void setClassId(long classId) {
		this.classId = classId;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<String> getRefs() {
		return refs;
	}
	public void setRefs(Set<String> refs) {
		this.refs = refs;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
}
