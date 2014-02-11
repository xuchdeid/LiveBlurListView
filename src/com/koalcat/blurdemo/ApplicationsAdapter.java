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

	public ApplicationsAdapter(Context context, ArrayList<AppInfo> apps) {
		super(context, 0, apps);
		mInflater = LayoutInflater.from(context);
		//mSectionString = context.getResources().getString(R.string.fast_scroll_alphabet);
		alphabetIndexer = new AlphabetIndexer(new IndexCursor(this), 0, "#ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final AppInfo info = getItem(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
		}

		info.icon = Utilities.createIconThumbnail(info.icon, getContext());

		final TextView textView = (TextView) convertView.findViewById(R.id.item_text);
		textView.setText(info.title);
        
		final ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image);
		imageView.setImageDrawable(info.icon);

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
}
