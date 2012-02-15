package net.appositedesigns.fileexplorer.util;

import java.util.Comparator;

import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.PreferenceUtil.SortField;

public class FileListSorter implements Comparator<FileListEntry> {

	private FileListActivity mContext;
	
	private boolean dirsOnTop = false;
	private SortField sortField;
	private int dir;
	
	public FileListSorter(FileListActivity context){
		
		mContext = context;
		PreferenceUtil util = new PreferenceUtil(mContext);
		
		dirsOnTop = util.isShowDirsOnTop();
		sortField = util.getSortField();
		dir =  util.getSortDir();
	}
	
	@Override
	public int compare(FileListEntry file1, FileListEntry file2) {

		if(dirsOnTop)
		{
			if(file1.getPath().isDirectory() && file2.getPath().isFile())
			{
				return -1;
			}
			else if(file2.getPath().isDirectory() && file1.getPath().isFile())
			{
				return 1;
			}
		}
		
		switch (sortField) {
		case NAME:
			return dir * file1.getName().compareToIgnoreCase(file2.getName());
			
		case MTIME:
			return dir * file1.getLastModified().compareTo(file2.getLastModified());

		case SIZE:
			return dir * Long.valueOf(file1.getSize()).compareTo(file2.getSize());
			
		default:
			break;
		}
		
		return 0;
	}
	
	

}
