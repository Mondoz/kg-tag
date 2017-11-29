package com.hiekn.kg.service.tagging.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 
 * @author govert
 *
 */
public class ConstResource {
	
	//
	// load properties 文件
	//
	static Properties props = new Properties();
	static {
		try {
			props.load(ConstResource.class.getClassLoader().getResourceAsStream("tagging.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			// 直接退出jvm
			System.exit(1);
		}
	}
	
	//
	// 索引数据本地缓存文件
	//
	public static final String INDEX_BUFFER_DIR = System.getProperty("user.dir")
			+ File.separator
			+ props.getProperty("index_buffer_dir", "index_buffer")
			+ File.separator;
	
	//thread
	public static final Integer THREADCOUNT = Integer.parseInt(props.getProperty("threads", "8"));
	public static final Integer DOCCOUNT = Integer.parseInt(props.getProperty("doc_count", "0"));
	
	//mongo
	public static final String MONGOURL = props.getProperty("mongodb_url");
	public static final Integer MONGOPORT = Integer.parseInt(props.getProperty("mongodb_port", "27017"));
	public static final String MONGOSOURCEDB = props.getProperty("mongodb_db");
	public static final String MONGOSOURCECOL = props.getProperty("mongodb_coll");
	public static final String MONGOUSER = props.getProperty("mongodb_user","admin");
	public static final String MONGOPASSWORD = props.getProperty("mongodb_pwd","123456");
	
	//kg
	public static final String KGMONGOURL = props.getProperty("kg_mongodb_url");
	public static final Integer KGMONGOPORT = Integer.parseInt(props.getProperty("kg_mongodb_port", "19130"));
	public static final String KG = props.getProperty("kg_name");
	
	//field
	public static final String FIELDS = props.getProperty("tagging_field");
	public static final String MAPFIELDS = props.getProperty("field_map");
	public static final List<Long> CONCEPTLIST = JSONArray.parseArray(props.getProperty("concept"),Long.class);
	public static final List<Long> INSTANCELIST = JSONArray.parseArray(props.getProperty("instance"),Long.class);

	//
	// elastic search
	//
	public static final String STORAGE_DB = props.getProperty("storage_db");
	public static final String ES_CLUSTER_NAME = props.getProperty("es_cluster_name", "elasticsearch");
	public static final String ES_INDEX = props.getProperty("es_index", "news");
	public static final String ES_TYPE = props.getProperty("es_type", "news_data");
	public static final String ES_URL = props.getProperty("es_url");
	public static final Integer ES_PORT = Integer.parseInt(props.getProperty("es_port", "9300"));
	
	//
	public static final int INDEX_SLEEP_MINUTES = Integer.parseInt(props.getProperty("index_sleep_min", "15"));
	
	public static final String REDIS_URL = props.getProperty("redis_url", "127.0.0.1");
	public static final int REDIS_PORT = Integer.parseInt(props.getProperty("redis_port", "6379"));
	
}
