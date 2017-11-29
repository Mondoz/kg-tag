package com.hiekn.kg.service.tagging.main;

import com.hiekn.kg.service.tagging.util.TaggerUtil;
import com.mongodb.spark.api.java.MongoSpark;
import com.mongodb.spark.rdd.api.java.JavaMongoRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.bson.Document;

public class SparkRunner {
	public static void main(String[] args) {
		sparkConnect(args);
	}

	public static void sparkConnect(String[] args) {
		SparkConf conf = new SparkConf()
				.setAppName("text_file");
		JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<Document> resultRDD = sc.textFile(args[0],10).repartition(10)
                .map(doc -> {
                    Document resultDoc = TaggerUtil.doSimpleTagByIndexUsingDB(doc);
                    return resultDoc;
		});
		resultRDD.coalesce(1).saveAsTextFile(args[1]);
	}
}




