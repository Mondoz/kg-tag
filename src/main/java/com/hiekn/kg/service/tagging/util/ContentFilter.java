package com.hiekn.kg.service.tagging.util;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ContentFilter {
	
	public static void main(String[] args) {
//		MongoClient client = new MongoClient();
//		MongoCursor<Document> cursor =  client.getDatabase("data").getCollection("news_data").find(new Document("_id","958c5f81pcc04p4744p9330pee0335dc17ec")).iterator();
//		try {
//			while (cursor.hasNext()) {
//				Document doc = cursor.next();
//				String content = doc.getString("content");
//				List<String> list = getSplitContent(content);
//				for (int i = 0; i < list.size(); i++) {
//					System.out.println(i + "行	" + list.get(i));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		client.close();
	}
	
	public static List<String> getSplitContent(String content) {
		List<String> splitContentList = new ArrayList<String>();
		if (content == null) content = "";
		try {
			Elements pElements = Jsoup.parse(content).getElementsByTag("p");
			for (Element element : pElements) {
				String elementText = element.text().trim();
				Element imgElement = element.getElementsByTag("img").first();
				if (imgElement != null) {
//				String imgHref = imgElement.attr("src");
					splitContentList.add(imgElement.toString());
					if (!elementText.equals("")) {
						splitContentList.add(elementText);
					}
				} 
				if (!elementText.equals("")) {
					splitContentList.add(elementText);
				}
			}
		} catch (Exception e) {
			
		}
		return splitContentList;
	}
}
