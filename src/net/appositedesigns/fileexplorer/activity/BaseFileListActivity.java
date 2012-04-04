package net.appositedesigns.fileexplorer.activity;

import net.appositedesigns.fileexplorer.util.BookmarksHelper;
import net.appositedesigns.fileexplorer.util.PreferenceHelper;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public abstract class BaseFileListActivity extends ListActivity {

	protected PreferenceHelper prefs;
	protected BookmarksHelper bookmarker;
	private OnSharedPreferenceChangeListener listener;
	protected boolean shouldRestartApp = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = new PreferenceHelper(this);
		bookmarker = new BookmarksHelper(this);
		setTheme(prefs.getTheme());
		super.onCreate(savedInstanceState);

		listenToThemeChange();
	}
	
	private void listenToThemeChange() {

		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (PreferenceHelper.PREF_THEME.equals(key)) {

					shouldRestartApp = true;

				}
				if (PreferenceHelper.PREF_USE_QUICKACTIONS.equals(key)) {

					shouldRestartApp = true;

				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(listener);
	}
	public synchronized PreferenceHelper getPreferenceHelper()
	{
		return prefs;
	}
	
	public BookmarksHelper getBookmarker()
	{
		return bookmarker;
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
