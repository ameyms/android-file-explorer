package net.appositedesigns.fileexplorer.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.FileListEntry;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.util.FileExplorerUtils;
import net.appositedesigns.fileexplorer.util.FileListSorter;
import net.appositedesigns.fileexplorer.util.PreferenceUtil;

import org.apache.commons.io.FileUtils;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class Finder extends AsyncTask<File, Integer, List<FileListEntry>>
{
	
	private FileExplorerMain caller;
	private ProgressDialog waitDialog;
	private PreferenceUtil prefs;
	
	private File currentDir;
	
	public Finder(FileExplorerMain caller) {
		
		this.caller = caller;
		prefs = new PreferenceUtil(this.caller);
	}

	@Override
	protected void onPostExecute(List<FileListEntry> result) {

		final List<FileListEntry> childFiles = result;
		
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				if(waitDialog!=null && waitDialog.isShowing())
				{
					waitDialog.dismiss();
				}
				caller.setCurrentDir(currentDir);
				caller.setNewChildren(childFiles);
			}
		});
	
	}
	@Override
	protected List<FileListEntry> doInBackground(File... params) {
		
		Thread waitForASec = new Thread() {
			
			@Override
			public void run() {
				
				waitDialog = new ProgressDialog(caller);
				waitDialog.setTitle("");
				waitDialog.setMessage(caller.getString(R.string.querying_filesys));
				waitDialog.setIndeterminate(true);
				
				try {
					Thread.sleep(100);
					if(this.isInterrupted())
					{
						return;
					}
					else
					{
						waitDialog.show();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				
			}
		};
		caller.runOnUiThread(waitForASec);
		
		currentDir = params[0];
		String[] children = currentDir.list();
		List<FileListEntry> childFiles = new ArrayList<FileListEntry>();
		
		boolean findDirSizes = prefs.isFindDirSizes();
		boolean showHidden = prefs.isShowHidden();
		boolean showSystem = prefs.isShowSystemFiles();
		
		for(String fileName : children)
		{
			File f = new File(currentDir.getAbsolutePath()+File.separator+fileName);
			
			if(!f.exists())
			{
				continue;
			}
			if(FileExplorerUtils.isProtected(f) && !showSystem)
			{
				continue;
			}
			if(f.isHidden() && !showHidden)
			{
				continue;
			}
			String fname = f.getName();
			
			FileListEntry child = new FileListEntry();
			child.setName(fname);
			child.setPath(f);
			if(f.isDirectory() && findDirSizes)
			{
				child.setSize(FileUtils.sizeOfDirectory(f));
			}
			else
			{
				child.setSize(f.length());
			}
			child.setLastModified(new Date(f.lastModified()));
			childFiles.add(child);
		}
		
		FileListSorter sorter = new FileListSorter(caller);
		Collections.sort(childFiles, sorter);
		
		waitForASec.interrupt();
		return childFiles;
	}
}
