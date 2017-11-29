package com.hiekn.kg.service.tagging.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.hiekn.kg.service.tagging.mongo.MongoSingleton;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.jsoup.Jsoup;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2014-12-21
 * 
 */
public class TaggerUtil implements Runnable{
	private static final Logger log = Logger.getLogger(TaggerUtil.class);

	private static Map<String, Double> idfMap = new HashMap<String, Double>();
	private static final double WEIGHT_ARRAY[] = {0.15, 0.25, 0.1, 0.2, 0.2, 0.3};	//摘要、同义、父概念、子概念、实例、属性
	private static final double[] ATTENUATION_FACTORS = {Math.sqrt(2) / 2};
	private static final int TAGGING_NUM = 8;

	private int skip;
	private int limit;

	public TaggerUtil(int skip,int limit) {
		this.skip = skip;
		this.limit = limit;
	}
	public TaggerUtil() {
	}

	private static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();

	private static Map<Integer,Map<Long,String>> classConceptMap = getClassConceptMap();

	public static void main(String[] args) throws IOException {
		FileWriter ffw = new FileWriter("data/first_class.txt");
		FileWriter sfw = new FileWriter("data/second_class.txt");
		FileWriter tfw = new FileWriter("data/third_class.txt");
		Map<Integer, Map<Long, String>> classConceptMap = new HashMap<Integer, Map<Long,String>>();
		MongoCollection<Document> col = kgClient.getDatabase(ConstResource.KG).getCollection("parent_son");
		Map<Long,String> fMap = new HashMap<Long,String>();
		Map<Long,String> sMap = new HashMap<Long,String>();
		Map<Long,String> tMap = new HashMap<Long,String>();
		MongoCursor<Document> fcursor = col.find(new Document("parent",5L)).iterator();
		String fs = "";
		String ss = "";
		String ts = "";
		while (fcursor.hasNext()) {
			Document doc = fcursor.next();
			fMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
			fs = fs + doc.getLong("son") + ",";
		}
		ffw.write(fs);
		ffw.flush();
		classConceptMap.put(1, fMap);
		MongoCursor<Document> scursor = col.find(new Document("parent",new Document("$in",fMap.keySet()))).iterator();
		while (scursor.hasNext()) {
			Document doc = scursor.next();
			sMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
			ss = ss + doc.getLong("son") + ",";
		}
		sfw.write(ss + "\r\n");
		sfw.flush();
		classConceptMap.put(2, sMap);
		MongoCursor<Document> tcursor = col.find(new Document("parent",new Document("$in",sMap.keySet()))).iterator();
		while (tcursor.hasNext()) {
			Document doc = tcursor.next();
			tMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
			ts = ts + doc.getLong("son") + ",";
		}
		tfw.write(ts);
		tfw.flush();
		ffw.close();
		sfw.close();
		tfw.close();
		classConceptMap.put(3, tMap);
	}

	private static Map<Integer, Map<Long, String>> getClassConceptMap() {
		Map<Integer, Map<Long, String>> classConceptMap = new HashMap<Integer, Map<Long,String>>();
		System.out.println(ConstResource.KG);
		MongoCollection<Document> col = kgClient.getDatabase(ConstResource.KG).getCollection("parent_son");
		Map<Long,String> fMap = new HashMap<Long,String>();
		Map<Long,String> sMap = new HashMap<Long,String>();
		Map<Long,String> tMap = new HashMap<Long,String>();
		MongoCursor<Document> fcursor = col.find(new Document("parent",5L)).iterator();
		while (fcursor.hasNext()) {
			Document doc = fcursor.next();
			fMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
		}
		classConceptMap.put(1, fMap);
		MongoCursor<Document> scursor = col.find(new Document("parent",new Document("$in",fMap.keySet()))).iterator();
		while (scursor.hasNext()) {
			Document doc = scursor.next();
			sMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
		}
		classConceptMap.put(2, sMap);
		MongoCursor<Document> tcursor = col.find(new Document("parent",new Document("$in",sMap.keySet()))).iterator();
		while (tcursor.hasNext()) {
			Document doc = tcursor.next();
			tMap.put(doc.getLong("son"), getNameById(doc.getLong("son")));
		}
		classConceptMap.put(3, tMap);
		return classConceptMap;
	}

