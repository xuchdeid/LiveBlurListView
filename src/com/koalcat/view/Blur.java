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
import android.support.v4.util.LruCache;

public class Blur {
	
	private static final boolean USE_NOMAL = false;
	
	private static final float BITMAP_SCALE_NOMAL = 0.8f;
	private static final float BLUR_RADIUS_NOMAL = 12.0f;
	
	private static final float BITMAP_SCALE_FAST = 0.5f;
	private static final float BLUR_RADIUS_FAST = 10.0f;
    
	ScriptIntrinsicBlur theIntrinsic;
	RenderScript rs;
	
	private LruCache<String, Bitmap> mMemoryCache; 
    
	public Blur(Context context) {
		rs = RenderScript.create(context);
		theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
		
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
	    final int cacheSize = maxMemory / 8;  
	  
	    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
	        @Override  
	        protected int sizeOf(String key, Bitmap bitmap) {  
	            return bitmap.getByteCount() / 1024;  
	        }  
	    };
	}

	public Bitmap blur(Bitmap image, boolean fast) {
		
		if (USE_NOMAL) {
			return Blur_Nomal(image, fast);
		}
		
		float scale = BITMAP_SCALE_NOMAL;
		float radius = BLUR_RADIUS_NOMAL;
		
		if (fast) {
			scale = BITMAP_SCALE_FAST;
			radius = BLUR_RADIUS_FAST;
		}
		
		int width = Math.round(image.getWidth() * scale);
		int height = Math.round(image.getHeight() * scale);

		Bitmap bitmap = Bitmap.createScaledBitmap(image, width, height, true);
		Bitmap outputBitmap = getBitmapFromMemCache("" + width + height);
		if (outputBitmap == null) {
			outputBitmap = bitmap.copy(bitmap.getConfig(), true);
			addBitmapToMemoryCache("" + width + height, outputBitmap);
		}
		//Bitmap outputBitmap = bitmap.copy(bitmap.getConfig(), true);

		Allocation tmpIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
		
		theIntrinsic.setRadius(radius);
		theIntrinsic.setInput(tmpIn);
		theIntrinsic.forEach(tmpOut);
		tmpOut.copyTo(outputBitmap);
		bitmap.recycle();
		tmpIn.destroy();
		tmpOut.destroy();
		
		return outputBitmap;
	}
	
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
	    if (getBitmapFromMemCache(key) == null) {  
	        mMemoryCache.put(key, bitmap);  
	    }  
	}  
	  
	private Bitmap getBitmapFromMemCache(String key) {  
	    return mMemoryCache.get(key);  
	}
    
	public void Destroy() {
		theIntrinsic.destroy();
		rs.destroy();
	}
	
	public Bitmap Blur_Nomal(Bitmap image, boolean fast) {
		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		float scale = BITMAP_SCALE_NOMAL;
		int radius = (int) BLUR_RADIUS_NOMAL;
		
		if (fast) {
			scale = BITMAP_SCALE_FAST;
			radius = (int) BLUR_RADIUS_FAST;
		}
		
		Bitmap bitmap = image.copy(image.getConfig(), true);
		//int width = Math.round(image.getWidth() * scale);
		//int height = Math.round(image.getHeight() * scale);

		//Bitmap bitmap = Bitmap.createScaledBitmap(image, width, height, false);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];

		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}
}

