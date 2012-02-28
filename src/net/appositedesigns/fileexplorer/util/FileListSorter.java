package net.appositedesigns.fileexplorer.util;

import java.util.Comparator;

import net.appositedesigns.fileexplorer.activity.BaseFileListActivity;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.PreferenceHelper.SortField;

public class FileListSorter implements Comparator<FileListEntry> {

	private BaseFileListActivity mContext;
	
	private boolean dirsOnTop = false;
	private SortField sortField;
	private int dir;
	
	public FileListSorter(BaseFileListActivity context){
		
		mContext = context;
		PreferenceHelper util = new PreferenceHelper(mContext);
		
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
