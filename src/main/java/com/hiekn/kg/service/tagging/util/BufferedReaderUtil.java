package com.hiekn.kg.service.tagging.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class BufferedReaderUtil {
	public static BufferedReader getBuffer(String filePath) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return br;
	}
}
