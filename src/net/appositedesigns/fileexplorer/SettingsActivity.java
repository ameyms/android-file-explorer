package net.appositedesigns.fileexplorer;

import java.util.List;

import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(new PreferenceUtil(this).getTheme());
		super.onCreate(savedInstanceState);
		
//		addPreferencesFromResource(R.xml.prefs);

	}
	
    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.prefs, target);
    }

	
	public static class NavigationPreferences extends PreferenceFragment  {
		 @Override
		 public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 addPreferencesFromResource(R.xml.nav_prefs);
		}
	}
	
	public static class GeekyPreferences extends PreferenceFragment {
		 @Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 addPreferencesFromResource(R.xml.geeky_prefs);
		}
	}
	
	public static class ViewPreferences extends PreferenceFragment  {
		
		 @Override
		 public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 addPreferencesFromResource(R.xml.view_prefs);
		}
	}
	
	public static class AboutPreferences extends PreferenceFragment  {
		
		 @Override
		 public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			 addPreferencesFromResource(R.xml.about_prefs);
			 
			 Preference share = (Preference)findPreference("pref_opt_share");
			 share.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					
					final Intent intent = new Intent(Intent.ACTION_SEND);
			    	intent.setType("text/plain");
			    	String text = getString(R.string.share_text);
			    	intent.putExtra(Intent.EXTRA_TEXT, text);
			    	intent.putExtra(Intent.EXTRA_SUBJECT, "FileExplorer");

			    	startActivity(Intent.createChooser(intent,
			    			getString(R.string.share_via)));
					return true;
				}
			});
		}
	}
	
}