package com.dotcom.nextup.categorymodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;


public class CategoryHistogram implements Comparator<Category>{
	
	private HashMap<Category, ArrayList<Category>> map = 
		new HashMap<Category, ArrayList<Category>>();
	//Amount of time which defines one event is "after" another (in seconds (4 hrs))
	private final Integer followTime = 14400;
	
	public CategoryHistogram() {
		super();
	}
	public Set<Category> getPrefixes() {
		return map.keySet();
	}
	
	public Boolean containsPrefix(Category prefix) {
		return map.containsKey(prefix);
	}

	public void createInitialHistogram(ArrayList<CheckIn> checkins) {
		int last = checkins.size() - 1;
		int index;
		for (CheckIn checkin : checkins) {
			index = checkins.indexOf(checkin);
			if (index != last) {
				CheckIn otherCheckin = checkins.get(index + 1);
				if ((checkin.getTime() - otherCheckin.getTime()) <= this.followTime) {
					for (Category category : checkin.getCategories()) {
						for (Category otherCat : otherCheckin.getCategories()) {
							addToCategoryHistogram(category, otherCat);
						}
					}
				}
			}
		}
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
		
		if (map.keySet().size() == 0) {
			ArrayList<Category> suffixList = new ArrayList<Category>();
			suffixList.add(suffix);
			map.put(prefix, suffixList);
			return 1;
		}
		for (Category category : map.keySet()) {
			if (category.getName().equals(prefix.getName())) {
				ArrayList<Category> suffixes = getAllSuffixes(prefix);
				Category foundSuff = suffixes.get(suffixes.indexOf(suffix));
				foundSuff.setFrequency(foundSuff.getFrequency() + 1);
				return 0;
			} 
			else {
				ArrayList<Category> suffixList = new ArrayList<Category>();
				suffixList.add(suffix);
				map.put(prefix, suffixList);
				return 1;
			}
		}
		return -1;
	}
	

	@Override
	public int compare(Category object1, Category object2) {
		return object1.getFrequency() - object2.getFrequency();
	}
	
	public void storeHistogram() {
			
	}
	
	public void containsHistogram() {
	}
}
