package net.appositedesigns.fileexplorer.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.BookmarkListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.FileListSorter;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class BookmarkLoader extends AsyncTask<File, Integer, List<FileListEntry>>
{
	
	private static final String TAG = BookmarkLoader.class.getName();
	
	private BookmarkListActivity caller;
	private ProgressDialog waitDialog;
	
	public BookmarkLoader(BookmarkListActivity caller) {
		
		this.caller = caller;
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
				Log.v(TAG, "Bookmarks for passed to caller");
				caller.setBookmarks(childFiles);
				if(childFiles.size()>0)
				{
					caller.getActionBar().setSubtitle(caller.getString(R.string.bookmarks_count, childFiles.size()));
				}
				else
				{
					caller.getActionBar().setSubtitle(caller.getString(R.string.bookmarks_count_0));
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
		
		
		List<FileListEntry> childFiles = new ArrayList<FileListEntry>(caller.getBookmarker().getBookmarks());
		
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
