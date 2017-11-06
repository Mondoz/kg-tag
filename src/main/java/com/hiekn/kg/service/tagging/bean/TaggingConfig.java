package com.hiekn.kg.service.tagging.bean;


public class TaggingConfig {
	private InputConfigBean inputConfigBean;
	private OutputConfigBean outputConfigBean;
	private KGConfigBean kgConfigBean;
	private TaggingAlgoBean taggingConfigBean;
	public InputConfigBean getInputConfigBean() {
		return inputConfigBean;
	}
	public void setInputConfigBean(InputConfigBean inputConfigBean) {
		this.inputConfigBean = inputConfigBean;
	}
	public OutputConfigBean getOutputConfigBean() {
		return outputConfigBean;
	}
	public void setOutputConfigBean(OutputConfigBean outputConfigBean) {
		this.outputConfigBean = outputConfigBean;
	}
	public KGConfigBean getKgConfigBean() {
		return kgConfigBean;
	}
	public void setKgConfigBean(KGConfigBean kgConfigBean) {
		this.kgConfigBean = kgConfigBean;
	}
	public TaggingAlgoBean getTaggingConfigBean() {
		return taggingConfigBean;
	}
	public void setTaggingConfigBean(TaggingAlgoBean taggingConfigBean) {
		this.taggingConfigBean = taggingConfigBean;
	}
	
}
