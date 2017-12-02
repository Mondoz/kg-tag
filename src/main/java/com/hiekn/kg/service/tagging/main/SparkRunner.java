package com.hiekn.kg.service.tagging.main;

import com.alibaba.fastjson.JSONObject;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.hiekn.kg.service.tagging.util.ConstResource;
import com.hiekn.kg.service.tagging.util.ContentFilter;
import com.hiekn.kg.service.tagging.util.TaggerUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkRunner {
	public static void main(String[] args) {
		sparkConnect(args);
	}

	static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();

	public static void sparkConnect(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("text_file");
		JavaSparkContext sc = new JavaSparkContext(conf);

		String taggingDBName = ConstResource.KG;
		MongoCollection<Document> col = kgClient.getDatabase(taggingDBName).getCollection("parent_son");
		String[] taggingField = ConstResource.FIELDS.split(",");
		List<Long> entitySonList = new ArrayList<Long>();
		ConstResource.INSTANCELIST.forEach(instance -> entitySonList.addAll(TaggerUtil.findAllSon(col, instance)));
		List<Long> conceptSonList = new ArrayList<Long>();
		ConstResource.CONCEPTLIST.forEach(concept -> conceptSonList.addAll(TaggerUtil.findAllSon(col, concept)));

		JavaRDD<JSONObject> resultRDD = sc.textFile(args[0])
                .map(doc -> {
					JSONObject docObj = JSONObject.parseObject(doc);
					Map<String,String> mapFields = TaggerUtil.reverseMap(ConstResource.MAPFIELDS);
					for (String field : taggingField) {
						if (docObj.containsKey(field)) {
							if (docObj.get(field) != null) {
								doc = doc + Jsoup.parse(docObj.get(field).toString()).text() + "\t";
							}
						}
					}
					doc = doc.toLowerCase();
					String text = doc;
					List<TaggingItem> taggingList;
					List<TaggingItem> parentTaggingList;
					JSONObject jsonObject = new JSONObject();
					try{
						Map<String, List<TaggingItem>> tagResultMap = TaggerUtil.getTagProcess(taggingDBName, text, conceptSonList, entitySonList, 0);
						taggingList = tagResultMap.get("tagging");
						parentTaggingList = tagResultMap.get("taggingParent");
						for (String key : docObj.keySet()) {
							if (mapFields.containsKey(key)) {
								if (key.equals("content")) {
									List<String> contentList = new ArrayList<String>();
									contentList = ContentFilter.getSplitContent(docObj.get("content").toString());
									jsonObject.put(mapFields.get(key), contentList);
								} else {
									jsonObject.put(mapFields.get(key), docObj.get(key) != null ? docObj.get(key) : "");
								}
							}
						}
						if (taggingList.size() > 0) {
							jsonObject.put("annotation_tag", taggingList);
						} else {
							jsonObject.put("annotation_tag", new ArrayList<>());
						}
						if (parentTaggingList.size() > 0) {
							jsonObject.put("parent_annotation_tag", parentTaggingList);
						} else {
							jsonObject.put("parent_annotation_tag", new ArrayList<>());
						}
					} catch (Exception e) {
						throw e;
					}
					jsonObject.put("thread",Thread.currentThread().getId());
					InetAddress addr = InetAddress.getLocalHost();
					String ip = addr.getHostAddress().toString();
					jsonObject.put("ip",ip);
                    return jsonObject;
		});
        
		resultRDD.saveAsTextFile(args[1]);
	}


}




