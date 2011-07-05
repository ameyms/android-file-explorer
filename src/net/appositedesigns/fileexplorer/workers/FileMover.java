package net.appositedesigns.fileexplorer.workers;

import java.io.File;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.util.AbortionFlag;
import net.appositedesigns.fileexplorer.util.FileExplorerUtils;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

public class FileMover extends AsyncTask<File, Integer, Boolean>
{
	private int mode= 1;
	private AbortionFlag flag;
	private FileExplorerMain caller;
	private ProgressDialog moveProgressDialog;
	
	public FileMover(FileExplorerMain context, int mode) {
		caller = context;
		this.mode =mode;
		flag = new AbortionFlag();
	}
	@Override
	protected void onPostExecute(Boolean result) {
		if(result)
		{
			if(mode==FileExplorerUtils.PASTE_MODE_MOVE)
			{
				FileExplorerUtils.setPasteSrcFile(null, 0);
			}
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(moveProgressDialog.isShowing())
					{
						moveProgressDialog.dismiss();
					}
					if(mode==FileExplorerUtils.PASTE_MODE_COPY)
					{
						Toast.makeText(caller.getApplicationContext(), caller.getString(R.string.copy_complete), Toast.LENGTH_LONG);
					}
					else
					{
						Toast.makeText(caller.getApplicationContext(), caller.getString(R.string.move_complete), Toast.LENGTH_LONG);
					}
					caller.refresh();
				}
			});
		}
		else
		{
			caller.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(moveProgressDialog.isShowing())
					{
						moveProgressDialog.dismiss();
					}
					Toast.makeText(caller.getApplicationContext(), caller.getString(R.string.generic_operation_failed), Toast.LENGTH_LONG);
				}
			});
		}
	}
	@Override
	protected Boolean doInBackground(File... params) {
		
		File destDir = params[0];
		return FileExplorerUtils.paste(mode, destDir, flag);
		
	}
	
	@Override
	protected void onPreExecute() {
		caller.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				String message = caller.getString(R.string.copying_path,FileExplorerUtils.getFileToPaste().getName());
				if(mode==FileExplorerUtils.PASTE_MODE_MOVE)
				{
					message = 
						caller.getString(R.string.moving_path,FileExplorerUtils.getFileToPaste().getName());
				}
				moveProgressDialog = new ProgressDialog(caller);
				moveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				moveProgressDialog.setMessage(message);
				moveProgressDialog.setButton(caller.getString(R.string.run_in_background), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
					}
				});
				moveProgressDialog.setButton2(caller.getString(R.string.cancel), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						FileMover.this.flag.abort();
					}
				});
				moveProgressDialog.show();
				
			}
		});
	}
}