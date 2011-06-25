package net.appositedesigns.fileexplorer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.MimeTypeMap;

final class FileExplorerUtils {

	public static File COPIED_FILE = null;
	private FileExplorerUtils(){}
	

	static boolean isMusic(File file) {
		
		String fileName = file.getName();
		return (fileName.endsWith(".mp3") || fileName.endsWith(".aac") || fileName.endsWith(".ogg") || fileName.endsWith(".wma"));
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

		if(fileToBeDeleted.isDirectory())
		{
			try
			{
				FileUtils.deleteDirectory(fileToBeDeleted);
				return true;
			}
			catch (IOException e)
			{
				return false;
			}
		}
		else
		{
			try {
				FileUtils.forceDelete(fileToBeDeleted);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}


	public static boolean mkDir(String canonicalPath, CharSequence newDirName) {
		
		File newdir = new File(canonicalPath+File.separator+newDirName);
		return newdir.mkdirs();
		
	}

	public static String prepareMeta(File f) {
		
		try
		{
			if(f.isFile())
			{
				return FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(f));
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
	
		try {
			intent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resource.toURL().toString())));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			intent.setType("application/x-octet-stream");
		}
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, resource);
	
		mContext.startActivity(Intent.createChooser(intent,"Send via"));
	
	}

	static boolean paste(File destinationDir, AbortionFlag flag) {
		
		if(doPaste(COPIED_FILE, destinationDir, flag))
		{
			COPIED_FILE = null;
			return true;
		}
		else
		{
			return false;
		}
	}
	static boolean doPaste(File srcFile, File destinationDir, AbortionFlag flag) {
		
		if(!flag.isAborted())
		try
		{
			if(srcFile.isDirectory())
			{
				
				File newDir = new File(destinationDir.getAbsolutePath()+File.separator+srcFile.getName());
				newDir.mkdirs();
				
				for(File child : srcFile.listFiles())
				{
					doPaste(child, newDir, flag);
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
}
