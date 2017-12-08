package com.hiekn.kg.service.tagging.main;

import com.alibaba.fastjson.JSONObject;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.hiekn.kg.service.tagging.util.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.bson.Document;
import org.elasticsearch.common.collect.Lists;
import org.jsoup.Jsoup;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SparkRunner {
	public static void main(String[] args) {
		sparkConnect(args);
	}

	static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();

	public static void sparkConnect(String[] args) {
		java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(java.util.logging.Level.SEVERE);
		SparkConf conf = new SparkConf()
				.setAppName("text_file");
		JavaSparkContext sc = new JavaSparkContext(conf);
		String path = args[0];
		String outputPath = args[1];
		String taggingDBName = ConstResource.KG;
		MongoCollection<Document> col = kgClient.getDatabase(taggingDBName).getCollection("parent_son");
		String[] taggingField = ConstResource.FIELDS.split(",");
		List<Long> entitySonList = new ArrayList<Long>();
		ConstResource.INSTANCELIST.forEach(instance -> entitySonList.addAll(TaggerUtil.findAllSon(col, instance)));
		List<Long> conceptSonList = new ArrayList<Long>();
		ConstResource.CONCEPTLIST.forEach(concept -> conceptSonList.addAll(TaggerUtil.findAllSon(col, concept)));
		SemanticSegUtil.segInit(entitySonList,conceptSonList);
		List<Value> wordList = Lists.newArrayList();
		String[] arr = {"1","2","3"};
		SemanticSegUtil.kgWordIdMap.get(taggingDBName).keySet().forEach(s -> wordList.add(new Value(s,arr)));
		Broadcast<Forest> broadForest = sc.broadcast(Library.makeForest(wordList));
		Broadcast<Map<String,Map<Long,Map<Long,TaggingItem>>>> kgNameParentIdMapBroadCast = sc.broadcast(SemanticSegUtil.kgNameParentIdMap);
		Broadcast<Map<String,Map<String,List<TaggingItem>>>> kgWordIdMapBroadCast = sc.broadcast(SemanticSegUtil.kgWordIdMap);

		JavaRDD<JSONObject> resultRDD = sc.textFile(path).repartition(10)
                .map(doc -> {
					JSONObject docObj = JSONObject.parseObject(doc);
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
					JSONObject jsonObject;
					try{
//						Map<String, List<TaggingItem>> tagResultMap = SemanticSegUtil.ansjSeg(taggingDBName, text, broadForest.value());
						Map<String, List<TaggingItem>> tagResultMap = SemanticSegUtil.ansjSeg(taggingDBName, text,broadForest.value(),kgWordIdMapBroadCast.value(),kgNameParentIdMapBroadCast.value());
						taggingList = tagResultMap.get("tagging");
						parentTaggingList = tagResultMap.get("taggingParent");
						jsonObject = docObj;
						if (taggingList.size() > 0) {
							if (docObj.containsKey("annotation_tag")) {
								taggingList.addAll(docObj.getObject("annotation_tag",List.class));
								jsonObject.put("annotation_tag", taggingList);
							} else {
								jsonObject.put("annotation_tag", taggingList);
							}
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
                    return jsonObject;
		});
		resultRDD.saveAsTextFile(outputPath);
	}
}




