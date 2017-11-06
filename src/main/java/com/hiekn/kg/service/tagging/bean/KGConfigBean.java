package com.hiekn.kg.service.tagging.bean;

import java.util.List;

public class KGConfigBean {
	/**
	 * 选择概念
	 */
	private List<Long> conceptFilter;
	
	/**
	 * 选择实例
	 */
	private List<Long> entityFilter;
	
	/**
	 * kgDBName
	 */
	private String kgDBName;
	
	public List<Long> getConceptFilter() {
		return conceptFilter;
	}
	public void setConceptFilter(List<Long> conceptFilter) {
		this.conceptFilter = conceptFilter;
	}
	public List<Long> getEntityFilter() {
		return entityFilter;
	}
	public void setEntityFilter(List<Long> entityFilter) {
		this.entityFilter = entityFilter;
	}
	public String getKgDBName() {
		return kgDBName;
	}
	public void setKgDBName(String kgDBName) {
		this.kgDBName = kgDBName;
	}
}
