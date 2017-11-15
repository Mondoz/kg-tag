package com.hiekn.kg.service.tagging.main;


import com.hiekn.kg.service.tagging.util.ConstResource;
import com.hiekn.kg.service.tagging.util.TaggerUtil;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TaggingRunner {

	static Logger log = Logger.getLogger(TaggingRunner.class);

	public static void main(String[] args) {
		try {
			int allDoc = ConstResource.DOCCOUNT;
			int threads = ConstResource.THREADCOUNT;
			ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
			int limit = allDoc / threads;
			for (int i = 0; i < threads; i++) {
				if (i == threads-1) {
					pool.submit(new TaggerUtil(i*limit, allDoc - (threads-1) * limit));
					log.info("last thread " + i + "\t" + i*limit + "\t" + (allDoc - (threads-1) * limit));
				} else {
					pool.submit(new TaggerUtil(i*limit, limit));
					log.info(i + "\t" + i*limit + "\t" + limit);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
