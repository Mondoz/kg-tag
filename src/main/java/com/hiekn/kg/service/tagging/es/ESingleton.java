package com.hiekn.kg.service.tagging.es;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.hiekn.kg.service.tagging.util.ConstResource;

/**
 * 
 * 
 * 
 * @author pzn
 *
 */
public class ESingleton {
	
	public static void main(String[] args) {
		new ESingleton().getClient();
	}
	
	static final Logger log = Logger.getLogger(ESingleton.class);
	static final SimpleDateFormat DAY_FORMATE = new SimpleDateFormat("yyyy-MM-dd");
	
	private TransportClient client = null;
	
	public ESingleton() {
		init();
	}
	
	public void init() {
		log.info("init elastic search client ... start");
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", ConstResource.ES_CLUSTER_NAME).build();
		client = new TransportClient(settings);
		
		String[] urls = ConstResource.ES_URL.split(",");

		for (String url : urls) {
			client.addTransportAddress(new InetSocketTransportAddress(url,
					ConstResource.ES_PORT));
		}
		log.info("init elastic search client ... done");
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public TransportClient getClient() {
		return client;
	}
	
	/**
	 * don not invoke directly
	 */
	public void close(){
		log.info("close elastic search client ... start");
		if (client != null) {
			client.close();
		}
		log.info("close elastic search client ... done");
	}
	
}
