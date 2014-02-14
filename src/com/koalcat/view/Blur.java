package com.koalcat.view;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class Blur {
	
	private static final float BITMAP_SCALE_NOMAL = 0.8f;
	private static final float BLUR_RADIUS_NOMAL = 12.0f;
	
	private static final float BITMAP_SCALE_FAST = 0.5f;
	private static final float BLUR_RADIUS_FAST = 10.0f;
    
	ScriptIntrinsicBlur theIntrinsic;
	RenderScript rs;
    
	public Blur(Context context) {
		rs = RenderScript.create(context);
		theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
	}

	public Bitmap blur(Bitmap image, boolean fast) {
		
		float scale = BITMAP_SCALE_NOMAL;
		float radius = BLUR_RADIUS_NOMAL;
		
		if (fast) {
			scale = BITMAP_SCALE_FAST;
			radius = BLUR_RADIUS_FAST;
		}
		
		int width = Math.round(image.getWidth() * scale);
		int height = Math.round(image.getHeight() * scale);

		Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
		Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

		Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
		Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
		theIntrinsic.setRadius(radius);
		theIntrinsic.setInput(tmpIn);
		theIntrinsic.forEach(tmpOut);
		tmpOut.copyTo(outputBitmap);
		inputBitmap.recycle();
		
		return outputBitmap;
	}
    
	public void Destroy() {
		theIntrinsic.destroy();
		rs.destroy();
	}
}

