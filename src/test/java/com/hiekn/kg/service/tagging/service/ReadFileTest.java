package com.hiekn.kg.service.tagging.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

public class ReadFileTest {

	public static void main(String[] args) throws FileNotFoundException {
		long startTime = System.currentTimeMillis();

		final int thNum = 2;
		final String filePath = "data/t.txt"; //266M
		CountDownLatch doneSignal = new CountDownLatch(thNum);
		RandomAccessFile[] arr = new RandomAccessFile[thNum];
		long length = new File(filePath).length();
		long everyThread = length / thNum;
		System.out.println("every" + everyThread);
		long left = length % thNum;
		for (int i = 0; i < thNum; i++) {
			arr[i] = new RandomAccessFile(filePath, "rw");

			if (i == (thNum - 1)) {
				new ReadFileThread(everyThread * i, everyThread * (i + 1) + left, arr[i], doneSignal).start();
			} else {
				new ReadFileThread(everyThread * i, everyThread * (i + 1), arr[i], doneSignal).start();
			}
		}

		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		System.out.println("The totally executed time: " + (endTime - startTime));
	}

}

class ReadFileThread extends Thread {

	private long start;
	private long end;
	private RandomAccessFile raf;
	private CountDownLatch doneSignal;
	private final int bufLen = 256;

	public ReadFileThread(long start, long end, RandomAccessFile raf, CountDownLatch doneSignal) {
		this.start = start;
		this.end = end;
		this.raf = raf;
		this.doneSignal = doneSignal;
	}

	@Override
	public void run() {
		try {
			raf.seek(start);
			long contentLen = end - start;
			long times = contentLen / bufLen + 1;
			byte[] buff = new byte[bufLen];
			int hasRead = 0;
			String result = null;
			for (int i = 0; i < times; i++) {
				hasRead = raf.read(buff);
				if (hasRead < 0) {
					break;
				}
				result = new String(buff, "utf-8");
				System.out.println(result);
			}
			doneSignal.countDown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
