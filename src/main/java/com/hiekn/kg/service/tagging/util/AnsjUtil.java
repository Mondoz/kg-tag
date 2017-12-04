package com.hiekn.kg.service.tagging.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiekn.kg.service.tagging.bean.TaggingItem;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.elasticsearch.common.collect.Sets;

public class AnsjUtil {

	private static Set<String> set = Sets.newHashSet();

	
	public static void main(String[] args) {
//		DicLibrary.insert(DicLibrary.DEFAULT, "Artificial Intelligence");
//		Result parser = ToAnalysis.parse("this is a new Artificial Intelligence tech");
//		System.out.println(parser);
		Set<String> ansjWord = getAnsjWord("this is a new Artificial Intelligence tech", Sets.newHashSet("ai"));
		for (String string : ansjWord) {
			System.out.println(string);
		}
	}

	public static Set<String> getAnsjWord(String input) {
		Set<String> wordSet = new HashSet<String>();
		Result parser = ToAnalysis.parse(input);
		for (Term term : parser) {
			wordSet.add(term.getName().toLowerCase());
		}
		return wordSet;
	}

	public static void init(String kgName, Set<String> wordSet) {
		wordSet.forEach(word -> DicLibrary.insert(DicLibrary.DEFAULT, word));
	}

	public static void init(String kgName) {
		List<TaggingItem> list = SemanticSegUtil.kgWordMap.get(kgName);
		for (TaggingItem entityBean : list) {
			DicLibrary.insert(DicLibrary.DEFAULT, entityBean.getName());
		}
	}

	
	public static Set<String> getAnsjWord(String input,Set<String> word) {
		Set<String> set = new HashSet<String>();
		DicLibrary.clear(DicLibrary.DEFAULT);
		try {
			if (word.size() > 0) {
				for (String string : word) {
//					DicLibrary.insert(DicLibrary.DEFAULT, string);
					DicLibrary.insert("u89", string);
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
		Result parser = ToAnalysis.parse(input);
		for (Term term : parser) {
			set.add(term.getName().toLowerCase());
		}
		return set;
	}

	public static Set<String> getAnsjWord(String input,Set<String> word,boolean use) {
		return word;
	}
}
