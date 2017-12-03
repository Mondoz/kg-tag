package com.hiekn.kg.service.tagging.main;

import com.alibaba.fastjson.JSONObject;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import com.hiekn.kg.service.tagging.mongo.KGMongoSingleton;
import com.hiekn.kg.service.tagging.util.*;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BufferedRunner {

	static MongoClient kgClient = KGMongoSingleton.getInstance().getMongoClient();
	static Logger log = Logger.getLogger(BufferedRunner.class);
	public static void main(String[] args) {
		java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(java.util.logging.Level.SEVERE);
		try {
//			String filePath = args[0];
//			String outputPath = args[1];
//			String outputPath = "data/paper";
//			String filePath = "data/paper_with_id.txt";
			String outputPath = "data/patent";
			String filePath = "data/patent.txt";
			int allCount = getCount(filePath);
			log.info("get count" + allCount);
			int threads = ConstResource.THREADCOUNT;
			CountDownLatch latch = new CountDownLatch(threads);
			ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

			String taggingDBName = ConstResource.KG;
			MongoCollection<Document> col = kgClient.getDatabase(taggingDBName).getCollection("parent_son");
			List<Long> entitySonList = new ArrayList<Long>();
			ConstResource.INSTANCELIST.forEach(instance -> entitySonList.addAll(TaggerUtil.findAllSon(col, instance)));
			List<Long> conceptSonList = new ArrayList<Long>();
			ConstResource.CONCEPTLIST.forEach(concept -> conceptSonList.addAll(TaggerUtil.findAllSon(col, concept)));
			SemanticSegUtil.segInit(entitySonList,conceptSonList);
			AnsjUtil.init(taggingDBName);
			int limit = allCount / threads;
			for (int i = 0; i < threads; i++) {
				if (i == threads-1) {
					pool.submit(new LocalReader(i*limit, allCount - (threads-1) * limit,filePath,outputPath,latch,i));
					log.info("last thread " + i + "\t" + i*limit + "\t" + (allCount - (threads-1) * limit));
				} else {
					pool.submit(new LocalReader(i*limit, limit,filePath,outputPath,latch,i));
					log.info(i + "\t" + i*limit + "\t" + limit);
				}
			}
			latch.await();
			log.info("all task finish");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getCount(String filePath) {
		log.info("start count");
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String input = "";
			while ((input = br.readLine())!=null) {
				count++;
				if (count % 50000 == 0) log.info("50000 finish");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return count;
	}

}

class LocalReader implements Runnable {

	static Logger log = Logger.getLogger(LocalReader.class);

	private int skip;
	private int limit;
	private String filePath;
	private String outputPath;
	private CountDownLatch latch;
	int fileNum;

	public LocalReader(int skip,int limit,String filePath,String outputPath,CountDownLatch latch,int fileNum) {
		this.skip = skip;
		this.limit = limit;
		this.filePath = filePath;
		this.latch = latch;
		this.fileNum = fileNum;
		this.outputPath = outputPath;
	}

	@Override
	public void run() {
		try {
			File fileFolder = new File(outputPath);
			if (!fileFolder.exists()) fileFolder.mkdirs();
			FileWriter fw = new FileWriter(outputPath + "/" + fileNum +".txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String input = "";
			int count = 0;
			int ccount = 0;
			int bulk = ConstResource.BULK;
			StringBuffer sb = new StringBuffer();
			String[] taggingField = ConstResource.FIELDS.split(",");
			int level = 0;
			while ((input = br.readLine())!=null) {
				long t1 = System.currentTimeMillis();
				if (count++ < skip) continue;
				if (count > skip + limit) break;
				ccount++;
				try {
					JSONObject doc = JSONObject.parseObject(input);
					Map<String,String> mapFields = TaggerUtil.reverseMap(ConstResource.MAPFIELDS);
					for (String field : taggingField) {
						if (doc.containsKey(field)) {
							if (doc.get(field) != null) {
								input = input + Jsoup.parse(doc.get(field).toString()).text() + "\t";
							}
						}
					}
					input = input.toLowerCase();
					String text = input;
					List<TaggingItem> taggingList;
					List<TaggingItem> parentTaggingList;
					JSONObject jsonObject;
					try{
//						Map<String, List<TaggingItem>> tagResultMap = TaggerUtil.getTagProcess(taggingDBName, text, conceptSonList, entitySonList, level);
						Map<String, List<TaggingItem>> tagResultMap = SemanticSegUtil.ansjSeg(ConstResource.KG, text);
						taggingList = tagResultMap.get("tagging");
						parentTaggingList = tagResultMap.get("taggingParent");
						jsonObject = doc;
						if (taggingList.size() > 0) {
							if (doc.containsKey("kg_ents")) {
								taggingList.addAll(doc.getObject("kg_ents",List.class));
								jsonObject.put("annotation_tag", taggingList);
								jsonObject.remove("kg_ents");
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
						log.error(e);
						throw e;
					}
					log.info(Thread.currentThread().getId() + " process using " + (System.currentTimeMillis() - t1));
					sb.append(jsonObject.toJSONString() + "\r\n");
					if (ccount % bulk == 0) {
						log.info(bulk + " finish");
						fw.write(sb.toString());
						fw.flush();
						sb = new StringBuffer();
					}
				} catch (Exception e) {
					sb.append(input);
					System.out.println(input);
				    e.printStackTrace();
				}
			}
			if (sb.length() > 0) {
				fw.write(sb.toString());
				fw.flush();
			}
			latch.countDown();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}
}