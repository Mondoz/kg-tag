package com.hiekn.kg.service.tagging.bean;

import java.util.List;
import java.util.Map;

public class TaggingAlgoBean {
	
	/**
	 * 模型类型
	 * 0 匹配标注
	 * 1 扩展标注
	 */
	private int modelType;
	
	/**
	 * 对象关系过滤
	 */
	private List<Long> relationFilter;
	/**
	 * 扩展的层数
	 */
	private int level;

	/**
	 * 标注的阈值
	 */
	private double threshold;
	
	/**
	 * 字段的权重
	 */
	private Map<String,Double> weightMap;
	
	public int getModelType() {
		return modelType;
	}
	public void setModelType(int modelType) {
		this.modelType = modelType;
	}
	public Map<String, Double> getWeightMap() {
		return weightMap;
	}
	public void setWeightMap(Map<String, Double> weightMap) {
		this.weightMap = weightMap;
	}
	
	public double getThreshold() {
		return threshold;
	}
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	public List<Long> getRelationFilter() {
		return relationFilter;
	}
	public void setRelationFilter(List<Long> relationFilter) {
		this.relationFilter = relationFilter;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
}
