package com.koalcat.blurdemo;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import java.util.ArrayList;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class ApplicationsAdapter extends ArrayAdapter<AppInfo> implements SectionIndexer{
	private final LayoutInflater mInflater;
	//private String mSectionString;
	private AlphabetIndexer alphabetIndexer;
	public LruCache<String, Bitmap> mMemoryCache;

	public ApplicationsAdapter(Context context, ArrayList<AppInfo> apps) {
		super(context, 0, apps);
		mInflater = LayoutInflater.from(context);
		//mSectionString = context.getResources().getString(R.string.fast_scroll_alphabet);
		alphabetIndexer = new AlphabetIndexer(new IndexCursor(this), 0, "#ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
		final int cacheSize = maxMemory / 8;  
		  
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
			@Override  
			protected int sizeOf(String key, Bitmap bitmap) {  
				return bitmap.getByteCount()/1024;
			}  
		};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final AppInfo info = getItem(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
		}

		//info.icon = Utilities.createIconThumbnail(info.icon, getContext());

		final TextView textView = ViewHolder.get(convertView, R.id.item_text);
		textView.setText(info.title);
        
		Bitmap bitmap = mMemoryCache.get(info.title_py);
		if (bitmap == null) {
			final PackageManager manager = getContext().getPackageManager();
			Drawable icon = null;
			try {
				icon = manager.getActivityIcon(info.mComponentName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bitmap = Utilities.createIcontoBitmapThumbnail(icon, this.getContext());
			mMemoryCache.put(info.title_py, bitmap);
		}
		
		final ImageView imageView = ViewHolder.get(convertView, R.id.item_image);
		//imageView.setImageDrawable(info.icon);
		imageView.setImageBitmap(bitmap);
//		imageView.setImageResource(R.drawable.ic_launcher);
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
		return alphabetIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return alphabetIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return alphabetIndexer.getSections();
	}
	
	public static class ViewHolder {
		@SuppressWarnings("unchecked")
		public static <T extends View> T get(View view, int id) {
			SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
			if (viewHolder == null) {
				viewHolder = new SparseArray<View>();
				view.setTag(viewHolder);
			}
			View childView = viewHolder.get(id);
			if (childView == null) {
				childView = view.findViewById(id);
				viewHolder.put(id, childView);
			}
			return (T) childView;
		}
	}
}
