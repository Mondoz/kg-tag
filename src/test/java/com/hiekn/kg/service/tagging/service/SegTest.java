package com.hiekn.kg.service.tagging.service;

import java.io.BufferedReader;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.hiekn.kg.service.tagging.util.BufferedReaderUtil;
import com.hiekn.kg.service.tagging.util.TaggerUtil;

public class SegTest {
	public static void main(String[] args) {
		try {
			BufferedReader br = BufferedReaderUtil.getBuffer("data/kw.txt");
			BufferedReader dbr = BufferedReaderUtil.getBuffer("data/patent_with_id.txt");
			String input = "";
			Set<String> set = new HashSet<>();
			while ((input = br.readLine()) != null) {
				set.add(input);
			}
			System.out.println("finish" + set.size());
			while ((input = dbr.readLine())!=null) {
				JSONObject obj = JSONObject.parseObject(input);
				String st = obj.getString("title") + obj.getString("abstact");
				long t1 = System.currentTimeMillis();
				for (String s : set) {
					if (!s.equals("")) {
						getCount2(st, s);
					}
				}
				System.out.println(System.currentTimeMillis() - t1);
			}
		} catch (Exception e) {
		    e.printStackTrace();
		}
//		Set<String> set = new HashSet<String>();
//		set.add("华联股份");
//		set.add("华联");
//		String s = "在测试中，华联中最好的股份是华联股份";
//		System.out.println(s.indexOf("华联", s.indexOf("华联") + 1));
//		Map<String,Integer> wordFrequencyMap = new HashMap<String,Integer>();
//		for (String string : set) {
//			wordFrequencyMap.put(string, getCount(s, string));
//		}
//		System.out.println("finish");
	}
	
	private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
 
    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isEnglishCharactor(char i) {
		boolean is = true;
		int ii = i;
		System.out.println(ii);
		if (!((i > 64 && i < 91) || (i > 96 && i < 123))) {
			is = false;
		}
		return is;
	}
	
	
	private static int getCount2(String input, String word) {
		int count = 0;
		int index = 0;
		if (input.contains(word)) return 1;
		return count;
	}
	
	public static int getCount(String input, String word) {
		int count = 0;
		int index = 0;
		while ((index = input.indexOf(word,index)) != -1) {
			if (TaggerUtil.isChinese(word)) {
				index = index + word.length();
				count++;
			} else {
				if (index > 0) {
					if (index + word.length() >= input.length()) {
						if (input.charAt(index-1) == 32) {
							index = index + word.length();
							count++;
						}
					} else {
						if (input.charAt(index-1) == 32 && input.charAt(index+word.length()) == 32) {
							index = index + word.length();
							count++;
						} else {
							index = index + word.length();
						}
					}
				} else {
					if (input.charAt(index+word.length() -1) == 32) {
						index = index + word.length();
						count++;
					} else {
						index = index + word.length();
					}
				}
			}
		}
		return count;
	}
}
