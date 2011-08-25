package net.appositedesigns.fileexplorer.util;

import java.io.File;

import net.appositedesigns.fileexplorer.exception.LocationInvalidException;
import android.content.Context;
import android.content.SharedPreferences;
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
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SHOW_DIRS_FIRST, true);
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
	
	public File getZipDestinationDir() throws LocationInvalidException
	{
		String dirPath= PreferenceManager.getDefaultSharedPreferences(mContext).getString(Constants.PREF_ZIP_LOCATION, "/sdcard/zipped");
		File dir = new File(dirPath);
		
		if(dir.exists() && dir.isDirectory())
		{
			return dir;
		}
		else if(!dir.exists())
		{
			dir.mkdirs();
			return dir;
		}
		else
		{
			throw new LocationInvalidException(dirPath);
		}
	}
	public boolean isEnableSdCardOptions() {

		return false;
		//		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_SDCARD_OPTIONS, true);
	}
	public boolean isZipEnabled() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(Constants.PREF_ZIP_ENABLE, false);
	}
	
	public boolean isEulaAccepted()
	{
		return mContext.getSharedPreferences(Constants.EULA_MARKER, mContext.MODE_PRIVATE).getBoolean(Constants.EULA_ACCEPTED, false);
	}
	
	public void markEulaAccepted()
	{
		SharedPreferences.Editor editor = mContext.getSharedPreferences(Constants.EULA_MARKER, mContext.MODE_WORLD_WRITEABLE).edit();
		editor.putBoolean(Constants.EULA_ACCEPTED, true);
		editor.commit();
	}
}
