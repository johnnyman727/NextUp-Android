package com.dotcom.nextup.classes;
/* deprecated.  just use what we can pull from yelp.

package com.dotcom.nextup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.prefs.Preferences;

public class Qualities {
	int under18; /* 1 for True (under 18 allowed), 0 for False, -1 for unspecified */
/*	int under21;
	HashMap<String, RatingFloat> rating_floats = new HashMap<String, RatingFloat>();
	PriceRange price_range;
	
	public double compatibilityWith(Preferences user_prefs) {
		/* absolutely required conditions */
/*		if ( user_prefs.getUnder18() == 1 && under18 != 1 ) return 0;
		if ( user_prefs.getUnder18() == 0 && under18 != 0 ) return 0;
		if ( user_prefs.getUnder21() == 1 && under21 != 1 ) return 0;
		if ( user_prefs.getUnder21() == 0 && under21 != 0 ) return 0;
		
		double rank = 0;     /* will normalize rank by                       */
/*		double max_rank = 0; /* max_rank, the highest rank could possibly be */
/*		double tempval;

		for ( RatingFloat temp : rating_floats.values() ) {
			tempval = temp.getRating();
			rank += tempval;
			max_rank++;
		}
		
		rank = rank / max_rank;
		return rank;
	}
	
	public Qualities() {
		under18 = -1;
		under21 = -1;
		rating_floats.put("popularity", new RatingFloat());
		rating_floats.put("vegetarian", new RatingFloat());
		rating_floats.put("vegan", new RatingFloat());
		rating_floats.put("lactose_free", new RatingFloat());
		rating_floats.put("gluten_free", new RatingFloat());
		rating_floats.put("wheelchair", new RatingFloat());
		rating_floats.put("deaf", new RatingFloat());
		price_range = null;
	}
	
	public void setUnder18(int val) {
		under18 = val;
	}
	
	public void setUnder21(int val) {
		under21 = val;
	}
	
	public void setRatingFloat(String name, RatingFloat val) {
		rating_floats.put(name, val);
	}

	public void setPriceRange(PriceRange val) {
		price_range = val;
	}
	
	public int getUnder18() {
		return under18;
	}
	
	public int getUnder21() {
		return under21;
	}
	
	public RatingFloat getRatingFloat(String name) {
		return rating_floats.get(name);
	}
	
	public PriceRange getPriceRange() {
		return price_range;
	}
}
*/
