package com.hiekn.kg.service.tagging.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hiekn.kg.service.tagging.util.BufferedReaderUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author xiaohuqi E-mail:xiaohuqi@126.com
 * @version 2016年11月28日 下午8:19:25
 * 
 */
public class T0 {
	
	private static final Logger log = Logger.getLogger(T0.class);


	@Test
	public void t7() {
		try {
			FileWriter fw = new FileWriter("data/result.txt");
			StringBuffer sb = new StringBuffer();
			String input = "";
			String str = "";
			BufferedReader br = BufferedReaderUtil.getBuffer("data/test1.json");
			while ((input = br.readLine()) != null) {
				str = input;
			}
			JSONObject obj = JSONObject.parseObject(str);
			System.out.println(obj.toJSONString());
			sb.append(obj.toJSONString());
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void t6() {
		MongoClient client = new MongoClient("127.0.0.1",27017);
		MongoCollection<Document> col = client.getDatabase("tj").getCollection("tj_test");
		MongoCursor<Document> cursor = col.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			JSONObject obj = new JSONObject();
			obj.put("list", doc.get("holder"));
			for (String key : doc.keySet()) {
				obj.put(key, doc.get(key));
			}
			System.out.println(JSON.toJSONString(obj));
			System.out.println(obj.toJSONString());
		}
		client.close();
	}
	
	@Test
	public void t5() {
		MongoClient client = new MongoClient("192.168.1.189",19130);
		MongoCollection<Document> col = client.getDatabase("kg_tj").getCollection("parent_son");
		Map<Long,Set<Long>> parentMap = new HashMap<Long,Set<Long>>();
		MongoCursor<Document> cursor = col.find().iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			long pid = doc.getLong("parent");
			long sid = doc.getLong("son");
			if (parentMap.containsKey(sid)) {
				parentMap.get(sid).add(pid);
			} else {
				Set<Long> pidSet = new HashSet<Long>();
				pidSet.add(pid);
				parentMap.put(sid, pidSet);
				if (parentMap.containsKey(pid)) {
					parentMap.get(sid).addAll(parentMap.get(pid));
				}
			}
		}
		for (Entry<Long,Set<Long>> entry : parentMap.entrySet()) {
			System.out.println(entry.getKey() + "" + entry.getValue());
		}
		client.close();
	}
	
	@Test
	public void t4() {
		String sa = "z";
		char ca = sa.charAt(0);
		String sZ = "A";
		char cZ = sZ.charAt(0);
		int ia = ca;
		int iZ = cZ;
		System.out.println(ia);
		System.out.println(iZ);
	}
	
	@Test
	public void t3() {
		long t1 = System.currentTimeMillis();
		MongoClient client = new MongoClient("47.104.13.92",19130);
		MongoCollection<Document> col = client.getDatabase("annotation_source").getCollection("dcb");
		MongoCursor<Document> cursor = col.find().iterator();
		int count = 0 ;
		long ct = System.currentTimeMillis();
		while (cursor.hasNext()) {
			cursor.next();
			if (count++ % 10 == 0) System.out.println(System.currentTimeMillis() - ct); ct = System.currentTimeMillis();
		}
		System.out.println(System.currentTimeMillis() - t1);
		client.close();
	}
	
	public List<Long> findAllSon(MongoCollection<Document> col,long id) {
		List<Long> sonList = new ArrayList<Long>();
		MongoCursor<Document> cursor = col.find(new Document("parent",id)).iterator();
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				long sonId = doc.getLong("son");
				sonList.add(sonId);
				List<Long> gSonList = findAllSon(col, sonId);
				sonList.addAll(gSonList);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return sonList;
	}
	
	@Test
	public void t2() {
		Map<String,List<Document>> dataMap = new HashMap<String, List<Document>>();
		MongoClient client = new MongoClient("192.168.1.156",27017);
		MongoCursor<Document> cursor = client.getDatabase("data").getCollection("tj_data").find() .iterator();
		try {
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				String type = doc.getString("type");
				if (dataMap.containsKey(type)) {
					dataMap.get(type).add(doc);
				} else {
					List<Document> list = new ArrayList<Document>();
					list.add(doc);
					dataMap.put(type, list);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
		System.out.println("load success");
		for (Entry<String,List<Document>> entry : dataMap.entrySet()) {
			client.getDatabase("data").getCollection("tj_" + entry.getKey()).insertMany(entry.getValue());
			System.out.println(entry.getKey() + " insert success");
		}
		client.close();
	}
	
	public static void main(String[] args) {
		List<String> entityIdList = new ArrayList<String>();
		entityIdList.add("aaa");
		entityIdList.add("bbbb");		
		DBObject whereDbo = new BasicDBObject("entity_id", new BasicDBObject(QueryOperators.IN, entityIdList));
		
		List<Integer> corpusTypeList = new ArrayList<Integer>();
		corpusTypeList.add(1);
		corpusTypeList.add(2);
		whereDbo.put("source_type", new BasicDBObject(QueryOperators.IN, corpusTypeList));
		
		whereDbo.put("name", "nane0");
		whereDbo.put("id", 100);
		whereDbo.put("version", new BasicDBObject(QueryOperators.GT, 1.5));
		
		System.out.println(whereDbo.toString());
	}

}
