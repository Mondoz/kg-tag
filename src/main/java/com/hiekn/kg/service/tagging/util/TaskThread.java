package com.hiekn.kg.service.tagging.util;

import com.hiekn.kg.service.tagging.bean.DBConfig;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TaskThread extends Thread{
	
	private static final Logger log = Logger.getLogger(TaskThread.class);
	
	private String taskId;
	private String subTaskId;
	private DBConfig configBean;
	private String taggingKGName;
	private long startTime;
	private long endTime;
	private List<String> taggingFieldList;
	private List<Long> conceptList;
	private List<Long> relationList;
	private int level;
	
	public TaskThread(String taskId, String subTaskId, DBConfig configBean, String taggingKGName, long startTime,
			long endTime, List<String> taggingFieldList, List<Long> conceptList, List<Long> relationList, int level) {
		this.taskId = taskId;
		this.subTaskId = subTaskId;
		this.configBean = configBean;
		this.taggingKGName = taggingKGName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.taggingFieldList = taggingFieldList;
		this.conceptList = conceptList;
		this.relationList = relationList;
		this.level = level;
	}
	
	public void run() {
		try {
			TimeUnit.SECONDS.sleep(2);
//			new TaggerUtil().doSimpleTagByIndexUsingDB(configBean,taggingKGName, startTime,
//			endTime,taggingFieldList,conceptList, relationList,level);
			sendInfo();
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private void sendInfo() throws IOException {
		RequestBody formBody = new FormBody.Builder()
		.add("taskId", taskId)
		.add("taskStatus", "3")
		.add("subTaskId", subTaskId).build();
		Request request = new Request.Builder()
		.url("http://192.168.1.101:8080/kg_exhibit_ws/project/update/status?userId=1")
		.post(formBody)
		.build();
		Response response = OkHttpUtil.execute(request);
		String source = response.body().string();
		System.out.println(source);
		
	}
	
}
