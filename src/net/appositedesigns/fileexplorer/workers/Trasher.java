package net.appositedesigns.fileexplorer.workers;

import java.io.File;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.util.FileExplorerUtils;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class Trasher extends AsyncTask<File, Integer, Boolean>
{
	
	private File fileToBeDeleted;
	private FileExplorerMain caller;
	private ProgressDialog waitDialog;
	
	public Trasher(FileExplorerMain caller) {

		this.caller = caller;
	}
	@Override
	protected void onPostExecute(Boolean result) {
		if(result)
		{
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					waitDialog.dismiss();
					Toast.makeText(caller.getApplicationContext(), "Deleted", Toast.LENGTH_LONG);
					caller.refresh();
				}
			});
		}
		else
		{
			FileExplorerUtils.setPasteSrcFile(fileToBeDeleted, FileExplorerUtils.getPasteMode());
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					waitDialog.dismiss();
					new Builder(caller)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(caller.getString(R.string.error))
					.setMessage(caller.getString(R.string.delete_failed, fileToBeDeleted.getName()))
					.show();
					
					
				}
			});
		}
	}
	@Override
	protected Boolean doInBackground(File... params) {
		
		fileToBeDeleted = params[0];
		
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				waitDialog = new ProgressDialog(caller);
				waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				waitDialog.setMessage(caller.getString(R.string.deleting_path,fileToBeDeleted.getName()));
				waitDialog.setCancelable(false);
				
				waitDialog.show();
			}
		});
		
		try
		{
			if(FileExplorerUtils.getFileToPaste().getCanonicalPath().equalsIgnoreCase(fileToBeDeleted.getCanonicalPath()))
			{
				FileExplorerUtils.setPasteSrcFile(null, FileExplorerUtils.getPasteMode());
			}
			return FileExplorerUtils.delete(fileToBeDeleted);
		}
		catch (Exception e) {
			return false;
		}
		
	}

}
