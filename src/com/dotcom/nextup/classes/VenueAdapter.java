package com.dotcom.nextup.classes;

import java.util.ArrayList;
import java.util.List;

import com.dotcom.nextup.classes.Venue;
import com.dotcom.nextup.classes.VenueView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class VenueAdapter extends BaseAdapter {

	private Context mContext;
	private List<Venue> mItems = new ArrayList<Venue>();
	
    public VenueAdapter(Context context) {
    	mContext = context;
    }
    
    public void addItem(Venue it) { mItems.add(it); }
    public Object getItem(int position) { return mItems.get(position); }
    public void setListItems(List<Venue> lit) { mItems = lit; }
    
    public int getCount() { return mItems.size(); }

    public boolean areAllItemsSelectable() { return false; }
    
    public boolean isSelectable(int position) {
        try{
            return mItems.get(position).isSelectable();
        }catch (IndexOutOfBoundsException aioobe){
            return false;
        }
    }
    
    public long getItemId(int position) {
    	return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	VenueView venvu;
    	if (convertView == null) {
    		venvu = new VenueView(mContext, mItems.get(position));
    	} else { // Reuse/Overwrite the View passed
    		// We are assuming(!) that it is castable!
    		venvu = (VenueView) convertView;
    		venvu.setText(mItems.get(position).getName());
    		venvu.setIcon(mItems.get(position).getDrawable());
    	}
    	return venvu;
    }
}
