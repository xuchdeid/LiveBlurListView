package com.koalcat.blurdemo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.util.StateSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.app.Activity;
import android.content.res.Resources;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

public class Utilities {
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;

    private static final Paint sPaint = new Paint();
    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }

    /**
     * Returns a Drawable representing the thumbnail of the specified Drawable.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * This method is not thread-safe and should be invoked on the UI thread only.
     *
     * @param icon The icon to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified icon or the icon itself if the
     *         thumbnail could not be created. 
     */
    public static Drawable createIconThumbnail(Drawable icon, Context context) {
        if (sIconWidth == -1) {
            final Resources resources = context.getResources();
            sIconWidth = sIconHeight = (int) resources.getDimension(android.R.dimen.app_icon_size);
        }

        int width = sIconWidth;
        int height = sIconHeight;

        float scale = 1.0f;
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {
            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
        }
        int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();

        if (width > 0 && height > 0) {
            if (width < iconWidth || height < iconHeight || scale != 1.0f) {
                final float ratio = (float) iconWidth / iconHeight;

                if (iconWidth > iconHeight) {
                    height = (int) (width / ratio);
                } else if (iconHeight > iconWidth) {
                    width = (int) (height * ratio);
                }

                final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ?
                            Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb);
                // Copy the old bounds to restore them later
                // If we were to do oldBounds = icon.getBounds(),
                // the call to setBounds() that follows would
                // change the same instance and we would lose the
                // old bounds
                sOldBounds.set(icon.getBounds());
                final int x = (sIconWidth - width) / 2;
                final int y = (sIconHeight - height) / 2;
                icon.setBounds(x, y, x + width, y + height);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            } else if (iconWidth < width && iconHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb);
                sOldBounds.set(icon.getBounds());
                final int x = (width - iconWidth) / 2;
                final int y = (height - iconHeight) / 2;
                icon.setBounds(x, y, x + iconWidth, y + iconHeight);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            }
        }
        
        StateListDrawable mDrawable = new StateListDrawable();
		Bitmap mBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		icon.setAlpha(0x99);
		Canvas canvas = new Canvas(mBitmap);
		icon.setBounds(new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
		icon.draw(canvas);
        mDrawable.addState(new int[] { android.R.attr.state_pressed }, new BitmapDrawable(context.getResources(), mBitmap));
        
        icon.setAlpha(0xff);
        mDrawable.addState(StateSet.WILD_CARD, icon);
        return mDrawable;
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * This method is not thread-safe and should be invoked on the UI thread only.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    public static Bitmap createBitmapThumbnail(Bitmap bitmap, Context context) {
    	
    	if (bitmap == null) {
    		return null;
    	}
    	
        if (sIconWidth == -1) {
            final Resources resources = context.getResources();
            sIconWidth = sIconHeight = (int) resources.getDimension(
                    android.R.dimen.app_icon_size);
        }

        int width = sIconWidth;
        int height = sIconHeight;

        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        if (width > 0 && height > 0 && (width < bitmapWidth || height < bitmapHeight)) {
            final float ratio = (float) bitmapWidth / bitmapHeight;

            if (bitmapWidth > bitmapHeight) {
                height = (int) (width / ratio);
            } else if (bitmapHeight > bitmapWidth) {
                width = (int) (height * ratio);
            }

            final Bitmap.Config c = (width == sIconWidth && height == sIconHeight) ?
                    bitmap.getConfig() : Bitmap.Config.ARGB_8888;
            final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
            final Canvas canvas = sCanvas;
            final Paint paint = sPaint;
            canvas.setBitmap(thumb);
            paint.setDither(false);
            paint.setFilterBitmap(true);
            sBounds.set((sIconWidth - width) / 2, (sIconHeight - height) / 2, width, height);
            sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
            canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
            return thumb;
        }

        return bitmap;
    }
    
    @SuppressWarnings("deprecation")
	public static StateListDrawable CreateAddDrawable(Context context) {
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
    	float dip = ((float)dm.densityDpi / 160.0f);
		int dock_height = (int) (context.getResources().getDimension(android.R.dimen.app_icon_size) * dip);
		StateListDrawable mDrawable = new StateListDrawable();
		
		Bitmap mBitmap = Bitmap.createBitmap((int)dock_height, (int)dock_height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		Drawable d = context.getResources().getDrawable(R.drawable.bg_squ_click);
        d.setBounds(new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        d.draw(canvas);
        mDrawable.addState(new int[] { android.R.attr.state_pressed }, new BitmapDrawable(mBitmap));
		
    	mBitmap = Bitmap.createBitmap((int)dock_height, (int)dock_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmap);
        d = context.getResources().getDrawable(R.drawable.bg_squ_normal);
        d.setBounds(new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        d.draw(canvas);
        Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		//paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4 * dip);
        float startX = mBitmap.getWidth() / 4;
		float startY = mBitmap.getHeight() / 4;
		float stopX = startX * 3;
		float stopY = startY * 3;
		canvas.drawLine(startX , startY * 2, stopX, startY * 2, paint);
		canvas.drawLine(startX * 2 , startY, startX * 2, stopY, paint);
        mDrawable.addState(StateSet.WILD_CARD, new BitmapDrawable(mBitmap));
        
        return mDrawable;
    }
    
    @SuppressWarnings("deprecation")
	public static StateListDrawable CreateSettingAddDrawable(Context context) {
    	DisplayMetrics dm = context.getResources().getDisplayMetrics();
    	float dip = ((float)dm.densityDpi / 160.0f);
		int dock_height = (int) (context.getResources().getDimension(android.R.dimen.app_icon_size) * dip);
		StateListDrawable mDrawable = new StateListDrawable();
		
		Bitmap mBitmap = Bitmap.createBitmap((int)dock_height, (int)dock_height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		Drawable d = context.getResources().getDrawable(R.drawable.bg_squ_click_black);
        d.setBounds(new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        d.draw(canvas);
        mDrawable.addState(new int[] { android.R.attr.state_pressed }, new BitmapDrawable(mBitmap));
		
    	mBitmap = Bitmap.createBitmap((int)dock_height, (int)dock_height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(mBitmap);
        d = context.getResources().getDrawable(R.drawable.bg_squ_normal_black);
        d.setBounds(new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()));
        d.draw(canvas);
        Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		//paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4 * dip);
        float startX = mBitmap.getWidth() / 4;
		float startY = mBitmap.getHeight() / 4;
		float stopX = startX * 3;
		float stopY = startY * 3;
		canvas.drawLine(startX , startY * 2, stopX, startY * 2, paint);
		canvas.drawLine(startX * 2 , startY, startX * 2, stopY, paint);
        mDrawable.addState(StateSet.WILD_CARD, new BitmapDrawable(mBitmap));
        
        return mDrawable;
    }
    
    public static boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }
    
    public static int getScreenBrightness(Activity activity) {
        int nowBrightnessValue = 255 / 2;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(
                    resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    
    public static void setBrightness(Activity activity, int brightness) {
    	if (brightness < 10) {
    		brightness = 10;
    	}
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);     
    }
    
    public static void saveBrightness(ContentResolver resolver, int brightness) {
        Uri uri = android.provider.Settings.System
                .getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness",
                brightness);
        resolver.notifyChange(uri, null);
    }
    
    public static void startAutoBrightness(Activity activity) {
    	Uri uri = android.provider.Settings.System
                .getUriFor("screen_brightness");
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        activity.getContentResolver().notifyChange(uri, null);
    }
    
    public static void stopAutoBrightness(Activity activity) {
    	Uri uri = android.provider.Settings.System
                .getUriFor("screen_brightness");
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        activity.getContentResolver().notifyChange(uri, null);
    }

    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {

        Bitmap bm = null;
        //Log.d("xuchdeid", "getArtworkFromFile songid:" + songid + " albumid:" + albumid);
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");

                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (IllegalStateException ex) {
        } catch (FileNotFoundException ex) {
        }
        if (bm != null) {

        }

        return bm;
    }
    
    public static long getAlbumIdbySong(Context context, String artist, String album) {
    	long albumid = -1;
    	String[] projection =  new String[] { 
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM};
    	
    	String selection = MediaStore.Audio.Media.ARTIST + " = ? AND " +
    			           MediaStore.Audio.Media.ALBUM + " = ?";
    	String[] args = new String[]{artist, album};
    	Cursor cursor;
    	try {
    		cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection, selection, args, null);
    	} catch (Exception e) {
    		return albumid;
    	}
    	if (cursor != null && cursor.getCount() > 0) {
    		//Log.d("xuchdeid", "cursor != null");
    		if (cursor.moveToFirst()) {
    			albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
    			//Log.d("xuchdeid", "artist:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
    			//Log.d("xuchdeid", "ALBUM:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
    			//Log.d("xuchdeid", "albumid " + albumid);
    		}
    		cursor.close();
    	}
    	//Log.d("xuchdeid", "getAlbumIdbySongId:" + albumid);
    	return albumid;
    }
    
	public static Point getDpi(Activity context) {
		Point point = new Point(0, 0);
		Display display = context.getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics(); 
		Class<?> c;
		try {
			c = Class.forName("android.view.Display");
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, dm);

			point.x = dm.widthPixels;
			point.y = dm.heightPixels;
		}catch(Exception e){
			e.printStackTrace();
		}
		return point;
    }
	
	public static Bitmap screenshot(int width, int height) {
		Class<?> c;
		Bitmap picture = null;
		try {
			c = Class.forName("android.view.Surface");
			Method method = c.getMethod("screenshot", new Class[] {int.class, int.class});
			picture = (Bitmap) method.invoke(c, new Object[] {width, height});
		}catch(Exception e){
			e.printStackTrace();
		}
		return picture;
	}
	
	public static Bitmap takeScreenShot(Activity activity){ 
        View view = activity.getWindow().getDecorView(); 
        view.setDrawingCacheEnabled(true); 
        view.buildDrawingCache(); 
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap; 
    }
	
	public static void SaveScreen(Activity activity) {
		Bitmap bitmap;
		/*View v1 = activity.findViewById(android.R.id.content);
		v1.setDrawingCacheEnabled(true);
		bitmap = Bitmap.createBitmap(v1.getDrawingCache());
		v1.setDrawingCacheEnabled(false);*/
		
		bitmap = takeScreenShot(activity);
		//bitmap = screenshot(100, 100);
		
		File path = Environment.getExternalStorageDirectory();
		File file = new File(path, "Dump.png");

		FileOutputStream outputStream;

		try {
		    outputStream = new FileOutputStream(file);
		    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		    outputStream.flush();
		    outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

