package spark;

import com.hiekn.kg.service.tagging.util.ConstResource;
import com.mongodb.spark.api.java.MongoSpark;
import com.mongodb.spark.config.ReadConfig;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.bson.Document;
import scala.Tuple2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SparkTest {
	public static void main(String[] args) {
		sparkConnect();
	}

	public static void sparkConnect() {
//		String host = "10.10.20.14:19130";

		SparkConf conf = new SparkConf().setAppName("connect").setMaster("local")
				.set("spark.mongodb.input.uri", "mongodb://"+ConstResource.KGMONGOURL + ":" + ConstResource.KGMONGOPORT +"/"+ ConstResource.KG+"." + "parent_son")
//				.set("spark.mongodb.input.uri", "mongodb://"+ConstResource.MONGOURL + ":" + ConstResource.MONGOPORT +"/"+ ConstResource.MONGOSOURCEDB+"." + ConstResource.MONGOSOURCECOL)
				.set("spark.mongodb.output.uri", "mongodb://"+ConstResource.ES_URL + ":" + ConstResource.ES_PORT+"/"+ConstResource.ES_INDEX+"." + ConstResource.ES_TYPE);;
				;
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaMongoRDD<Document> parentSonRDD = MongoSpark.load(sc);
		Map<String, String> readOverrides = new HashMap<String, String>();
		readOverrides.put("collection", "entity_id");
		ReadConfig readConfig = ReadConfig.create(sc).withJavaOptions(readOverrides);
		JavaMongoRDD<Document> entIdRDD = MongoSpark.load(sc,readConfig);

//		javaRDD.flatMap(s -> {
//			List<String> list = Lists.newArrayList();
//			if (s.contains("")) {
//				list.add(s);
//			}
//			return list;
//		});
//		JavaRDD<Document> resultRDD = rdd
//				.withPipeline(
//						Collections.singletonList(
//								new Document("$match",
//										new Document("concept_id",5))
//						))
//				.map(new Function<Document, Document>() {
//			@Override
//			public Document call(Document document) throws Exception {
////				Document resultDoc = TaggerUtil.doSimpleTagByIndexUsingDB(document);
////				return resultDoc;
//				System.out.println(document.get("name"));
//				return document;
//			}
//		});

		Map<Long,Set<Long>> parentIdMap = new HashMap<Long,Set<Long>>();
		parentSonRDD.foreach(s -> {
			long pid = s.getLong("parent");
			long sid = s.getLong("son");
			System.out.println(pid + "\t" + sid);
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
		});
		Broadcast<Map<Long,Set<Long>>> broadcastParentIdMap = sc.broadcast(parentIdMap);

		JavaPairRDD<Long,String> resultRDD = entIdRDD.mapToPair(s -> {
			String name = s.getString("name");
			long id = 0;
			try {
				id = s.getLong("id");
			} catch (Exception e) {

			} finally {

			}
			return new Tuple2<>(id,name);
		});
		resultRDD.foreach(tuple -> 	System.out.println(tuple._1 + "\t" + tuple._2));
//		resultRDD.cache();
//		JavaPairRDD<String,Object> resultRDD = sc.textFile("").;
//		resultRDD.saveAsTextFile("");
//		rdd.foreach(x -> System.out.println(x.get("title")));
//		List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
//		JavaRDD<Integer> distData = sc.parallelize(data);
//		distData.foreach(x -> System.out.println(x));
	}
}




