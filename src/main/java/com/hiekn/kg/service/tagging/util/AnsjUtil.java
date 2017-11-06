package com.hiekn.kg.service.tagging.util;

import java.util.HashSet;
import java.util.Set;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;

public class AnsjUtil {
	
	public static void main(String[] args) {
//		DicLibrary.insert(DicLibrary.DEFAULT, "Artificial Intelligence");
//		Result parser = ToAnalysis.parse("this is a new Artificial Intelligence tech");
//		System.out.println(parser);
		Set<String> ansjWord = getAnsjWord("this is a new Artificial Intelligence tech", null);
		for (String string : ansjWord) {
			System.out.println(string);
		}
	}
	
	
	public static Set<String> getAnsjWord(String input,Set<String> word) {
		Set<String> set = new HashSet<String>();
		DicLibrary.clear(DicLibrary.DEFAULT);
		try {
			for (String string : word) {
				DicLibrary.insert(DicLibrary.DEFAULT, string);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Result parser = ToAnalysis.parse(input);
		for (Term term : parser) {
			set.add(term.getName().toLowerCase());
		}
		return set;
	}
}
