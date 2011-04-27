package com.dotcom.nextup.categorymodels;

import java.io.Serializable;
import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Comparator<Category>, Parcelable, Serializable {

	private static final long serialVersionUID = 3131950815409215218L;
	private String name;
	private Integer frequency;
	private Integer averageTime; //hour of day, 0 - 23
	
	public Category() {
		super();
		this.name = "Unknown Name";
	}
	public Category(String name) {
		super();
		this.name = name;
	}
	public Category(String nam, Integer freq, Integer time) {
		name = nam;
		frequency = freq;
		averageTime = time;
	}
	
	public String getName() {
		if (this.name != null)
			return this.name;
		return null;
		}
	public void setName(String name) {this.name = name;}
	public void setFrequency(Integer frequency) {this.frequency = frequency;}
	public Integer getFrequency() {return this.frequency;}
	public void setAverageTime(Integer averageTime) {this.averageTime = averageTime;}
	public Integer getAverageTime() {return averageTime;}
	
	@Override
	public int compare(Category c1, Category c2) {
		return c1.getFrequency() - c2.getFrequency();
	}

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(name);
		if (frequency == null)
			dest.writeInt(-1);
		else
			dest.writeInt(frequency);
		if (averageTime != null) 
			dest.writeInt(averageTime);
		else
			dest.writeInt(-1);
	}
	public Category(Parcel in) {
		this.name = in.readString();
		this.frequency = in.readInt();
		this.averageTime = in.readInt();
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
}
