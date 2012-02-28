package net.appositedesigns.fileexplorer.activity;

import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public abstract class BaseFileListActivity extends ListActivity {

	protected PreferenceUtil prefs;
	private OnSharedPreferenceChangeListener listener;
	protected boolean shouldRestartApp = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		prefs = new PreferenceUtil(this);
		setTheme(prefs.getTheme());
		super.onCreate(savedInstanceState);

		listenToThemeChange();
	}
	
	private void listenToThemeChange() {

		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (PreferenceUtil.PREF_THEME.equals(key)) {

					shouldRestartApp = true;

				}
				if (PreferenceUtil.PREF_USE_QUICKACTIONS.equals(key)) {

					shouldRestartApp = true;

				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(listener);
	}
	public PreferenceUtil getPreferenceHelper()
	{
		return prefs;
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(listener);
	}
}
