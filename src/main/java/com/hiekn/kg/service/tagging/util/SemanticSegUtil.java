package com.hiekn.kg.service.tagging.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.elasticsearch.common.collect.Lists;

import java.io.BufferedReader;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2014-5-29
 * 
 */
public class SemanticSegUtil {
    static Logger log = Logger.getLogger(SemanticSegUtil.class);
    
    static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();
    
    /**
     * 记录每一个kg中概念的层级结构 第一个key dbname 第二个key 当前概念 第三个key 当前概念的父概念id名称
     */
	public static Map<String,Map<Long,Map<Long,String>>> kgNameParentIdMap = new HashMap<String, Map<Long,Map<Long,String>>>();
    
    /**
     * kgName 实例object list
     */
    public static Map<String,List<TaggingItem>> kgWordMap = Maps.newHashMap();
    public static Map<String,Map<String,List<TaggingItem>>> kgWordIdMap = Maps.newHashMap();

    public static void main(String[] args) {
    	try {
    		String taggingDBName = ConstResource.KG;
			MongoCollection<Document> col = kgClient.getDatabase(taggingDBName).getCollection("parent_son");
			String[] taggingField = ConstResource.FIELDS.split(",");
			List<Long> entitySonList = new ArrayList<Long>();
			ConstResource.INSTANCELIST.forEach(instance -> entitySonList.addAll(TaggerUtil.findAllSon(col, instance)));
			List<Long> conceptSonList = new ArrayList<Long>();
			ConstResource.CONCEPTLIST.forEach(concept -> conceptSonList.addAll(TaggerUtil.findAllSon(col, concept)));
			String input = "";
			BufferedReader br = BufferedReaderUtil.getBuffer("data/patent_with_id.txt");
			while ((input = br.readLine()) != null) {
				ansjSeg(ConstResource.KG, input);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void segInit(List<Long> entityList, List<Long> conceptList) {
		//初始化父概念map
		String kgName = ConstResource.KG;
		if (!kgNameParentIdMap.containsKey(kgName)) {
			synchronized (SemanticSegUtil.class) {
				if (!kgNameParentIdMap.containsKey(kgName)) {
					initParentMap(kgName);
				}
			}
		}
		Map<Long,Map<Long,String>> parentIdNameMap = kgNameParentIdMap.get(kgName);
		//初始化实例map
		if (!kgWordMap.containsKey(kgName)) {
			synchronized (SemanticSegUtil.class) {
				if (!kgWordMap.containsKey(kgName)) {
					initKgWordMap(kgName, entityList, conceptList);
				}
			}
		}
	}

	public static Map<String,List<TaggingItem>> ansjSeg(String kgName,String txt) {
		long t1 = System.currentTimeMillis();
		Map<String,List<TaggingItem>> resultTagMap = Maps.newHashMap();
		List<TaggingItem> taggingItemList = Lists.newArrayList();
		List<TaggingItem> parentTaggingItemList = Lists.newArrayList();
		Set<String> ansjWord = AnsjUtil.getAnsjWord(txt);
		Map<String, List<TaggingItem>> wordIdMap = kgWordIdMap.get(kgName);
		for (String s : ansjWord) {
			if (wordIdMap.keySet().contains(s)) {
				taggingItemList.addAll(wordIdMap.get(s));
			}
		}
		for (TaggingItem taggingItem : taggingItemList) {
			long classId = taggingItem.getClassId();
			Map<Long, String> classParentMap = kgNameParentIdMap.get(kgName).get(classId);
			for (Entry<Long, String> entry : classParentMap.entrySet()) {
				TaggingItem parentItem = new TaggingItem(entry.getKey(), entry.getValue());
				parentTaggingItemList.add(parentItem);
			}
		}
		resultTagMap.put("tagging", taggingItemList);
		resultTagMap.put("taggingParent", parentTaggingItemList);
		System.out.println(System.currentTimeMillis() - t1);
//		System.out.println(JSON.toJSON(taggingItemList));
//		System.out.println(JSON.toJSON(parentTaggingItemList));
		return resultTagMap;
	}
	
	/**
	 * 输入文档 直接返回实体概念词频
	 * @param kgName
	 * @param text
	 * @return
	 */
	public static Map<String,ParentItemBean> segByDbIndex(String kgName, String text, List<Long> conceptList,List<Long> entityList){
		long t0 = System.currentTimeMillis();
//		log.info("start seg");
		//初始化父概念map
		if (!kgNameParentIdMap.containsKey(kgName)) {
			synchronized (SemanticSegUtil.class) {
				if (!kgNameParentIdMap.containsKey(kgName)) {
					initParentMap(kgName);
				}
			}
		}
		Map<Long,Map<Long,String>> parentIdNameMap = kgNameParentIdMap.get(kgName);
		Map<String,ParentItemBean> dataMap = new HashMap<String,ParentItemBean>();
		//初始化实例map
		if (!kgWordMap.containsKey(kgName)) {
			synchronized (SemanticSegUtil.class) {
				if (!kgWordMap.containsKey(kgName)) {
					initKgWordMap(kgName, entityList, conceptList);
				}
			}
		}
		try {
			if (kgWordMap.containsKey(kgName)) {
				List<TaggingItem> objList = kgWordMap.get(kgName);
				long t1 = System.currentTimeMillis();
				for (TaggingItem dbObj : objList) {
					String word = dbObj.getName().toLowerCase();
					long id = dbObj.getId();
					long conceptId = dbObj.getClassId();
					if (word.length() > 1) {
						int wordCount = getCount2(text, word);
						if (wordCount != 0) {
							if (!dataMap.containsKey(word)) {
								ParentItemBean pi = new ParentItemBean();
								pi.setCount(wordCount);
								pi.setId(Lists.newArrayList(id));
								pi.setParentNameIdMap(parentIdNameMap.get(conceptId));
								dataMap.put(word, pi);
							}
							else {
								dataMap.get(word).getId().add(id);
							}
						}
					}
				}
				System.out.println("count using" + (System.currentTimeMillis() - t1));
			} else {
				log.info("kgWordMap dont contains " + kgName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("seg using " + (System.currentTimeMillis() - t0));
		return dataMap;
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

    private static int getCount2(String input, String word) {
		int count = 0;
		if (input.contains(word)) {
			count = 1;
		}
		return count;
	}
	
	private static int getCount(String input, String word) {
		int count = 0;
		int index = 0;
		while ((index = input.indexOf(word,index)) != -1) {
			if (isChinese(word)) {
				index = index + word.length();
				count++;
			} else {
				if (index > 0) {
					if (index + word.length() >= input.length()) {
						char i = input.charAt(index-1);
						if (!isEnglishCharactor(i)) {
							index = index + word.length();
							count++;
						} else {
							index = index + word.length();
						}
					} else {
						char i = input.charAt(index-1);
						char lastI = input.charAt(index+word.length());
						if (!isEnglishCharactor(i) && !isEnglishCharactor(lastI)) {
							index = index + word.length();
							count++;
						} else {
							index = index + word.length();
						}
					}
				} else {
					if (index + word.length() >= input.length()) {
						index = index + word.length();
						count++;
					} else {
						char i = input.charAt(index+word.length());
						if (!isEnglishCharactor(i)) {
							index = index + word.length();
							count++;
						} else {
							index = index + word.length();
						}
					}
				}
			}
		}
		return count;
	}

	private static int getCount(String input, String word,boolean bool) {
		int count = 0;
		if (input.contains(word)) count = 1;
		return count;
	}
	
	private static void initKgWordMap(String kgName, List<Long> entityList, List<Long> conceptList) {
		long t1 = System.currentTimeMillis();
		BasicDBList searchList = new BasicDBList();
		BasicDBObject entSearchObj = new BasicDBObject();
		entSearchObj.append("concept_id",new BasicDBObject("$in",entityList));
		BasicDBObject conSearchObj = new BasicDBObject();
		conSearchObj.append("id", new BasicDBObject("$in",conceptList));
		searchList.add(entSearchObj);
		searchList.add(conSearchObj);
		MongoCursor<Document> cursor = kgClient.getDatabase(kgName).getCollection("entity_id")
				.find(new Document("$or",searchList)).iterator();
		List<TaggingItem> dbObjList = Lists.newArrayList();
//		DBCursor cursor = client.getDB(kgName).getCollection("entity_id")
//				.find(new BasicDBObject("concept_id",new BasicDBObject("$in",entityList)));
		Map<String,List<TaggingItem>> wordIdMap = Maps.newHashMap();
		try {
			while (cursor.hasNext()) {
				Document obj = cursor.next();
				String name = obj.getString("name");
				Long id = obj.getLong("id");
				Long concept_id = obj.getLong("concept_id");
				TaggingItem eb = new TaggingItem();
				eb.setId(id);
				eb.setClassId(concept_id);
				eb.setName(name);
				dbObjList.add(eb);
				if (wordIdMap.containsKey(name)) {
					wordIdMap.get(name).add(eb);
				} else {
					wordIdMap.put(name, Lists.newArrayList(eb));
				}
			}
		} catch (Exception e) {
			log.error(e + "init kg word error");
		}
		kgWordMap.put(kgName, dbObjList);
		kgWordIdMap.put(kgName, wordIdMap);
		log.info("init " + kgName + " finish using " + (System.currentTimeMillis() - t1)); 
	}
	
	private static void initParentMap(String dbName) {
		long t1 = System.currentTimeMillis();
		MongoCollection<Document> col = kgClient.getDatabase(dbName).getCollection("parent_son");
		Map<Long,Set<Long>> parentIdMap = new HashMap<Long,Set<Long>>();
		Map<Long,Map<Long,String>> parentIdNameMap = new HashMap<Long, Map<Long,String>>();
		MongoCursor<Document> cursor = col.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			long pid = Long.valueOf(doc.get("parent").toString());
			long sid = Long.valueOf(doc.get("son").toString());
			if (parentIdMap.containsKey(sid)) {
				parentIdMap.get(sid).add(pid);
			} else {
				Set<Long> pidSet = new HashSet<Long>();
				pidSet.add(pid);
				pidSet.add(sid);
				parentIdMap.put(sid, pidSet);
				if (parentIdMap.containsKey(pid)) {
					parentIdMap.get(sid).addAll(parentIdMap.get(pid));
				}
			}
		}
		
		for (Entry<Long, Set<Long>> entry : parentIdMap.entrySet()) {
			Map<Long,String> idNameMap = new HashMap<Long, String>();
			Set<Long> idSet =  entry.getValue();
			for (Long id : idSet) {
				String name = getNameById(id, dbName);
				idNameMap.put(id, name);
			}
			parentIdNameMap.put(entry.getKey(), idNameMap);
		}
		
		kgNameParentIdMap.put(dbName, parentIdNameMap);
		
		log.info("init parent " + dbName + " finish using " + (System.currentTimeMillis() - t1)); 
	}
	
	private static String getNameById(long id,String kgName) {
		String name = "";
		MongoCursor<Document> cursor = kgClient.getDatabase(kgName).getCollection("entity_id")
				.find(new Document("id",id)).iterator();
		if (cursor.hasNext()) {
			name = cursor.next().getString("name");
		}
		return name;
	}
	
	private static boolean isEnglishCharactor(char i) {
		boolean is = true;
		if (!((i > 64 && i < 91) || (i > 96 && i < 123))) {
			is = false;
		}
		return is;
	}
	
}


class ParentItemBean {

	private List<Long> id;
	private int count;
	private Map<Long, String> parentNameIdMap;

	public List<Long> getId() {
		return id;
	}

	public void setId(List<Long> id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Map<Long, String> getParentNameIdMap() {
		return parentNameIdMap;
	}

	public void setParentNameIdMap(Map<Long, String> parentNameIdMap) {
		this.parentNameIdMap = parentNameIdMap;
	}
}