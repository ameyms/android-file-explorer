package net.appositedesigns.fileexplorer;

import java.io.File;

import net.apposite.fileexplorer.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

final class FileExplorerUtils {

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


	static CharSequence getSize(long length) {
		
		if(length >= 1024*1024)
		{
			return (length/(1024*1024))+" MB";
		}
		else if(length >= 1024)
		{
			return (length/(1024))+" KB";
		}
		else
		{
			return length+" B";
		}
	}
}
