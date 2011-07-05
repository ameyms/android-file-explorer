package net.appositedesigns.fileexplorer.util;

import java.io.File;

import android.content.Context;
import android.preference.PreferenceManager;

public final class PreferenceUtil {

	public enum SortField{NAME,MTIME,SIZE}
	
	private Context mContext;
	public PreferenceUtil(Context context) 
	{
		mContext = context;
	}
	public boolean isShowDirsOnTop()
	{
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SHOW_DIRS_FIRST, false);
	}
	public boolean isShowHidden()
	{
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SHOW_HIDDEN, true);
	}
	
	public SortField getSortField()
	{
		String field= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_SORT_FIELD, Constants.SORT_FIELD_NAME);
		if(Constants.SORT_FIELD_NAME.equalsIgnoreCase(field))
		{
			return SortField.NAME;
		}
		else if(Constants.SORT_FIELD_M_TIME.equalsIgnoreCase(field))
		{
			return SortField.MTIME;
		}
		else if(Constants.SORT_FIELD_SIZE.equalsIgnoreCase(field))
		{
			return SortField.SIZE;
		}
		else
		{
			return SortField.NAME;
		}
	}
	
	public int getSortDir()
	{
		String field= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_SORT_DIR, "asc");
		if(Constants.SORT_DIR_ASC.equalsIgnoreCase(field))
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
	
	public File getStartDir()
	{
		String dirPath= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_HOME_DIR, "/");
		File homeDir = new File(dirPath);
		
		if(homeDir.exists() && homeDir.isDirectory())
		{
			return homeDir;
		}
		else
		{
			return new File("/");
		}
	}
	public boolean isFindDirSizes() {
		
		return false;
	//	return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SHOW_DIR_SIZES, false);
	}
	public boolean isShowSystemFiles() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SHOW_SYSFILES, true);
	}
	
}
