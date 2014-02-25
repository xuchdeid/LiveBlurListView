package com.koalcat.blurdemo;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.koalcat.blurdemo.HanziToPinyin.Token;
import com.koalcat.view.LiveBlurListView;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class BlurActivity extends Activity implements OnItemClickListener {
	
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
	private static final int UI_NOTIFICATION_RATE = 4;

	private static Collator sCollator;
	private Handler mUIHandler, mHandler;
	
	private final int Intent_Flag = Intent.FLAG_ACTIVITY_NEW_TASK | 
			Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
	
	ApplicationsAdapter mApplicationsAdapter;
	private ArrayList<AppInfo> mApplications;
	private ApplicationsLoader mApplicationsLoader;
	private final HashMap<ComponentName, AppInfo> mAppInfoCache =
            new HashMap<ComponentName, AppInfo>(INITIAL_ICON_CACHE_CAPACITY);

	LiveBlurListView mList;
	private ActionBar mActionBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		canSetTranslucentFlag();
		mActionBar = getActionBar();
		mActionBar.hide();
		//mActionBar.setDisplayShowCustomEnabled(true);
		//mActionBar.setBackgroundDrawable(null);

		//TextView v = new TextView(this);
		//v.setText("hello world");
		//v.setTextSize(25);
		//LayoutParams l = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		//mActionBar.setCustomView(v, l);
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blur);

		sCollator = Collator.getInstance();
		mUIHandler = new Handler(this.getMainLooper());
		mList = (LiveBlurListView)findViewById(R.id.list);
		mApplications = new ArrayList<AppInfo>();
		mApplicationsAdapter = new ApplicationsAdapter(this, mApplications);
		mList.setAdapter(mApplicationsAdapter);
		mList.setOnItemClickListener(this);
		mApplicationsLoader = new ApplicationsLoader(this);
		HandlerThread mthread = new HandlerThread("launcher");
		mthread.start();
		mHandler = new Handler(mthread.getLooper());
		mHandler.postDelayed(mApplicationsLoader, 500);
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.blur, menu);
        return true;
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	class ApplicationsLoader implements Runnable {
    	
		private Context mLauncher;
		private volatile boolean mStopped;
		private volatile boolean mRunning;

		public ApplicationsLoader(Context launcher) {
			mLauncher = launcher;
			mRunning = true;
		}

		void stop() {
			mStopped = true;
		}

		boolean isRunning() {
			return mRunning;
		}
	
		public void run() {
	        	
			Intent mainIntent;
	
			//get apk main and can launcher activity
			mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			final PackageManager manager = mLauncher.getPackageManager();
			final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
	
			if (apps != null && !mStopped) {
				final int count = apps.size();
				// Can be set to null on the UI thread by the unbind() method
				// Do not access without checking for null first
				final ApplicationsAdapter applicationList = mApplicationsAdapter;
				ChangeNotifier action = new ChangeNotifier(applicationList, true);
				final HashMap<ComponentName, AppInfo> appInfoCache = mAppInfoCache;
	
				for (int i = 0; i < count && !mStopped; i++) {
					ResolveInfo info = apps.get(i);
					//if (!info.activityInfo.packageName.equals(getPackageName())) {
					AppInfo application =
						makeAndCacheApplicationInfo(manager, appInfoCache, info, mLauncher);
					if (action.add(application) && !mStopped) {
						mUIHandler.post(action);
						action = new ChangeNotifier(applicationList, false);
					}
					//}
				}
				mUIHandler.post(action);
	
			}
	
			mRunning = false;
		}
	}
    
	private static class ChangeNotifier implements Runnable {
		private final ApplicationsAdapter mApplicationList;
		private final ArrayList<AppInfo> mBuffer;
	
		private boolean mFirst = true;
	
		ChangeNotifier(ApplicationsAdapter applicationList, boolean first) {
			mApplicationList = applicationList;
			mFirst = first;
			mBuffer = new ArrayList<AppInfo>(UI_NOTIFICATION_RATE);
		}
	
		public void run() {
			final ApplicationsAdapter applicationList = mApplicationList;
			// Can be set to null on the UI thread by the unbind() method
			if (applicationList == null) return;
	
			if (mFirst) {
				applicationList.setNotifyOnChange(false);
				applicationList.clear();
				mFirst = false;
			}
	
			final ArrayList<AppInfo> buffer = mBuffer;
			final int count = buffer.size();
	
			for (int i = 0; i < count; i++) {
				// applicationList.setNotifyOnChange(false);
				applicationList.add(buffer.get(i));
			}
	
			buffer.clear();
	
			applicationList.sort(new ApplicationInfoComparator());
			applicationList.notifyDataSetChanged();
		}
	
		boolean add(AppInfo application) {
			final ArrayList<AppInfo> buffer = mBuffer;
			buffer.add(application);
			return buffer.size() >= UI_NOTIFICATION_RATE;
		}
	}
    
	private static AppInfo makeAndCacheApplicationInfo(PackageManager manager,
			HashMap<ComponentName, AppInfo> appInfoCache, ResolveInfo info,
			Context context) {
	
		ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name);
		/*if (info.activityInfo.packageName.equals(context.getPackageName())) {
			info.activityInfo.name = "com.koalcat.launcher.activity.Setting";
		}*/
		AppInfo application = appInfoCache.get(componentName);
	
		if (application == null) {
			application = new AppInfo();
	
			updateApplicationInfoTitleAndIcon(manager, info, application, context);
			application.setComponentName(componentName);
	
			appInfoCache.put(componentName, application);
		}
	
		return application;
	}
    
	private static void updateApplicationInfoTitleAndIcon(PackageManager manager, ResolveInfo info,
			AppInfo application, Context context) {
			application.title = info.loadLabel(manager).toString();
		if (application.title.charAt(0) == ' ') {
			application.title = application.title.substring(1);
		}
		if (application.title == null) {
			application.title = info.activityInfo.name;
	            
		}
		if (application.title_py == null) {
			application.title_py = getPinYin(application.title);
			if (application.title_py.equals("")) {
				application.title_py = application.title;
			}
		}
	
		application.icon =
				Utilities.createIconThumbnail(info.activityInfo.loadIcon(manager), context);
	}
    
	static class ApplicationInfoComparator implements Comparator<AppInfo> {
		public final int compare(AppInfo a, AppInfo b) {
			return sCollator.compare(a.title_py, b.title_py);
		}
	}
    
	public static String getPinYin(String input) {  
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(input);  
		StringBuilder sb = new StringBuilder();  
		if (tokens != null && tokens.size() > 0) {  
			for (Token token : tokens) {  
				if (Token.PINYIN == token.type) {  
					sb.append(token.target);  
				} else {  
					sb.append(token.source);  
				}  
			}  
		}  
		return sb.toString();  
	}  
    
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mApplicationsLoader != null) {
			mApplicationsLoader.stop();
		}
	}

	private int canSetTranslucentFlag() {
		Resources resources = getResources();
		int id = resources.getIdentifier("config_enableTranslucentDecor", "bool", "android");
		if (id != 0) {
			boolean enabled = resources.getBoolean(id);
			if (enabled) {
				Window w = getWindow();  
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | 
    	        		WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, 
    	        		WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |
    	        		WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				
				//w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, 
				//WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

				id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
				if (id > 0) {
					if (!resources.getBoolean(id)) return 0;
				} else {
					return 0;
				}
    	        
				id = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    	        
				if (id > 0) {
					return resources.getDimensionPixelSize(id);
				}
			}
		}
		return 0;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		AppInfo mAppInfo = (AppInfo)arg0.getItemAtPosition(arg2);
		launch_app(mAppInfo);
	}
	
	private void launch_app(AppInfo app) {
		app.setActivity(app.mComponentName, Intent_Flag);
		try {
			startActivity(app.intent);
		} catch (ActivityNotFoundException e) {
			
		}
		finish();
	}
}
