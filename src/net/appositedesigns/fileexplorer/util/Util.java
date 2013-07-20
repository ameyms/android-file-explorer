package net.appositedesigns.fileexplorer.util;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.callbacks.CancellationCallback;
import net.appositedesigns.fileexplorer.model.FileListEntry;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class Util {

	private static final String TAG = Util.class.getName();
	private static File COPIED_FILE = null;
	private static int pasteMode = 1;
	
	
	public static final int PASTE_MODE_COPY = 0;
	public static final int PASTE_MODE_MOVE = 1;
	
	
	private Util(){}
	
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

		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		
		if(type == null)
			return false;
		else
		return (type.toLowerCase().startsWith("audio/"));

	}

	static boolean isVideo(File file) {

		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		
		if(type == null)
			return false;
		else
		return (type.toLowerCase().startsWith("video/"));
	}

	public static boolean isPicture(File file) {
		
		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		
		if(type == null)
			return false;
		else
		return (type.toLowerCase().startsWith("image/"));
	}
	
	public static boolean isProtected(File path)
	{
		return (!path.canRead() && !path.canWrite());
	}
	
	public static boolean isUnzippable(File path)
	{
		return (path.isFile() && path.canRead() && path.getName().endsWith(".zip"));
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
			if(Util.isProtected(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_sys_dir);
					
			}
			else if(Util.isSdCard(file))
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
			if(Util.isProtected(file))
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
			else if(Util.isMusic(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_music);
			}
			else if(Util.isVideo(file))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_video);
			}
			else if(Util.isPicture(file))
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

	public static String prepareMeta(FileListEntry file,FileListActivity context) {
		
		File f = file.getPath();
		try
		{
			if(isProtected(f))
			{
				return context.getString(R.string.system_path);
			}
			if(file.getPath().isFile())
			{
				return context.getString(R.string.size_is, FileUtils.byteCountToDisplaySize(file.getSize()));
			}
			
		}
		catch (Exception e) {
			Log.e(Util.class.getName(), e.getMessage());
		}
		
		return "";
	}

	public static boolean paste(int mode, File destinationDir, AbortionFlag flag) {
		
		Log.v(TAG, "Will now paste file on clipboard");
		File fileBeingPasted = new File(getFileToPaste().getParent(),getFileToPaste().getName());
		if(doPaste(mode, getFileToPaste(), destinationDir, flag))
		{
			if(getPasteMode() == PASTE_MODE_MOVE)
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

	public static boolean canShowQuickActions(FileListEntry currentFile, FileListActivity mContext) {
		
		if(!mContext.getPreferenceHelper().useQuickActions() || mContext.isInPickMode())
		{
			return false;
		}
		
		File path = currentFile.getPath();
		if(isProtected(path))
		{
			return false;
		}
		if(isSdCard(path))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public static CharSequence[] getFileProperties(FileListEntry file, FileListActivity context) {
		
		if(Util.isSdCard(file.getPath()))
		{
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			long sdAvailSize = (long)stat.getAvailableBlocks() *(long)stat.getBlockSize();
			long totalSize = (long)stat.getBlockCount() *(long)stat.getBlockSize();
			
			return new CharSequence[]{context.getString(R.string.total_capacity, Util.getSizeStr(totalSize)),
					context.getString(R.string.free_space, Util.getSizeStr(sdAvailSize))};
		}
		else if(file.getPath().isFile())
		return new CharSequence[]{context.getString(R.string.filepath_is, file.getPath().getAbsolutePath()),
				context.getString(R.string.mtime_is, DateFormat.getDateFormat(context).format(file.getLastModified())),
				context.getString(R.string.size_is, FileUtils.byteCountToDisplaySize(file.getSize()))};
		
		else
		{
			return new CharSequence[]{context.getString(R.string.filepath_is, file.getPath().getAbsolutePath()),
					context.getString(R.string.mtime_is, DateFormat.getDateFormat(context).format(file.getLastModified()))};
		}
	}
	
	private static String getSizeStr(long bytes) {
		
		if(bytes >= FileUtils.ONE_GB)
		{
			return (double)Math.round((((double)bytes / FileUtils.ONE_GB)*100))/100 + " GB";
		}
		else if(bytes >= FileUtils.ONE_MB)
		{
			return (double)Math.round((((double)bytes / FileUtils.ONE_MB)*100))/100 + " MB";
		}
		else if(bytes >= FileUtils.ONE_KB)
		{
			return (double)Math.round((((double)bytes / FileUtils.ONE_KB)*100))/100 + " KB";
		}

		return bytes+" bytes";
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
			Log.w(TAG, "Could not execute DU command for "+dir.getAbsolutePath(), e);
		}
		
		return sizes;
		
	}

	public static void gotoPath(final String currentPath, final FileListActivity mContext) {
	
		gotoPath(currentPath, mContext, null);
	}
	public static void gotoPath(final String currentPath, final FileListActivity mContext,final CancellationCallback callback) {
		
		final EditText input = new EditText(mContext);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		input.setSingleLine();
		new Builder(mContext)
		.setTitle(mContext.getString(R.string.goto_path))
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			CharSequence toPath = input.getText();
			try
			{
				File toDir = new File(toPath.toString());
				if(toDir.isDirectory() && toDir.exists())
				{
					mContext.listContents(toDir);
				}
				else
				{
					throw new FileNotFoundException();
				}
				
			}
			catch (Exception e) {
				Log.e(TAG, "Error navigating to path"+toPath, e);
				new Builder(mContext)
				.setTitle(mContext.getString(R.string.error))
				.setMessage(mContext.getString(R.string.path_not_exist))
				.show();
			}
		  }
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
			  if(callback != null)
			  callback.onCancel();
		  }
		})
		.show();
		input.setText(currentPath);
	}

	public static File getDownloadsFolder() {
		return new File("/sdcard/"+Environment.DIRECTORY_DOWNLOADS);
	}

	public static File getDcimFolder() {
		return new File("/sdcard/"+Environment.DIRECTORY_DCIM);
	}
}
