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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.StatFs;
import android.util.Log;

public class Finder extends AsyncTask<File, Integer, List<FileListEntry>>
{
	
	private static final String TAG = Finder.class.getName();
	
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
		Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" received");
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				if(waitDialog!=null && waitDialog.isShowing())
				{
					waitDialog.dismiss();
				}
				Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" passed to caller");
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
					
					Log.e(TAG, "Progressbar waiting thread encountered exception ",e);
					e.printStackTrace();
				}

				
			}
		};
		caller.runOnUiThread(waitForASec);
		
		currentDir = params[0];
		Log.v(TAG, "Received directory to list paths - "+currentDir.getAbsolutePath());
		
		String[] children = currentDir.list();
		List<FileListEntry> childFiles = new ArrayList<FileListEntry>();
		
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
			if(f.isDirectory())
			{
				StatFs stat = new StatFs(f.getPath());
				long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();
				child.setSize((bytesAvailable));
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
		
		Log.v(TAG, "Will now interrupt thread waiting to show progress bar");
		if(waitForASec.isAlive())
		{
			try
			{
				waitForASec.interrupt();
			}
			catch (Exception e) {
				
				Log.e(TAG, "Error while interrupting thread",e);
			}
		}
		return childFiles;
	}
}
