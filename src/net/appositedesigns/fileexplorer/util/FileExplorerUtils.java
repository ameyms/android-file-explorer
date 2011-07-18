package net.appositedesigns.fileexplorer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.FileListEntry;
import net.appositedesigns.fileexplorer.R;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public final class FileExplorerUtils {

	private static final String TAG = FileExplorerUtils.class.getName();
	private static File COPIED_FILE = null;
	private static int pasteMode = 1;
	
	
	public static final int PASTE_MODE_COPY = 0;
	public static final int PASTE_MODE_MOVE = 1;
	
	
	private FileExplorerUtils(){}
	
	 public static synchronized void setPasteSrcFile(File f, int mode) 
	  {  
	         COPIED_FILE = f;  
	         pasteMode = mode%2; 
	  }  

	 public static synchronized File getFileToPaste()
	 {
		 return COPIED_FILE;
	 }
	 
	 public static synchronized int getPasteMode()
	 {
		 return pasteMode;
	 }
	static boolean isMusic(File file) {
		
		String fileName = file.getName();
		return (fileName.endsWith(".mp3") || fileName.endsWith(".aac") || fileName.endsWith(".ogg") || fileName.endsWith(".wav") || fileName.endsWith(".m4a") || fileName.endsWith(".wma"));
	}
	
	public static boolean isPicture(File file) {
		
		String fileName = file.getName();
		return (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif"));
	}
	
	public static boolean isProtected(File path)
	{
		return (!path.canRead() && !path.canWrite());
	}


	public static boolean isRoot(File dir) {
		
		return dir.getAbsolutePath().equals("/");
	}


	public static boolean isSdCard(File file) {
		
		try {
			return (file.getCanonicalPath().equals(Environment.getExternalStorageDirectory().getCanonicalPath()));
		} catch (IOException e) {
			return false;
		}
		
	}


	public static Drawable getIcon(Context mContext, File file) {
		
		if(!file.isFile()) //dir
		{
			if(FileExplorerUtils.isProtected(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_sys_dir);
					
			}
			else if(FileExplorerUtils.isSdCard(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_sdcard);
			}
			else 
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_dir);
			}
		}
		else //file
		{
			String fileName = file.getName();
			if(FileExplorerUtils.isProtected(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_sys_file);
					
			}
			if(fileName.endsWith(".apk"))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_apk);
			}
			if(fileName.endsWith(".zip"))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_zip);
			}
			else if(FileExplorerUtils.isMusic(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_music);
			}
			else if(FileExplorerUtils.isPicture(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_image);
			}
			else
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_generic);
			}
		}
		
	}


	public static boolean delete(File fileToBeDeleted) {

		try
		{
			FileUtils.forceDelete(fileToBeDeleted);
			return true;
		} catch (IOException e) {
			return false;
		}
	}


	public static boolean mkDir(String canonicalPath, CharSequence newDirName) {
		
		File newdir = new File(canonicalPath+File.separator+newDirName);
		return newdir.mkdirs();
		
	}

	public static String prepareMeta(FileListEntry file,FileExplorerMain context) {
		
		File f = file.getPath();
		try
		{
			if(isProtected(f))
			{
				return context.getString(R.string.system_path);
			}
			return context.getString(R.string.size_is, FileUtils.byteCountToDisplaySize(file.getSize()));
			
		}
		catch (Exception e) {
			Log.e(FileExplorerUtils.class.getName(), e.getMessage());
		}
		
		return "";
	}

	public static boolean paste(int mode, File destinationDir, AbortionFlag flag) {
		
		Log.v(TAG, "Will now paste file on clipboard");
		File fileBeingPasted = new File(getFileToPaste().getParent(),getFileToPaste().getName());
		if(doPaste(mode, getFileToPaste(), destinationDir, flag))
		{
			if(fileBeingPasted.isFile())
			{
				if(FileUtils.deleteQuietly(fileBeingPasted))
				{
					Log.i(TAG, "File deleted after paste "+fileBeingPasted.getAbsolutePath());
				}
				else
				{
					Log.w(TAG, "File NOT deleted after paste "+fileBeingPasted.getAbsolutePath());
				}
			}
			else
			{
				try {
					FileUtils.deleteDirectory(fileBeingPasted);
				} catch (IOException e) {
					Log.e(TAG, "Error while deleting directory after paste - "+fileBeingPasted.getAbsolutePath(), e);
					return false;
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	private static boolean doPaste(int mode, File srcFile, File destinationDir, AbortionFlag flag) {
		
		if(!flag.isAborted())
		try
		{
			if(srcFile.isDirectory())
			{
				
				File newDir = new File(destinationDir.getAbsolutePath()+File.separator+srcFile.getName());
				newDir.mkdirs();
				
				for(File child : srcFile.listFiles())
				{
					doPaste(mode, child, newDir, flag);
				}
				return true;
			}
			else
			{
				FileUtils.copyFileToDirectory(srcFile, destinationDir);
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}
		else
		{
			return false;
		}
	}


	public static boolean canPaste(File destDir) {
		
		if(getFileToPaste() == null)
		{
			return false;
		}
		if(getFileToPaste().isFile())
		{
			return true;
		}
		try
		{
			if(destDir.getCanonicalPath().startsWith(COPIED_FILE.getCanonicalPath()))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		catch (Exception e) {
			
			return false;
		}
	}

	public static boolean canShowActions(FileListEntry currentFile, FileExplorerMain mContext) {
		
		PreferenceUtil prefs = new PreferenceUtil(mContext);
		File path = currentFile.getPath();
		if(isProtected(path))
		{
			return false;
		}
		if(isSdCard(path))
		{
			return prefs.isEnableSdCardOptions();
		}
		else
		{
			return true;
		}
	}

	public static CharSequence[] getFileProperties(FileListEntry file, FileExplorerMain context) {
		
		if(file.getPath().isFile())
		return new CharSequence[]{context.getString(R.string.filepath_is, file.getPath().getAbsolutePath()),
				context.getString(R.string.mtime_is, file.getLastModified().toLocaleString()),
				context.getString(R.string.size_is, FileUtils.byteCountToDisplaySize(file.getSize()))};
		
		else
		{
			return new CharSequence[]{context.getString(R.string.filepath_is, file.getPath().getAbsolutePath()),
					context.getString(R.string.mtime_is, file.getLastModified().toLocaleString())};
		}
	}
	
	public static Map<String, Long> getDirSizes(File dir)
	{
		Map<String, Long> sizes = new HashMap<String, Long>();
		
		try {
			
			Process du = Runtime.getRuntime().exec("/system/bin/du -b -d1 "+dir.getCanonicalPath(), new String[]{}, Environment.getRootDirectory());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					du.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null)
			{
				String[] parts = line.split("\\s+");
				
				String sizeStr = parts[0];
				Long size = Long.parseLong(sizeStr);
				
				String path = parts[1];
				
				sizes.put(path, size);
			}
			
		} catch (IOException e) {
			Log.e(TAG, "Could not execute DU command for "+dir.getAbsolutePath(), e);
		}
		
		return sizes;
		
	}
}
