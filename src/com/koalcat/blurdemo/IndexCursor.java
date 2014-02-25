package com.koalcat.blurdemo;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class IndexCursor implements Cursor {
	
	private ArrayAdapter<AppInfo> adapter;
	private int position;
     
	public IndexCursor(ArrayAdapter<AppInfo> adapter){
		this.adapter = adapter;
	}

	@Override
	public int getCount() {
		return adapter.getCount();
	}
	     
	@Override
	public String getString(int columnIndex) {
		AppInfo app = adapter.getItem(position);
		if (app.title_py.length() == 0) {
			return app.title.substring(0, 1);
		} else {
			return app.title_py.substring(0, 1);
		}
	}
     
	@Override
	public boolean moveToPosition(int position) {
		if(position < -1 || position > getCount()){
			return false;
		}
         
		this.position = position;
		return true;
	}
     
	@Override
	public void close() {}
	@Override
	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {}
	@Override
	public void deactivate() {}
	@Override
	public byte[] getBlob(int arg0) {return null;}
	@Override
	public int getColumnCount() {return 0;}
	@Override
	public int getColumnIndex(String columnName) {return 0;}
	@Override
	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {return 0;}
	@Override
	public String getColumnName(int columnIndex) {return null;}
	@Override
	public String[] getColumnNames() {return null;}
	@Override
	public double getDouble(int columnIndex) {return 0;}
	@Override
	public Bundle getExtras() {return null;}
	@Override
	public float getFloat(int columnIndex) {return 0;}
	@Override
	public int getInt(int columnIndex) {return 0;}
	@Override
	public long getLong(int columnIndex) {return 0;}
	@Override
	public int getPosition() {return position;}
	@Override
	public short getShort(int columnIndex) {return 0;}
	@Override
	public boolean getWantsAllOnMoveCalls() {return false;}
	@Override
	public boolean isAfterLast() {return false;}
	@Override
	public boolean isBeforeFirst() {return false;}
	@Override
	public boolean isClosed() {return false;}
	@Override
	public boolean isFirst() {return false;}
	@Override
	public boolean isLast() {return false;}
	@Override
	public boolean isNull(int columnIndex) {return false;}
	@Override
	public boolean move(int offset) {return false;}
	@Override
	public boolean moveToFirst() {return false;}
	@Override
	public boolean moveToLast() {return false;}
	@Override
	public boolean moveToNext() {return false;}
	@Override
	public boolean moveToPrevious() {return false;}
	@Override
	public void registerContentObserver(ContentObserver observer) {}
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {}
	@Override
	public boolean requery() {return false;}
	@Override
	public Bundle respond(Bundle extras) {return null;}
	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {}
	@Override
	public void unregisterContentObserver(ContentObserver observer) {}
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {}

	@Override
	public int getType(int columnIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Uri getNotificationUri() {
		// TODO Auto-generated method stub
		return null;
	}
}

