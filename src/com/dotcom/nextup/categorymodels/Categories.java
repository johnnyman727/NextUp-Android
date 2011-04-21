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
	
	/* ENCODING INTO KEY VALUE PAIRS */
	// also has special encoding to key value pairs
	// for convenient bundle passing between activities
	// cat-1-name
	// cat-1-frequency
	// cat-1-averageTime
	// cat-2-name ...
	public ArrayList<String> getKeys() {
		//get arraylist of strings of all attributes of categories
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < cats.size(); i++) {
			String ii = Integer.toString(i);
			ArrayList<String> attrs = cats.get(i).getAttributes();
			for (int j = 0; j < attrs.size(); j++) {
				res.add(encodeKey(ii, attrs.get(j)));
			}
		}
		return res;
	}
	public ArrayList<String> getValues() {
		// get arraylist of strings of all attributes of categories
		// cat-1-name-Cafe
		// cat-1-frequency-2
		// cat-1-averageTime-11
		// cat-2-name-Bar ...
		ArrayList<String> res = new ArrayList<String>();
		for (int i = 0; i < cats.size(); i++) {
			String ii = Integer.toString(i);
			ArrayList<String> vals = cats.get(i).getAttributeValues();
			for (int j = 0; j < vals.size(); j++) {
				String desc = this.crappyConverter(j);
				res.add(encodeValue(ii, desc, vals.get(i)));
			}
		}
		return res;
	}
	private String crappyConverter(int j) {
		/* depends completely on Category.getAttributes()
		 * because I'm too lazy to do real data abstraction
		 */
		if (j == 0) return "name";
		if (j == 1) return "frequency";
		if (j == 2) return "averageTime";
		else return "";
	}

	
	/* DECODING FROM KEY VALUE PAIRS BACK INTO CATEGORIES */
	public Categories decode(ArrayList<String> keys, ArrayList<String> values) {
		Categories clowder = new Categories(); // a herd of horses, a clowder of cats
		
		return clowder;
	}
	
	/* THE ACTUAL ENCODE / DECODE OF CATEGORY / STRING */
	private String encodeKey(String index, String attr) {
		return "cat-" + index + "-" + attr;
	}
	
	private String encodeValue(String index, String desc, String val) {
		return "cat-" + index + "-" + desc + "-" + val;
	}
}
