package com.dotcom.nextup.classes;

import java.util.ArrayList;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import com.dotcom.nextup.categorymodels.Category;
import com.google.android.maps.GeoPoint;

public class Venue implements Comparable<Venue>, Parcelable{
	// attributes are listed here in the order they are put in and taken out of a parcel
	private String name;
    private GeoPoint latlong;
    private String image_url;
    private String url;
    double distance; // distance in meters from current location
    private boolean mSelectable = true;
    double rating; // some venues have a five star rating, some don't
    private ArrayList<Category> categories;
    
    public Venue (String name, String url, String imageURL, GeoPoint latlong, double d, ArrayList<Category> cats) {
    	this.name = name;
    	this.url = url;
    	this.image_url = imageURL;
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = cats;
    }
    
    public Venue (String name, String url, GeoPoint latlong, double d) {
    	this.name = name;
    	this.url = url;
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = new ArrayList<Category>();
    }
    
    public Venue (String name, GeoPoint latlong, double d) {
    	this.name = name; 
    	this.latlong = latlong;
    	this.distance = d;
    	this.categories = new ArrayList<Category>();
    }
    
    public String toString() { return "Venue " + this.name;}
	public void setmSelectable(boolean mSelectable) { this.mSelectable = mSelectable; }
	public boolean ismSelectable() { return mSelectable; }
    
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    public void setRating(double rating) { this.rating = rating; }
    public double getRating() { return rating; }
    public GeoPoint getLatlong() { return latlong; }
    public void setGeoPoint(GeoPoint gp) {latlong = gp; }
    public String getURL() { return url; }
    public void setURL(String URL) { url = URL; }
    public String getImageURL() { return image_url; }
    public void setImageURL(String URL) { image_url = URL; }
    public double getDistance() { return distance; }
    public void setDistance(double d) { distance = d; }
	public void setCategories(ArrayList<Category> categories) { this.categories = categories; }
	public void addCategory (Category cat) { this.categories.add(cat); }
	public ArrayList<Category> getCategories() { return categories; }

	/* being Comparable */
	
    public int compareTo(Venue other) {
        if(this.name != null)
            return this.name.compareTo(other.getName());
        else
            throw new IllegalArgumentException();
    }
	
	/* being Parcelable */
	
	public Venue (Parcel in) {
		this.name = in.readString();
		double latE6 = in.readDouble();
		double lonE6 = in.readDouble();
		if (latE6 != -1 && lonE6 != -1) this.latlong = new GeoPoint((int)latE6, (int)lonE6);
		String imgurl = in.readString();
		if (!imgurl.equals("")) this.image_url = imgurl;
		String urll = in.readString();
		if (!urll.equals("")) this.url = urll;
		double d = in.readDouble();
		if (d != -1) this.distance = d;
		String isSelectable = in.readString();
		if (isSelectable.equals("true")) this.mSelectable = true;
		if (isSelectable.equals("false")) this.mSelectable = false;
		double r = in.readDouble();
		if (r != -1) this.rating = r;
		this.categories = new ArrayList<Category>();
		int c = in.readInt();
		if (c != -1) {
			for (int i = 0; i < c; i++) {
				try {
					Category cat = (Category) in.readParcelable(null);
					if (cat != null) this.categories.add(cat);
				} catch (BadParcelableException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		if ( name != null ) dest.writeString(name);
		else dest.writeString("");
		if (latlong != null) {
			dest.writeDouble(latlong.getLatitudeE6());
			dest.writeDouble(latlong.getLongitudeE6());
		} else {
			dest.writeDouble(-1);
			dest.writeDouble(-1);
		}
		if (image_url != null) dest.writeString(image_url);
		else dest.writeString("");
		if (url != null) dest.writeString(url);
		else dest.writeString("");
		if (!Double.isNaN(distance)) dest.writeDouble(distance);
		else dest.writeDouble(-1);
		dest.writeString(Boolean.toString(mSelectable));
		if (!Double.isNaN(rating)) dest.writeDouble(rating);
		else dest.writeDouble(-1);
		if (categories != null && categories.size() > 0) {
			dest.writeInt(categories.size());
			for (int i = 0; i < categories.size(); i++) {
				dest.writeParcelable(categories.get(i), i);
			}
		} else {
			dest.writeInt(-1);
		}
	}
	
	@SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Category createFromParcel(Parcel in)
        {
            return new Category(in);
        }

		@Override
		public Category[] newArray(int size) {
			return new Category[size];
		}
    };
    
	@Override
	public int describeContents() {
		return 0;
	}
    
}
