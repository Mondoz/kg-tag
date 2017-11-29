package com.hiekn.kg.service.tagging.main;

import com.hiekn.kg.service.tagging.util.ConstResource;
import com.hiekn.kg.service.tagging.util.TaggerUtil;
import org.apache.hadoop.hdfs.server.namenode.snapshot.FileWithSnapshot;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BufferedRunner {

	static Logger log = Logger.getLogger(BufferedRunner.class);
	public static void main(String[] args) {
		try {
//			String filePath = args[0];
			String filePath = "data/1.txt";
			int allCount = getCount(filePath);
			int threads = ConstResource.THREADCOUNT;
			ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
			int limit = allCount / threads;
			for (int i = 0; i < threads; i++) {
				if (i == threads-1) {
					pool.submit(new LocalReader(i*limit, allCount - (threads-1) * limit,filePath));
					log.info("last thread " + i + "\t" + i*limit + "\t" + (allCount - (threads-1) * limit));
				} else {
					pool.submit(new LocalReader(i*limit, limit,filePath));
					log.info(i + "\t" + i*limit + "\t" + limit);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getCount(String filePath) {
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String input = "";
			while ((input = br.readLine())!=null) {
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return count;
	}

}

class LocalReader implements Runnable {
	private int skip;
	private int limit;
	private String filePath;

	public LocalReader(int skip,int limit,String filePath) {
		this.skip = skip;
		this.limit = limit;
		this.filePath = filePath;
	}

	@Override
	public void run() {
		try {
//			FileWriter fw = new FileWriter(filePath.substring(0,filePath.lastIndexOf("/")) + "/" + (skip / limit) +".txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			String input = "";
			int count = 0;
			while ((input = br.readLine())!=null) {
//				System.out.println(input);
				if (count++ < skip) continue;
				if (count > skip + limit) break;
//				String doc = TaggerUtil.doSimpleTagByIndexUsingDB(input).toJSONString();
//				fw.write(doc +"\r\n");
//				fw.flush();

			}
		} catch (Exception e) {

		} finally {

		}

	}
}