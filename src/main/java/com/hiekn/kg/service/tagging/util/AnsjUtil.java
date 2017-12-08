package com.hiekn.kg.service.tagging.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.hiekn.kg.service.tagging.bean.TaggingItem;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.library.DicLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.log4j.Logger;
import org.elasticsearch.common.collect.Sets;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;

public class AnsjUtil {

	static Logger log = Logger.getLogger(AnsjUtil.class);

	private static Map<String,Set<String>> kgWordMap = Maps.newHashMap();

	
	public static void main(String[] args) {
//		DicLibrary.insert(DicLibrary.DEFAULT, "Artificial Intelligence");
//		Result parser = ToAnalysis.parse("this is a new Artificial Intelligence tech");
//		System.out.println(parser);
	}

	public static Set<String> getAnsjWord(String input, Forest forest) {
		Set<String> wordSet = new HashSet<String>();
		Result parser = ToAnalysis.parse(input, forest);
		for (Term term : parser) {
			wordSet.add(term.getName().toLowerCase());
		}
		return wordSet;
	}

	public static Set<String> getAnsjWord(String input,String kgName, Set<String> set) {
		Set<String> wordSet = new HashSet<String>();
		if (!kgWordMap.containsKey(kgName)) {
			init(kgName,set);
		}
		Result parser = ToAnalysis.parse(input);
		for (Term term : parser) {
			wordSet.add(term.getName().toLowerCase());
		}
		return wordSet;
	}

	public static void init(String kgName, Set<String> wordSet) {
		wordSet.forEach(word -> {
			DicLibrary.insert(DicLibrary.DEFAULT, word);
		});
		kgWordMap.put(kgName, wordSet);
		log.info("init" + kgName);
	}
}
