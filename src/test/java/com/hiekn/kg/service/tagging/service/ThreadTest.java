package com.hiekn.kg.service.tagging.service;

import java.util.concurrent.TimeUnit;

public class ThreadTest {
	static volatile boolean RUN_THREAD_FLAG= true;
	public static void main(String[] args) {
		Thread t = new Thread() {
			public void run() {
				while (RUN_THREAD_FLAG) {
					System.out.println("running");
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		System.out.println("thread start");
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		RUN_THREAD_FLAG = false;
		System.out.println("thread stop");
	}
}
