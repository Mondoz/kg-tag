package com.hiekn.kg.service.tagging.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.hiekn.kg.service.tagging.mongo.MongoSingleton;
import com.hiekn.kg.service.tagging.util.ConstResource;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;

public class T1 {
	
	@Test
	public void t3() {
		DBCursor cursor = new MongoClient("47.104.13.92",19130).getDB("u89_graph_ea33277a")
				.getCollection("entity_id").find();
		int count = 0;
		while (cursor.hasNext()) {
			DBObject doc = cursor.next();
			System.out.println(doc.get("id") + "" + doc.get("name"));
		}
	}
	
	
	@Test
	public void t2() {
		String str = ConstResource.MAPFIELDS;
		Map<String,String> originMap = JSON.parseObject(str, Map.class);
		Map<String,String> map = new HashMap<String,String>();
		for (Entry<String,String> entry : originMap.entrySet()) {
			map.put(entry.getValue(), entry.getKey());
		}
	}

	@Test
	public void t1() {
		TreeNode root = new TreeNode(1);
		TreeNode rootTemp = root;

		System.out.println("origin root : " + root.val);
		rootTemp.val = 2;
		System.out.println("after change rootTemp'val, root.val : " + root.val);
		rootTemp = null;
		System.out.println("after change rootTemp, root : " + root.val);
		rootTemp = new TreeNode(3);
		System.out.println("after change rootTemp, root : " + root.val);
	}
	
	private List<Integer> getLeaves(TreeNode root, List<Integer> leaves) {
		if(root == null){
			return null;
		}
		if(root.left == null && root.right == null){
			leaves.add(root.val);
			// delete this node
			root = null;
//			root.val = -1;
		} else {
			getLeaves(root.left, leaves);
			getLeaves(root.right, leaves);
		}
		return leaves;
	}
}

class TreeNode {
	
	TreeNode(int i) {
		this.val = i;
	}
	
	public TreeNode left;
	public TreeNode right;
	
	public int val;
}
