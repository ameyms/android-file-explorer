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
import net.appositedesigns.fileexplorer.model.FileListing;
import net.appositedesigns.fileexplorer.util.FileListSorter;
import net.appositedesigns.fileexplorer.util.Util;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class Finder extends AsyncTask<File, Integer, FileListing>
{
	
	private static final String TAG = Finder.class.getName();
	
	private FileListActivity caller;
	private ProgressDialog waitDialog;
	
	private File currentDir;
	
	public Finder(FileListActivity caller) {
		
		this.caller = caller;
	}

	@Override
	protected void onPostExecute(FileListing result) {

		FileListing childFilesList = result;
		Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" received");
		
		if(waitDialog!=null && waitDialog.isShowing())
		{
			waitDialog.dismiss();
		}
		Log.v(TAG, "Children for "+currentDir.getAbsolutePath()+" passed to caller");
		caller.setCurrentDirAndChilren(currentDir,childFilesList);
	
	}
	@Override
	protected FileListing doInBackground(File... params) {
		
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
		FileListing listing = new FileListing(new ArrayList<FileListEntry>());
		List<FileListEntry> childFiles = listing.getChildren();
		
		boolean showHidden = caller.getPreferenceHelper().isShowHidden();
		boolean showSystem = caller.getPreferenceHelper().isShowSystemFiles();
		Map<String, Long> dirSizes = Util.getDirSizes(currentDir);
		

		for(String fileName : children)
		{
			if(".nomedia".equals(fileName))
			{
				listing.setExcludeFromMedia(true);
			}
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
		return listing;
	}
}
