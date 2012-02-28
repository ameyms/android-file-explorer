package net.appositedesigns.fileexplorer.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.Util;
import net.appositedesigns.fileexplorer.util.FileListSorter;
import net.appositedesigns.fileexplorer.util.PreferenceHelper;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class Finder extends AsyncTask<File, Integer, List<FileListEntry>>
{
	
	private static final String TAG = Finder.class.getName();
	
	private FileListActivity caller;
	private ProgressDialog waitDialog;
	private PreferenceHelper prefs;
	
	private File currentDir;
	
	public Finder(FileListActivity caller) {
		
		this.caller = caller;
		prefs = new PreferenceHelper(this.caller);
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
				caller.getActionBar().setSubtitle(caller.getString(R.string.item_count_subtitle, childFiles.size()));
				
				if(Util.isRoot(currentDir))
		    	{
					caller.getActionBar().setDisplayHomeAsUpEnabled(false);
					caller.getActionBar().setTitle(caller.getString(R.string.filesystem));
		    	}
		    	else
		    	{
		    		caller.getActionBar().setTitle(currentDir.getName());
		    		caller.getActionBar().setDisplayHomeAsUpEnabled(true);
		    	}
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
						caller.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {

								if(waitDialog!=null)
									waitDialog.show();
							}
						});

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
		
		Map<String, Long> dirSizes = Util.getDirSizes(currentDir);
		
		for(String fileName : children)
		{
			File f = new File(currentDir.getAbsolutePath()+File.separator+fileName);
			
			if(!f.exists())
			{
				continue;
			}
			if(Util.isProtected(f) && !showSystem)
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
				try
				{
					Long dirSize = dirSizes.get(f.getCanonicalPath());
					child.setSize(dirSize);
				}
				catch (Exception e) {

					Log.w(TAG, "Could not find size for "+child.getPath().getAbsolutePath());
					child.setSize(0);
				}
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
