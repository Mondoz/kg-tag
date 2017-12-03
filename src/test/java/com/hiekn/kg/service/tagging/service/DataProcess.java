package com.hiekn.kg.service.tagging.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hiekn.kg.service.tagging.util.BufferedReaderUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataProcess {

    public static void main(String[] args) {
//        eleData();
    }

    @Test
    public void t1() {
        try {
            FileWriter fw = new FileWriter("data/aaaaaaaa.txt");
            File[] file = new File("data/patent_ori").listFiles();
            Set<String> set = new HashSet<String>();
            for (File file1 : file) {
                BufferedReader br = BufferedReaderUtil.getBuffer(file1.getPath());
                String input = "";
                while ((input = br.readLine())!=null) {
                    try {
//                        input = input.replaceAll("\\\\v","");
                        JSONObject obj = JSONObject.parseObject(input);
                        String id = obj.getString("_id");
                        set.add(id);
                    } catch (Exception e) {
                        System.out.println(input);
                        e.printStackTrace();
                    }
                }
                System.out.println(file1.getName() + " has finish");
            }

            BufferedReader br = BufferedReaderUtil.getBuffer("data/patent_with_id.txt");
            String input = "";
            while ((input = br.readLine())!=null) {
                try {
                    JSONObject obj = JSONObject.parseObject(input);
                    String id = obj.getString("_id");
                    if (!set.contains(id)) {
                        fw.write(input + "\r\n");
                        fw.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
