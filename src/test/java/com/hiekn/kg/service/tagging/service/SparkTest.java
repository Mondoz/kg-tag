package com.hiekn.kg.service.tagging.service;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

public class SparkTest {
	
	public static void main(String[] args) {
		runner();
	}


	public static void runner() {
		String filePath = "data/paper_with_id.txt";
		SparkConf conf = new SparkConf()
				.setMaster("local")
				.setAppName("spark_test");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<Integer> rdd = sc.textFile(filePath).map(s -> s.length());
		int count = rdd.reduce((a, b) -> a + b);
		System.out.println(count);

	}
}
