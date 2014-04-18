package com.koalcat.blurdemo;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;

public class AppInfo {
	
	public static final int NO_ID = -1;
	public String title;
	public String title_py;
	public Intent intent;
	public Drawable icon;
	public Bitmap bitmap;
	public ComponentName mComponentName;
	public ImageButton mImageButton;
    
	public final void setComponentName(ComponentName className) {
		this.mComponentName = className;
	}
    
	public final void setActivity(ComponentName className, int launchFlags) {
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(className);
		intent.setFlags(launchFlags);
	}
}
