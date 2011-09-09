package net.appositedesigns.fileexplorer;

import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new PreferenceUtil(this).getTheme());
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

	}
}