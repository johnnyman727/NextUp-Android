/* @deprecated since we are pulling all our ratings from yelp. */


package com.dotcom.nextup.classes;

public class RatingFloat2 {
	double rating;
	int n_ratings;

	public void update(double a_rating) {
		/* averages in the new rating */
		rating = (rating * n_ratings + a_rating) / (n_ratings + 1);
		n_ratings++;
	}

	public RatingFloat2() {
		rating = 0.5;  /* liberal: every rating starts out average */
		n_ratings = 0;
	}
	
	public double getRating() {
		return rating;
	}
	
	public int getNRatings() {
		return n_ratings;
	}
	
	public void setRating(double val) {
		rating = val;
	}
	
	public void setNRatings(int val) {
		n_ratings = val;
	}
}
