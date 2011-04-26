package com.dotcom.nextup.categorymodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;


public class CategoryHistogram implements Comparator<Category>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1408459070568084039L;
	private HashMap<Category, ArrayList<Category>> map = 
		new HashMap<Category, ArrayList<Category>>();
	//Amount of time which defines one event is "after" another (in seconds (4 hrs))
	public static final Integer followTime = 3;
	
	public CategoryHistogram() {
		super();
	}
	
	public CategoryHistogram(HashMap<Category, ArrayList<Category>> map) {
		this();
		this.map = map;
	}
	public Set<Category> getPrefixes() {
		return map.keySet();
	}
	
	public Boolean containsPrefix(Category prefix) {
		return map.containsKey(prefix);
	}

	public static Boolean areSerial(CheckIn c1, CheckIn c2) {
		return ((c1.getCreatedAt() - c2.getCreatedAt()) <= followTime);
	}
	public void addCheckInsToHistogram(ArrayList<CheckIn> checkins) {
		int last = checkins.size() - 1;
		int index;
		for (CheckIn checkin : checkins) {
			index = checkins.indexOf(checkin);
			if (index != last) {
				CheckIn otherCheckin = checkins.get(index + 1);
				if (areSerial(checkin, otherCheckin)) {
					for (Category category : checkin.getCategories()) {
						for (Category otherCat : otherCheckin.getCategories()) {
							addToCategoryHistogram(category, otherCat);
						}
					}
				}
			}
		}
	}
	
	public void addToHistogram(ArrayList<CheckIn> checkins) {
		
	}
	public Boolean containsSuffix(Category suffix) {
		for (Category prefix: map.keySet()) {
			for (Category suff: map.get(prefix)) {
				if (suff.equals(suffix)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public ArrayList<Category> getAllSuffixes(Category prefix) {
		ArrayList<Category> result = new ArrayList<Category>();
		if (containsPrefix(prefix))
			result = map.get(prefix);
		return result;
	}
	
	public ArrayList<Category> getTopThreeSuffixes(Category prefix) {
		if (prefix == null) {
			return null;
		}
		ArrayList<Category> topThree = new ArrayList<Category>();
		sortCategories(map.get(prefix));
		for (int i = 0; i < 3; i++) {
			topThree.add(map.get(prefix).get(i));
		}
		return topThree;
	}
	
	public void sortCategories(ArrayList<Category> suffixes) {
		 Collections.sort(suffixes, new Comparator<Category>(){

				@Override
				public int compare(Category object1, Category object2) {
					return object1.getFrequency() - object2.getFrequency();
				}
	 
	        });
	}

	public Category getRandomSuffix(Category prefix) {
		Random numGen = new Random();
		ArrayList<Category> suffixes = getAllSuffixes(prefix);
		return suffixes.get(numGen.nextInt(suffixes.size()));
		
	}
	
	public int addToCategoryHistogram(Category prefix, Category suffix) {
		Boolean prefixExists = false;
		Boolean suffixExists = false;
		int index = -1;
		
		if (prefix == null || suffix == null)
			return -1;
		if (map.keySet().size() == 0) {
			ArrayList<Category> suffixList = new ArrayList<Category>();
			suffixList.add(suffix);
			map.put(prefix, suffixList);
			return 1;
		}
		for (Category category : map.keySet()) {
			if (category.getName().equals(prefix.getName()))
				prefixExists = true;
		}
		if (prefixExists) {
			ArrayList<Category> suffixes = getAllSuffixes(prefix);
			if ((index = suffixes.indexOf(suffix)) != -1)
				suffixExists = true;
			if (suffixExists) {
				Category foundSuff = suffixes.get(index);
				foundSuff.setFrequency(foundSuff.getFrequency() + 1);
				return 0;
				
			} else {
				suffix.setFrequency(1);
				suffixes.add(suffix);
				return 0;
			}
		}else {
				ArrayList<Category> suffixList = new ArrayList<Category>();
				suffixList.add(suffix);
				map.put(prefix, suffixList);
				return 1;
			}
	}
	

	@Override
	public int compare(Category object1, Category object2) {
		return object1.getFrequency() - object2.getFrequency();
	}
	
	public HashMap<Category, ArrayList<Category>> getMap() {
		return this.map;
	}
	public void setMap(HashMap<Category, ArrayList<Category>> map) {
		this.map = map;
	}
}
