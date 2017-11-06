package com.hiekn.kg.service.tagging.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
 
public class OkHttpUtil {
	private static final OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
				.writeTimeout(200, TimeUnit.SECONDS)
				.readTimeout(200, TimeUnit.SECONDS)
				.connectTimeout(100, TimeUnit.SECONDS).build();

	/**
	 * 该不会开启异步线程。
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static Response execute(Request request) throws IOException{
		return mOkHttpClient.newCall(request).execute();
	}
	/**
	 * 开启异步线程访问网络
	 * @param request
	 * @param responseCallback
	 */
	public static void enqueue(Request request, Callback responseCallback){
		mOkHttpClient.newCall(request).enqueue(responseCallback);
	}
	
	public static String getStringFromServer(String url) throws IOException{
		Request request = new Request.Builder().url(url).build();
		Response response = execute(request);
		if (response.isSuccessful()) {
			String responseUrl = response.body().string();
			return responseUrl;
		} else {
			throw new IOException("Unexpected code " + response);
		}
	}
	private static final String CHARSET_NAME = "UTF-8";
	/**
	 * 为HttpGet 的 url 方便的添加1个name value 参数。
	 * @param url
	 * @param name
	 * @param value
	 * @return
	 */
	public static String attachHttpGetParam(String url, String name, String value){
		return url + "?" + name + "=" + value;
	}
}