	/**
	 * index 分词  整库标注
	 * @throws Exception
	 */
	public void doSimpleTagByIndexUsingDB() throws Exception{
		log.info("start tagging ");
		String sourceHost = ConstResource.MONGOURL;
		int sourcePort = ConstResource.MONGOPORT;
		String sourceDB = ConstResource.MONGOSOURCEDB;
		String sourceCol = ConstResource.MONGOSOURCECOL;
		String sourceType = "mongo";
		String targetHost = ConstResource.ES_URL;
		int targetPort = ConstResource.ES_PORT;
		String targetCluster = ConstResource.ES_CLUSTER_NAME;
		String targetDB = ConstResource.ES_INDEX;
		String targetCol = ConstResource.ES_TYPE;
		String targetType = "mongo";
		int option = 1;
		MongoClient sourceClient = null;
		MongoClient targetMongoClient = null;
		Connection sqlConn = null;
		if (sourceType.equals("mongo")) {
//			sourceClient = new MongoClient(sourceHost,sourcePort);
			sourceClient = MongoSingleton.getInstance().getMongoClient();
		}
		TransportClient targetEsClient = null;
		BulkRequestBuilder bulk = null;
		List<DBObject> resultDocList = new ArrayList<DBObject>();
		if (targetType.equals("mongo")) {
			targetMongoClient = new MongoClient(targetHost,targetPort);
		} else if (targetType.equals("es")) {
			Settings settings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", targetCluster).build();
			targetEsClient = new TransportClient(settings);
			targetEsClient.addTransportAddress(new InetSocketTransportAddress(targetHost,targetPort));
			bulk = targetEsClient.prepareBulk();
		}
		String taggingDBName = ConstResource.KG;
		DBCollection col = kgClient.getDB(taggingDBName).getCollection("parent_son");
		String[] taggingField = ConstResource.FIELDS.split(",");
		List<Long> entitySonList = new ArrayList<Long>();
		entitySonList.addAll(findAllSon(col, 5L));
		List<Long> conceptSonList = new ArrayList<Long>();
		conceptSonList.addAll(findAllSon(col, 5L));
		int level = 0;
		Map<String,String> mapFields = reverseMap(ConstResource.MAPFIELDS);

		if (sourceType.equals("mongo")) {
			DBCursor cursor = null;
			cursor = sourceClient.getDB(sourceDB).getCollection(sourceCol).find().skip(skip).limit(limit);
			log.info("read info " + skip + "\t" + limit);
			cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
			int cursorCount = 1;
			int insertCount = 1000;
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				String docId = doc.get("_id").toString();
				String input = "";
				while (cursorCount++ % insertCount == 0) {
					if (targetType.equals("mongo")) {
						if (resultDocList.size() > 0) {
							targetMongoClient.getDB(targetDB).getCollection(targetCol).insert(resultDocList);
							resultDocList = new ArrayList<DBObject>();
							log.info("insert " + insertCount +" successful ");
						}
					} else if (targetType.equals("es")) {
						if (bulk.numberOfActions() > 0) {
							BulkResponse br = bulk.execute().actionGet();
							if (br.hasFailures()) {
								log.info(br);
								log.error("索引失败" + cursorCount);
								Exception e = new Exception("error");
								throw e;
							}
							bulk = targetEsClient.prepareBulk();
						}
					}
				}
				for (String field : taggingField) {
					if (doc.containsKey(field)) {
						if (doc.get(field) != null) {
							input = input + Jsoup.parse(doc.get(field).toString()).text() + "\t";
						}
					}
				}
				input = input.toLowerCase();
				String text = input;
				List<TaggingItem> taggingList = new ArrayList<TaggingItem>();
				List<TaggingItem> parentTaggingList = new ArrayList<TaggingItem>();
				List<TaggingItem> fparentTaggingList = new ArrayList<TaggingItem>();
				List<TaggingItem> sparentTaggingList = new ArrayList<TaggingItem>();
				List<TaggingItem> tparentTaggingList = new ArrayList<TaggingItem>();
				try{
					long t0 = System.currentTimeMillis();
					Map<String, List<TaggingItem>> tagResultMap = getTagProcess(taggingDBName, text, conceptSonList, entitySonList, level);
					taggingList = tagResultMap.get("tagging");
					parentTaggingList = tagResultMap.get("taggingParent");
					fparentTaggingList = tagResultMap.get("ftaggingParent");
					sparentTaggingList = tagResultMap.get("staggingParent");
					tparentTaggingList = tagResultMap.get("ttaggingParent");
//					log.info("process using " + (System.currentTimeMillis() - t0) + "\t" +System.currentTimeMillis());
					if (option == 1) {
						//insert
						if (targetType.equals("mongo")) {
							//mongo 插入
							DBObject resultDoc = new BasicDBObject();
							for (String key : doc.keySet()) {
								if (mapFields.containsKey(key)) {
									if (key.equals("content")) {
										List<String> contentList = new ArrayList<String>();
										contentList = ContentFilter.getSplitContent(doc.get("content").toString());
										resultDoc.put(mapFields.get(key), contentList);
									} else {
										resultDoc.put(mapFields.get(key), doc.get(key) != null ? doc.get(key) : "");
									}
								}
							}
							if (taggingList.size() > 0) {
								resultDoc.put("annotation_tag", JSON.toJSONString(taggingList));
							} else {
								resultDoc.put("annotation_tag", new ArrayList<>());
							}

							if (parentTaggingList.size() > 0) {
								resultDoc.put("parent_annotation_tag", JSON.toJSONString(parentTaggingList));
							} else {
								resultDoc.put("parent_annotation_tag", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultDoc.put("annotation_1", JSON.toJSONString(fparentTaggingList));
							} else {
								resultDoc.put("annotation_1", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultDoc.put("annotation_2", JSON.toJSONString(sparentTaggingList));
							} else {
								resultDoc.put("annotation_2", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultDoc.put("annotation_3", JSON.toJSONString(tparentTaggingList));
							} else {
								resultDoc.put("annotation_3", new ArrayList<>());
							}
							resultDocList.add(resultDoc);
						} else if (targetType.equals("es")) {
							//es
							JSONObject resultObj = new JSONObject();
							for (String key : doc.keySet()) {
								if (mapFields.containsKey(key)) {
									if (key.equals("content")) {
										List<String> contentList = new ArrayList<String>();
										if (doc.get("content") != null) {
											contentList = ContentFilter.getSplitContent(doc.get("content").toString());
										}
										resultObj.put(mapFields.get(key), contentList);
									} else {
										resultObj.put(mapFields.get(key), doc.get(key) != null ? doc.get(key).toString() : "");
									}
								}
							}
							if (taggingList.size() > 0) {
								resultObj.put("annotation_tag", JSONArray.parse(JSON.toJSONString(taggingList)));
							} else {
								resultObj.put("annotation_tag", new ArrayList<>());
							}

							if (parentTaggingList.size() > 0) {
								resultObj.put("parent_annotation_tag", JSONArray.parse(JSON.toJSONString(parentTaggingList)));
							} else {
								resultObj.put("parent_annotation_tag", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultObj.put("annotation_1", JSONArray.parse(JSON.toJSONString(fparentTaggingList)));
							} else {
								resultObj.put("annotation_1", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultObj.put("annotation_2", JSONArray.parse(JSON.toJSONString(sparentTaggingList)));
							} else {
								resultObj.put("annotation_2", new ArrayList<>());
							}
							if (parentTaggingList.size() > 0) {
								resultObj.put("annotation_3", JSONArray.parse(JSON.toJSONString(tparentTaggingList)));
							} else {
								resultObj.put("annotation_3", new ArrayList<>());
							}

							String source = JSON.toJSONString(resultObj);
							bulk.add(targetEsClient.prepareIndex(targetDB, targetCol).setSource(source));
						}
					} else if (option == 0){
						//update
						if (targetType.equals("mongo")) {
							targetMongoClient.getDB(targetDB).getCollection(targetCol).update(new BasicDBObject("_id",docId), new BasicDBObject("$set",
									new BasicDBObject("annotation_tag",JSON.toJSONString(taggingList))));
						} else if (targetType.equals("es")) {
							//TODO es

						}
					}
				}catch(Exception e){
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					log.error(sw.toString());
					throw e;
				}
			}
		}

		if (targetType.equals("mongo")) {
			if (resultDocList.size() > 0) {
				targetMongoClient.getDB(targetDB).getCollection(targetCol).insert(resultDocList);
				log.info("result doc insert success");
			}
			if (sourceType.equals("mongo")) {
//				sourceClient.close();
			} else if (sourceType.equals("mysql")) {
				sqlConn.close();
			}
			targetMongoClient.close();
		} else if (targetType.equals("es")) {
			if (bulk.numberOfActions() > 0) {
				BulkResponse br = bulk.execute().actionGet();
				if (br.hasFailures()) {
					log.info(br);
					log.error("索引失败");
					Exception e = new Exception("error");
					throw e;
				}
			}
			if (sourceType.equals("mongo")) {
//				sourceClient.close();
			} else if (sourceType.equals("mysql")) {
				sqlConn.close();
			}
			targetEsClient.close();
		}
	}

	public static JSONObject doSimpleTagByIndexUsingDB(String docString) throws Exception {
//		log.info(Thread.currentThread().getId() + " start tagging " + docString);
		long t1 = System.currentTimeMillis();
		String taggingDBName = ConstResource.KG;
		MongoCollection<Document> col = kgClient.getDatabase(taggingDBName).getCollection("parent_son");
		String[] taggingField = ConstResource.FIELDS.split(",");
		List<Long> entitySonList = new ArrayList<Long>();
		ConstResource.INSTANCELIST.forEach(instance -> entitySonList.addAll(findAllSon(col, instance)));
		List<Long> conceptSonList = new ArrayList<Long>();
		ConstResource.CONCEPTLIST.forEach(concept -> conceptSonList.addAll(findAllSon(col, concept)));
		int level = 0;
		JSONObject doc = JSONObject.parseObject(docString);
		Map<String,String> mapFields = reverseMap(ConstResource.MAPFIELDS);
		String input = "";
		for (String field : taggingField) {
			if (doc.containsKey(field)) {
				if (doc.get(field) != null) {
					input = input + Jsoup.parse(doc.get(field).toString()).text() + "\t";
				}
			}
		}
		input = input.toLowerCase();
		String text = input;
		List<TaggingItem> taggingList = new ArrayList<TaggingItem>();
		List<TaggingItem> parentTaggingList = new ArrayList<TaggingItem>();
		JSONObject jsonObject = new JSONObject();
		try{
			long t0 = System.currentTimeMillis();
			Map<String, List<TaggingItem>> tagResultMap = getTagProcess(taggingDBName, text, conceptSonList, entitySonList, level);
			taggingList = tagResultMap.get("tagging");
			parentTaggingList = tagResultMap.get("taggingParent");
//			log.info("process using " + (System.currentTimeMillis() - t0) + "\t" +System.currentTimeMillis());
			//insert
			//mongo 插入
			for (String key : doc.keySet()) {
				if (mapFields.containsKey(key)) {
					if (key.equals("content")) {
						List<String> contentList = new ArrayList<String>();
						contentList = ContentFilter.getSplitContent(doc.get("content").toString());
						jsonObject.put(mapFields.get(key), contentList);
					} else {
						jsonObject.put(mapFields.get(key), doc.get(key) != null ? doc.get(key) : "");
					}
				}
			}
			if (taggingList.size() > 0) {
				jsonObject.put("annotation_tag", JSON.toJSONString(taggingList));
			} else {
				jsonObject.put("annotation_tag", new ArrayList<>());
			}

			if (parentTaggingList.size() > 0) {
				jsonObject.put("parent_annotation_tag", JSON.toJSONString(parentTaggingList));
			} else {
				jsonObject.put("parent_annotation_tag", new ArrayList<>());
			}
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		log.info(Thread.currentThread().getId() + "process using " + (System.currentTimeMillis() - t1));
		return jsonObject;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String,List<TaggingItem>> getTagProcess(String taggingDBName, String text, List<Long> conceptSonList, List<Long> entitySonList, int level) {
		Map<String,List<TaggingItem>> taggingResultMap = Maps.newHashMap();
		List<TaggingItem> taggingList = Lists.newArrayList();
		List<TaggingItem> ftaggingList = Lists.newArrayList();
		List<TaggingItem> staggingList = Lists.newArrayList();
		List<TaggingItem> ttaggingList = Lists.newArrayList();
		Set<Long> fset = new HashSet<Long>();
		Set<Long> sset = new HashSet<Long>();
		Set<Long> tset = new HashSet<Long>();
		List<TaggingItem> parentTaggingList = Lists.newArrayList();
		Map<String, Map<String,Object>> map = SemanticSegUtil.segByDbIndex(taggingDBName, text,conceptSonList,entitySonList);
		Set<String> ansjWord = AnsjUtil.getAnsjWord(text, map.keySet());
		Map<Long, Integer> idMap = new HashMap<Long, Integer>();
		
		double baseScore = 1 / Math.sqrt(text.length());
		Map<Long,Map<Long,String>> parentIdNameMap = new HashMap<Long, Map<Long,String>>();
		for(String word : map.keySet()){
			if (isChinese(word)) {
				if (!ansjWord.contains(word)) continue;
			}
			List<Long> idList = null;
			List<Long> entityList = (List) map.get(word).get("id");
			if(entityList.size() > 0){
				idList = entityList;
			}
			if(idList != null){
				for(int i=0;i<idList.size();i++){
					long id = idList.get(i);
					parentIdNameMap.put(id, (Map<Long, String>) map.get(word).get("parent"));
					idMap.put(id, idMap.containsKey(id) ? (Integer)map.get(word).get("count") + idMap.get(id) : (Integer)map.get(word).get("count"));
				}
			}
		}
		
		Map<Long,String> allParentMap = new HashMap<Long, String>();
		
		for(long id : idMap.keySet()){
			Map<String,Long> nameMap = getNameMapById(taggingDBName, id);
			List<TaggingItem> tempItemList = new ArrayList<TaggingItem>();
			for (String name : nameMap.keySet()) {
				TaggingItem item = new TaggingItem(id, name);
				item.setScore(idMap.get(id) * baseScore);
				item.setClassId(nameMap.get(name));
				tempItemList.add(item);
				if (classConceptMap.get(1).keySet().contains(id)) {
					if (!fset.contains(id)) {
						ftaggingList.add(new TaggingItem(id, classConceptMap.get(1).get(id),getConceptById(id)));
						fset.add(id);
					} 
				} else if (classConceptMap.get(2).keySet().contains(id)) {
					if (!sset.contains(id)) {
						staggingList.add(new TaggingItem(id, classConceptMap.get(2).get(id),getConceptById(id)));
						sset.add(id);
					}
					long fid = getConceptById(id);
					if (!fset.contains(fid)) {
						ftaggingList.add(new TaggingItem(fid, classConceptMap.get(1).get(fid),getConceptById(fid)));
						fset.add(id);
					} 
				} else if (classConceptMap.get(3).keySet().contains(id)) {
					if (!tset.contains(id)) {
						ttaggingList.add(new TaggingItem(id, classConceptMap.get(3).get(id),getConceptById(id)));
						tset.add(id);
					}
					long sid = getConceptById(id);
					if (!sset.contains(sid)) {
						staggingList.add(new TaggingItem(sid, classConceptMap.get(2).get(sid),getConceptById(sid)));
						sset.add(sid);
					} 
					long fid = getConceptById(getConceptById(id));
					if (!fset.contains(fid)) {
						ftaggingList.add(new TaggingItem(fid, classConceptMap.get(1).get(fid),getConceptById(fid)));
						fset.add(id);
					} 
				}
			}
			if (level == 0) {
				taggingList.addAll(tempItemList);
				if (parentIdNameMap.get(id) != null) {
					allParentMap.putAll(parentIdNameMap.get(id));
				}
			} else {
				
			}
		}
		
		for (Long allParentKey : allParentMap.keySet()) {
			TaggingItem item = new TaggingItem(allParentKey,allParentMap.get(allParentKey));
			parentTaggingList.add(item);
		}
		
		taggingResultMap.put("tagging", taggingList);
		taggingResultMap.put("taggingParent", parentTaggingList);
		taggingResultMap.put("ftaggingParent", ftaggingList);
		taggingResultMap.put("staggingParent", staggingList);
		taggingResultMap.put("ttaggingParent", ttaggingList);
		
		return taggingResultMap;
	}
	
	// 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
 
    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

	private static Map<String,Long> getNameMapById(String kgName, Long id) {
		String name = "";
		long concept = 0L;
		Map<String,Long> nameMap = new HashMap<String,Long>();
		DBCursor cursor = kgClient.getDB(ConstResource.KG)
				.getCollection("entity_id").find(new BasicDBObject("id",id));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				name = doc.get("name").toString();
				concept = doc.get("concept_id") != null ? Long.valueOf(doc.get("concept_id").toString()) : 0L;
				nameMap.put(name, concept);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			cursor.close();
		}
		return nameMap;
	}
	
	private static Long getConceptById(Long id) {
		String name = "";
		long concept = 0L;
		Map<String,Long> nameMap = new HashMap<String,Long>();
		DBCursor cursor = kgClient.getDB(ConstResource.KG)
				.getCollection("entity_id").find(new BasicDBObject("id",id));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				name = doc.get("name").toString();
				concept = doc.get("concept_id") != null ? Long.valueOf(doc.get("concept_id").toString()) : 0L;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			cursor.close();
		}
		return concept;
	}
	
	private static String getNameById(Long id) {
		String name = "";
		DBCursor cursor = kgClient.getDB(ConstResource.KG)
				.getCollection("entity_id").find(new BasicDBObject("id",id));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				name = doc.get("name").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			cursor.close();
		}
		return name;
	}
	
	private List<String> getNameById(String kgName, Long id) {
		String name = "";
		List<String> nameList = new ArrayList<String>();
		DBCursor cursor = kgClient.getDB(kgName)
				.getCollection("entity_id").find(new BasicDBObject("id",id));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				name = doc.get("name").toString();
				nameList.add(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			cursor.close();
		}
		return nameList;
	}
	
	private List<Long> getEntityId(String kgName, String word) {
		List<Long> list = new ArrayList<Long>();
		DBCursor cursor = kgClient.getDB(kgName)
				.getCollection("entity_id").find(new BasicDBObject("name",word).append("type", 1));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				list.add((Long)doc.get("id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		return list;
	}
	
	public List<Long> findAllSon(DBCollection col,long id) {
		List<Long> sonList = new ArrayList<Long>();
		DBCursor cursor = col.find(new BasicDBObject("parent",id));
		try {
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				long sonId = Long.valueOf(doc.get("son").toString());
				sonList.add(sonId);
				List<Long> gSonList = findAllSon(col, sonId);
				sonList.addAll(gSonList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sonList;
	}

	public static List<Long> findAllSon(MongoCollection<Document> col,long id) {
		List<Long> sonList = new ArrayList<Long>();
		MongoCursor<Document> cursor = col.find(new Document("parent",id)).iterator();
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				long sonId = Long.valueOf(doc.get("son").toString());
				sonList.add(sonId);
				List<Long> gSonList = findAllSon(col, sonId);
				sonList.addAll(gSonList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sonList;
	}

	private static Map<String,String> reverseMap(String str) {
		Map<String,String> originMap = JSON.parseObject(str, Map.class);
		Map<String,String> map = new HashMap<String,String>();
		for (Entry<String,String> entry : originMap.entrySet()) {
			map.put(entry.getValue(), entry.getKey());
		}
		return map;
	}

	@Override
	public void run() {
		try {
			doSimpleTagByIndexUsingDB();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
