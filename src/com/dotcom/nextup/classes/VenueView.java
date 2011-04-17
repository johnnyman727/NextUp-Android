package com.dotcom.nextup.classes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/* http://www.anddev.org/novice-tutorials-f8/iconified-textlist-the-making-of-t97.html?sid=57c1096099cb666b386eb4ab65aba0c6 */

public class VenueView extends LinearLayout {
	private TextView mText;
	private ImageView mIcon;
	
	public VenueView(Context context, Venue ven) {
		super(context);
        /* First Icon and the Text to the right (horizontal),
         * not above and below (vertical) */
		this.setOrientation(HORIZONTAL);
		
		mIcon = new ImageView(context);
		mIcon.setImageDrawable(ven.getDrawable());
		// left, top, right, bottom
		mIcon.setPadding(5, 5, 5, 5); // 5px to the right
		/* At first, add the Icon to ourself
		 * (! we are extending LinearLayout) */
		addView(mIcon,  new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mText = new TextView(context);
		mText.setText(ven.getName());
		/* Now the text (after the icon) */
        addView(mText, new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
   
	public void setText(String words) {
          mText.setText(words);
	}
	public void setIcon(Drawable bullet) {
          mIcon.setImageDrawable(bullet);
	}
	
	public TextView getTextView() {
        return this.mText;
	}
	public ImageView getImageView() {
        return this.mIcon;
	}
}
