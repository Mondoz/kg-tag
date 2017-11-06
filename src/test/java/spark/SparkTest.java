package spark;

import com.google.common.collect.Lists;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class SparkTest {
    public static void main(String[] args) {
        sparkConnect();
    }

    public static void sparkConnect() {
        SparkConf conf = new SparkConf().setAppName("connect").setMaster("local")
                .set("spark.mongodb.input.uri", "mongodb://127.0.0.1/wanfang.achieve_list")
                .set("spark.mongodb.output.uri", "mongodb://127.0.0.1/wanfang.test");;
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = sc.parallelize(data);
        distData.foreach(x -> System.out.println(x));
    }
}




