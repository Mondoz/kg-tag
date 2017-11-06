package com.hiekn.kg.service.tagging.mongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hiekn.kg.service.tagging.util.ConstResource;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * 
 * 里面维护一个MongoClient
 * 
 * @author pzn
 *
 */
public class MongoSingleton {
	
	/**
	 * log
	 */
	private static final Logger log = Logger.getLogger(MongoSingleton.class);
	
	/**
	 * mongo url
	 */
	private static final String MONGODB_URL = ConstResource.MONGOURL;

	/**
	 * mongo port
	 */
	private static final int MONGODB_PORT = ConstResource.MONGOPORT;

	/**
	 * mongo 重连次数
	 */
	private static final int MONGO_RETRY_TIMES = 5;
	
	private volatile static MongoSingleton instance = new MongoSingleton();
	
	public static MongoSingleton getInstance() {
		if (instance == null) {
			synchronized (MongoSingleton.class) {
				if (instance == null) {
					instance = new MongoSingleton();
				}
			}
		}
		return instance;
	}
	
	private MongoClient mgClient = null;
	
	public MongoSingleton() {
		if (!init(MONGO_RETRY_TIMES)) {
			log.error("尝试 " + MONGO_RETRY_TIMES + " 次不能连上mongodb...");
		}
	}
	
	/**
	 * 
	 * 初始化mgClient、MongoDatabase、MongoCollection
	 * 
	 * 当设置tryTimes 大于 0  时，表示最大的尝试重连次数。如果尝试连接的次数
	 * 
	 * 等于tryTimes，还是不能成功初始化mgClient、MongoDatabase、MongoCollection.
	 * 
	 * 则会返回false，表示初始化失败，否则，返回true。
	 * 
	 * @param tryTimes 连接失败时，尝试连接的最大次数.必须大于等于1，否则不会初始化mongo
	 * @return false 初始化失败 , true 初始化成功
	 */
	private boolean init(int tryTimes) {

		if (tryTimes <= 0) {
			return false;
		}

		try {
			log.info("init mongo client ... start");
			MongoClientOptions options = MongoClientOptions.builder()
					.connectionsPerHost(20).minConnectionsPerHost(1)
					.maxConnectionIdleTime(300000).maxConnectionLifeTime(180000)
					.connectTimeout(10000).socketTimeout(120000).build();
			MongoCredential credential = MongoCredential.createCredential(ConstResource.MONGOUSER,
                    "admin",
                    ConstResource.MONGOPASSWORD.toCharArray());
			String[] urls = MONGODB_URL.split(",");
			List<ServerAddress> urlList = new ArrayList<ServerAddress>();
			for (String url : urls) {
				urlList.add(new ServerAddress(url, MONGODB_PORT));
			}
			List<MongoCredential> credentialList = new ArrayList<MongoCredential>();
			credentialList.add(credential);
			mgClient = new MongoClient(urlList, options);
//			mgClient = new MongoClient(urlList, credentialList, options);
			log.info("init mongo client ... done");
			return true;
		} catch (Exception e) {
			log.error("Exception " + e);
			return init(--tryTimes);//尝试重新连接
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public MongoClient getMongoClient() {
		return mgClient;
	}
	
	/**
	 * 
	 */
	public void close() {
		log.info("close mongo client ... start");
		if (null != mgClient) {
			mgClient.close();
			mgClient = null;
		}
		log.info("close mongo client ... done");
	}
	
	/**
	 * 
	 * 
	 * db.sim_hash.aggregate(
	 *  	[
     * 			{
     * 				$group : {
     *     				_id : null,
     *     				total: { $sum: "$simi_count"},
     *     			count: { $sum: 1 }
     *  			}
     *			}
   	 *		]
     *	)  
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
}
