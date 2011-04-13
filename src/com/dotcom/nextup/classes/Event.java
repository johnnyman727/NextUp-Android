package com.dotcom.nextup.classes;
//package com.dotcom.nextup;
//
//public class Event {
//	private Category category;
//	private String name;
//	private Location location;
//	private Qualities qualities;
//	private Hours hours;
//	
//	public float compatibilityWith(Prefs user_prefs) {
//		/* returns float in range 0 to 1, 1 is best 
//		 * combines travel_time with compatibility_with_user_prefs 
//		 */
//		
//		/* prefs_rank from 0 to 1 */
//		float prefs_rank = qualities.compatibilityWith(user_prefs);
//		/* travel time in minutes */
//		float travel_time = location.travelTimeTo(user_loc, user_prefs.getTransport());
//		
//		float max_travel_time = 60; /* no one wants to travel for longer than this */
//		
//		if (travel_time > max_travel_time) {
//			return 0;
//		}
//		
//		/* totally reasonable to travel 30 min or less */
//		if (travel_time < max_travel_time/2) { 
//			return prefs_rank;
//		}
//
//		/* when max_travel_time/2 < travel_time < max_travel_time,
//		 * this function scales the prefs_rank such that
//		 * at travel_time = max_travel_time,   prefs_rank is halved
//		 * at travel_time = max_travel_time/2, prefs_rank is unchanged
//		 * in between, prefs_rank is scaled linearly
//		 */
//		return prefs_rank*(1 - ((travel_time - max_travel_time/2)/max_travel_time));
//	}
//	
//	public Event(String given_name, Location given_location) {
//		name = given_name;
//		location = given_location;
//		qualities = Qualities();
//		category = Category();
//		hours = Hours();
//	}
//	
//	public String getName() {
//		return name;
//	}
//	
//	public Category getCategory() {
//		return category;
//	}
//	
//	public Location getLocation() {
//		return location;
//	}
//	
//	public Qualities getQualities() {
//		return qualities;
//	}
//	
//	public Hours getHours() {
//		return hours;
//	}
//	
//	public void setName(String given_name) {
//		name = given_name;
//	}
//	
//	public void setCategory(Category given_category) {
//		category = given_category;
//	}
//	
//	public void setLocation(Location given_location) {
//		location = given_location;
//	}
//	
//	public void setQualities(Qualities given_qualities) {
//		qualities = given_qualities;
//	}
//	
//	public void setHours(Hours given_hours) {
//		hours = given_hours;
//	}
//}
