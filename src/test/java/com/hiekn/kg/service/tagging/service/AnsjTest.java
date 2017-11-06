package com.hiekn.kg.service.tagging.service;

import org.ansj.domain.Result;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

public class AnsjTest {
	public static void main(String[] args) {
		DicLibrary.insert(DicLibrary.DEFAULT, "年洁");
		DicLibrary.insert(DicLibrary.DEFAULT, "有多");
		DicLibrary.insert(DicLibrary.DEFAULT, "aron swartz");
		DicLibrary.insert(DicLibrary.DEFAULT, "摩拜单车");
		String str = "高储能密度电介质材料" ;
		Result parse = ToAnalysis.parse(str);
		System.out.println(parse);
	}
}
