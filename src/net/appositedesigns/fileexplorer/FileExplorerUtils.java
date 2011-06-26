package net.appositedesigns.fileexplorer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

final class FileExplorerUtils {

	private static File COPIED_FILE = null;
	private static int pasteMode = 1;
	
	
	static final int PASTE_MODE_COPY = 0;
	static final int PASTE_MODE_MOVE = 1;
	
	
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
	
	static boolean isPicture(File file) {
		
		String fileName = file.getName();
		return (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".bmp") || fileName.endsWith(".gif"));
	}
	
	static boolean isProtected(File path)
	{
		return (!path.canRead() && !path.canWrite());
	}


	static boolean isRoot(File dir) {
		
		return dir.getAbsolutePath().equals("/");
	}


	static boolean isSdCard(File file) {
		
		if(file.isDirectory())
		{
			if(file.getName().equalsIgnoreCase("sdcard"))
			{
				if(file.getParentFile().getAbsolutePath().equalsIgnoreCase("/"))
				{
					return true;
				}
			}
		}
		return false;
	}


	static Drawable getIcon(Context mContext, File file) {
		
		if(file.isDirectory()) //dir
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
			if(fileName.endsWith(".apk"))
			{
				return mContext.getResources().getDrawable(R.drawable.filetype_apk);
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


	static boolean delete(File fileToBeDeleted) {

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

	public static String prepareMeta(FileListEntry entry) {
		
		File f = entry.getPath();
		try
		{
			if(f.isFile())
			{
				return FileUtils.byteCountToDisplaySize(entry.getSize());
			}
			else if(isProtected(f))
			{
				return "System File";
			}
		}
		catch (Exception e) {
			Log.e(FileExplorerUtils.class.getName(), e.getMessage());
		}
		
		return "";
	}

	static void share(File resource, Context mContext) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
	
		Uri uri = Uri.fromFile(resource);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setType(type);
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(type==null?"*/*":type);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
	
		mContext.startActivity(Intent.createChooser(intent,"Send via"));
	
	}

	static boolean paste(int mode, File destinationDir, AbortionFlag flag) {
		
		File fileBeingPasted = new File(getFileToPaste().getParent(),getFileToPaste().getName());
		if(doPaste(mode, getFileToPaste(), destinationDir, flag))
		{
			if(fileBeingPasted.isFile())
			{
				FileUtils.deleteQuietly(fileBeingPasted);
			}
			else
			{
				try {
					FileUtils.deleteDirectory(fileBeingPasted);
				} catch (IOException e) {
					Log.e(FileExplorerUtils.class.getName(), e.getMessage());
					e.printStackTrace();
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	static boolean doPaste(int mode, File srcFile, File destinationDir, AbortionFlag flag) {
		
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


	static boolean canPaste(File destDir) {
		
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
}
