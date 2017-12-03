package com.hiekn.kg.service.tagging.util;

import com.google.common.collect.Maps;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.elasticsearch.common.collect.Lists;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2014-5-29
 * 
 */
public class SemanticSegUtil {
	private static Set<String> DIC = new HashSet<String>();
    private static int MAX_LENGTH = 0;
    static Logger log = Logger.getLogger(SemanticSegUtil.class);
    
    static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();
    
    /**
     * 记录每一个kg中概念的层级结构 第一个key dbname 第二个key 当前概念 第三个key 当前概念的父概念id名称
     */
    private static Map<String,Map<Long,Map<Long,String>>> kgNameParentIdMap = new HashMap<String, Map<Long,Map<Long,String>>>();
    
    /**
     * kgName 实例object list
     */
    private static Map<String,List<EntityBean>> kgWordMap = Maps.newConcurrentMap();
    private static Map<Integer,List<Long>> classConceptMap = new HashMap<Integer,List<Long>>();
	
	public static Set<String> getDic(){
		return DIC;
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
				List<EntityBean> objList = kgWordMap.get(kgName);
				long t1 = System.currentTimeMillis();
				for (EntityBean dbObj : objList) {
					String word = dbObj.getName().toLowerCase();
					long id = dbObj.getId();
					long conceptId = dbObj.getConceptId();
					if (word.length() > 1) {
						int wordCount = getCount2(text, word);
						if (wordCount != 0) {
							if (!dataMap.containsKey(word)) {
								ParentItemBean pi = new ParentItemBean();
								pi.setCount(wordCount);
								pi.setId(Lists.newArrayList(id));
								pi.setParentNameIdMap(parentIdNameMap.get(conceptId));
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
		Document entSearchObj = new Document();
		entSearchObj.append("concept_id",new Document("$in",entityList));
		Document conSearchObj = new Document();
		conSearchObj.append("id", new Document("$in",conceptList));
		searchList.add(entSearchObj);
		searchList.add(conSearchObj);
		MongoCursor<Document> cursor = kgClient.getDatabase(kgName).getCollection("entity_id")
				.find(new Document("$or",searchList)).iterator();
		List<EntityBean> dbObjList = Lists.newArrayList();
//		DBCursor cursor = client.getDB(kgName).getCollection("entity_id")
//				.find(new BasicDBObject("concept_id",new BasicDBObject("$in",entityList)));
		try {
			while (cursor.hasNext()) {
				Document obj = cursor.next();
				EntityBean eb = new EntityBean();
				eb.setId(obj.getLong("id"));
				eb.setConceptId(obj.getLong("concept_id"));
				eb.setName(obj.getString("name"));
				dbObjList.add(eb);
			}
		} catch (Exception e) {
			log.error(e + "init kg word error");
		}
		kgWordMap.put(kgName, dbObjList);
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

class EntityBean {

	private String name;
	private long conceptId;
	private long id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getConceptId() {
		return conceptId;
	}

	public void setConceptId(long conceptId) {
		this.conceptId = conceptId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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