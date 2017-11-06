package com.hiekn.kg.service.tagging.util;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

public class JSONUtils {
	
//	private static Gson gson = new Gson();
	
//	public static Gson getGson() {
//		if (local.get() == null) {
//			GsonBuilder gsonBuilder = new GsonBuilder();
//			gsonBuilder.registerTypeAdapter(java.util.Date.class, new JsonDeserializer<java.util.Date>() {
//				public java.util.Date deserialize(com.google.gson.JsonElement p1, java.lang.reflect.Type p2,
//					com.google.gson.JsonDeserializationContext p3) {
//					return new java.util.Date(p1.getAsLong());
//				}
//			});
//			gsonBuilder.registerTypeAdapter(java.util.Date.class, new JsonSerializer<java.util.Date>(){
//
//				public JsonElement serialize(Date arg0, Type arg1,
//						JsonSerializationContext arg2) {
//					// TODO Auto-generated method stub
//					return new JsonPrimitive(arg0.getTime());
//				}
//				
//			} );
//			Gson gson = gsonBuilder.create();
//			local.set(gson);
//			return gson;
//		} else {
//			return local.get();
//		}
//	}

	public static <T> T fromJson(String json, Class<T> cls) {
//		return gson.fromJson(json, cls);
		return JSON.parseObject(json, cls);
	}
	
	public static <T> T fromJson(String json,  Type typeOfT) {
//		return gson.fromJson(json, typeOfT);
		return JSON.parseObject(json, typeOfT);
	}
	
	public static String toJson(Object obj) {
//		return gson.toJson(obj);
		return JSON.toJSONString(obj);
	}
	
	
}