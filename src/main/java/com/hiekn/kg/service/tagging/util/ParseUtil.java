package com.hiekn.kg.service.tagging.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.jsoup.Jsoup;

import java.util.Map;

public class ParseUtil {

	public static String parse(JSONObject docObj, Map<String,String> fieldsMap) {
		String result = "";
		for (String field : fieldsMap.keySet()) {
			if (docObj.containsKey(field)) {
				if (docObj.get(field) != null) {
					if (fieldsMap.get(field).equals("")) {
						result = result + Jsoup.parse(docObj.get(field).toString()).text() + "\t";
					} else {
						result = result + Jsoup.parse(JSONPath.eval(docObj, fieldsMap.get(field)).toString()).text() + "\t";
					}
				}
			}
		}
		result = result.toLowerCase();
		return result;
	}
}
