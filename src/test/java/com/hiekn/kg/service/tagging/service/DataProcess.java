package com.hiekn.kg.service.tagging.service;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

public class DataProcess {

    public static void main(String[] args) {
        eleData();
    }


    public static void eleData() {
        MongoClient client = new MongoClient("10.10.20.14",19130);
        MongoDatabase db = client.getDatabase("annotation_source");
//        MongoCollection<Document> sourceCol = db.getCollection("patent");
//        MongoCollection<Document> targetCol = db.getCollection("patent_ele");
        MongoCollection<Document> sourceCol = db.getCollection("paper");
        MongoCollection<Document> targetCol = db.getCollection("paper_ele");
//        Document searchDoc = new Document("applicants.name.original",new Document(
//                "$in", Lists.newArrayList("华东中心实验所","华东电力试验研究院","华东电力试验研究院有限公司","国网上海市电力公司电力科学研究院")
//        ));
        Document searchDoc = new Document("authors.organization.name",new Document(
                "$in", Lists.newArrayList("华东中心实验所","华东电力试验研究院","华东电力试验研究院有限公司","国网上海市电力公司电力科学研究院")
        ));
        MongoCursor<Document> cursor = sourceCol.find(searchDoc).iterator();
        List<Document> docList = Lists.newArrayList();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                System.out.println(doc.get("title"));
                docList.add(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        targetCol.insertMany(docList);
        client.close();
    }
}
