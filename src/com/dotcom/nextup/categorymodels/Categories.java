package com.dotcom.nextup.categorymodels;

import java.util.ArrayList;

public class Categories {
	ArrayList<Category> cats = null;
	
	public Categories() {
		cats = new ArrayList<Category>();
	}
	
	// has your normal arraylist methods
	public void add(Category cat) { cats.add(cat); }
	public int size() { return cats.size(); }
	public void remove(int i) {cats.remove(i); }
	public Category get(int i) { return cats.get(i); }
	public void addAll(ArrayList<Category> morecats) { 
		cats.addAll(morecats); 
	}
	public void addAll(int i, ArrayList<Category> morecats) {
		cats.addAll(i, morecats);
	}
	
	// also has special encoding to key value pairs
	// for convenient bundle passing between activities
	public ArrayList<String> getKeys() {
		//get arraylist of strings of all attributes of categories
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < cats.size(); i++) {
			String ii = Integer.toString(i);
			ArrayList<String> attrs = cats.get(i).getAttributes();
			for (int j = 0; j < attrs.size(); j++) {
				String jj = Integer.toString(j);
				String key = attrs.get(j) + "-" + ii + "-" + jj;
				res.add(key);
			}
		}
		return res;
	}
	public ArrayList<String> getValues() {
		//get arraylist of strings of all attributes of categories
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < cats.size(); i++) {
			String ii = Integer.toString(i);
			ArrayList<String> vals = cats.get(i).getAttributeValues();
			for (int j = 0; j < vals.size(); j++) {
				String jj = Integer.toString(j);
				String key = vals.get(j) + "-" + ii + "-" + jj;
				res.add(key);
			}
		}
		return res;
	}
}
