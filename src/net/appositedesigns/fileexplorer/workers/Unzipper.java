package net.appositedesigns.fileexplorer.workers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.util.AbortionFlag;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;

public class Unzipper extends AsyncTask<File, String, List<String>> {

	private static final String TAG = Unzipper.class.getName();
	private FileListActivity mContext;
	private AbortionFlag flag;
	private ProgressDialog zipProgress;
	private File destination;
	private int unzippedCount;
	
	public Unzipper(FileListActivity context, File destination) {
	
		mContext = context;
		unzippedCount = 0;
		this.flag = new AbortionFlag();
		this.destination = destination;
	}
	
	@Override
	protected void onPostExecute(final List<String> result)
	{
		mContext.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				boolean unzippedAtleastOne = (result!=null) && (result.size()>0);
				if (zipProgress != null && zipProgress.isShowing()) {
					zipProgress.dismiss();
				}
				if(flag.isAborted())
				{
					mContext.refresh();
					String message = unzippedAtleastOne?mContext.getString(R.string.unzip_abort_partial, destination.getAbsolutePath()):mContext.getString(R.string.unzip_aborted);
					new Builder(mContext)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(mContext.getString(R.string.abort))
					.setMessage(message)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
	
							dialog.dismiss();
						}
					})
					.show();
					
					
				}
				else if(unzippedAtleastOne)
				{
					mContext.refresh();
					new Builder(mContext)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(mContext.getString(R.string.success))
					.setMessage(mContext.getString(R.string.unzip_success, result.size()))
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
	
							dialog.dismiss();
						}
					})
					.show();
					
				}
			}
		});
	}
	
	@Override
	protected void onPreExecute() {
		
		mContext.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				zipProgress = new ProgressDialog(mContext);
				zipProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				zipProgress.setMessage(mContext.getString(R.string.unzip_progress));
				zipProgress.setButton(mContext.getString(R.string.run_in_background), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
					}
				});
				zipProgress.setButton2(mContext.getString(R.string.cancel), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						zipProgress.getButton(which).setText(R.string.cancelling);
						Unzipper.this.flag.abort();
					}
				});
				zipProgress.show();
				
				
			}
		});
	}
	
	@Override
	protected List<String> doInBackground(File... files) {
		
		List<String> extracted  = new ArrayList<String>();
		if(files!=null && files.length >= 1)
		{

			Log.i(TAG, "Zip files: "+files);
			for(File zipFile : files)
			{
				try {
					extractFolder(zipFile, extracted);
					extracted.add(zipFile.getAbsolutePath());
				} catch (Exception e) {
					Log.e(TAG, "Failed to unzip "+zipFile.getAbsolutePath()+". File exists - "+zipFile.exists(), e);
					mContext.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
			
							new Builder(mContext)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle(mContext.getString(R.string.zip))
							.setMessage(mContext.getString(R.string.unzip_failed_generic))
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
			
									dialog.dismiss();
								}
							})
							.show();
						}
					});
				}
			}
		}
		else
		{
			return null;
		}

		return extracted;
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		
		if(values!=null && values.length > 0)
		{
			unzippedCount +=values.length;
			final String curr = values[0];
			mContext.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					if(zipProgress!=null && zipProgress.isShowing())		
					{
						zipProgress.setMessage(mContext.getString(R.string.unzip_progress, curr));				
					}
				}
			});
		}
		
	}
	private void extractFolder(File zipFile, List<String> extracted) throws ZipException, IOException 
	{
	    Log.i(TAG,zipFile.getAbsolutePath());
	    int BUFFER = 2048;
	    File file = zipFile;

	    ZipFile zip = new ZipFile(file);
	    String newPath = destination.getAbsolutePath()+"/"+zipFile.getName().substring(0, zipFile.getName().length() - 4);

	    new File(newPath).mkdir();
	    Enumeration zipFileEntries = zip.entries();

	    // Process each entry
	    while (zipFileEntries.hasMoreElements() && !flag.isAborted())
	    {
	        // grab a zip file entry
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        //destFile = new File(newPath, destFile.getName());
	        File destinationParent = destFile.getParentFile();
	        publishProgress(destinationParent.getName());
	        // create the parent directory structure if needed
	        destinationParent.mkdirs();

	        extracted.add(destinationParent.getAbsolutePath());
	        if (!entry.isDirectory())
	        {
	        	publishProgress(entry.getName());
	            BufferedInputStream is = new BufferedInputStream(zip
	            .getInputStream(entry));
	            int currentByte;
	            // establish buffer for writing file
	            byte data[] = new byte[BUFFER];

	            // write the current file to disk
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos,
	            BUFFER);

	            // read and write until last byte is encountered
	            while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	            
	            extracted.add(destFile.getAbsolutePath());
	        }

//	        if (currentEntry.endsWith(".zip"))
//	        {
//	            // found a zip file, try to open
//	            extractFolder(destFile.getAbsolutePath());
//	        }
	    }
	}

}
