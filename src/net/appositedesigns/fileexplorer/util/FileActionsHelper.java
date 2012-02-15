package net.appositedesigns.fileexplorer.util;

import java.io.File;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.callbacks.OperationCallback;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.workers.Trasher;
import net.appositedesigns.fileexplorer.workers.Zipper;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

public class FileActionsHelper {

	
	protected static final String TAG = FileActionsHelper.class.getName();

	
	public static void copyFile(File file, FileListActivity mContext)
	{
		Util.setPasteSrcFile(file,Util.PASTE_MODE_COPY);
		Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.copied_toast, file.getName()), Toast.LENGTH_SHORT).show();
		mContext.invalidateOptionsMenu();
	}
	
	public static void cutFile(final File file, final FileListActivity mContext)
	{
		Util.setPasteSrcFile(file,Util.PASTE_MODE_MOVE);
		Toast.makeText(mContext.getApplicationContext(),  mContext.getString(R.string.cut_toast, file.getName()), Toast.LENGTH_SHORT).show();
		mContext.invalidateOptionsMenu();
	}
	
	public static void showProperties(final FileListEntry file, final FileListActivity mContext)
	{
		new Builder(mContext)
		.setTitle(mContext.getString(R.string.properties_for, file.getName()))
		.setItems(Util.getFileProperties(file, mContext), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			dialog.dismiss();
		  }
		})
		.show();
	}
	public static void deleteFile(final File file, final FileListActivity mContext,final OperationCallback<Void> callback)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setCancelable(true);
		builder.setMessage(mContext.getString(R.string.confirm_delete, file.getName()))
	       .setCancelable(false)
	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {

	        	   new Trasher(mContext, callback).execute(file);
	           }
	       })
	       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                
	           }
	       }).setTitle(R.string.confirm);
		
		AlertDialog confirm = builder.create();
		confirm.show();
	}

	public static int[] getContextMenuOptions(File file, FileListActivity caller) {

		PreferenceUtil prefs = new PreferenceUtil(caller);
		
		if(Util.isProtected(file))
		{
			return null;
		}
		if(Util.isSdCard(file))
		{
			if(prefs.isEnableSdCardOptions())
			{
				return new int[]{R.id.menu_rescan, R.id.menu_props};
			}
			else
			{
				return new int[]{R.id.menu_props};
			}
			
		}
		else if(file.isDirectory())
		{
			if(prefs.isZipEnabled())
			{
				return new int[]{R.id.menu_copy,R.id.menu_cut, R.id.menu_delete, R.id.menu_rename, R.id.menu_zip, R.id.menu_props};
			}
			return new int[]{R.id.menu_copy, R.id.menu_cut, R.id.menu_delete, R.id.menu_rename, R.id.menu_props};
			
		}
		else
		{
			if(prefs.isZipEnabled())
			{
				return new int[]{R.id.menu_share, R.id.menu_copy, R.id.menu_cut, R.id.menu_delete, R.id.menu_rename, R.id.menu_zip, R.id.menu_props};
			}
			return new int[]{R.id.menu_share, R.id.menu_copy, R.id.menu_cut, R.id.menu_delete, R.id.menu_rename, R.id.menu_props};
		}
	}

	public static void rename(final File file, final FileListActivity mContext, final OperationCallback<Void> callback)
	{
		final EditText input = new EditText(mContext);
		input.setHint(mContext.getString(R.string.enter_new_name));
		input.setSingleLine();
		
		new Builder(mContext)
		.setTitle(mContext.getString(R.string.rename_dialog_title, file.getName()))
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			CharSequence newName = input.getText();
			try
			{
				File parentFolder = file.getParentFile();
				if(file.renameTo(new File(parentFolder, newName.toString())))
				{
					if(callback!=null)
					{
						callback.onSuccess();
					}
					Toast.makeText(mContext, mContext.getString(R.string.rename_toast, file.getName(), newName), Toast.LENGTH_LONG).show();
					mContext.refresh();
				}
				else
				{
					if(callback!=null)
					{
						callback.onFailure(new Exception());
					}
					new Builder(mContext)
					.setTitle(mContext.getString(R.string.error))
					.setMessage(mContext.getString(R.string.rename_failed, file.getName()))
					.show();
				}
				
			}
			catch (Exception e) {
				if(callback!=null)
				{
					callback.onFailure(e);
				}

				Log.e(TAG, "Error occured while renaming path", e);
				new Builder(mContext)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(mContext.getString(R.string.error))
				.setMessage(mContext.getString(R.string.rename_failed, file.getName()))
				.show();
			}
		  }
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
		  }
		})
		.show();
	}
	
	public static void zip(final File file, final FileListActivity mContext)
	{
		try
		{
			File zipLoc = new PreferenceUtil(mContext).getZipDestinationDir();
			final EditText input = new EditText(mContext);
			input.setHint(mContext.getString(R.string.enter_zip_file_name));
			input.setSingleLine();
			new Builder(mContext)
			.setTitle(mContext.getString(R.string.zip_dialog, zipLoc.getAbsolutePath()))
			.setView(input).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  
				String zipName = input.getText().toString();
				new Zipper(zipName,mContext).execute(file);
			  }
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			   
				  dialog.dismiss();
			  }
			}).show();
		}
		catch (Exception e) {
			Log.e(TAG, "Zip destination was invalid", e);
			mContext.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
	
					new Builder(mContext)
					.setTitle(mContext.getString(R.string.error))
					.setMessage(mContext.getString(R.string.zip_dest_invalid))
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
	public static void doOperation(FileListEntry entry,int action, FileListActivity mContext, OperationCallback<Void> callback) {
		
		File file = entry.getPath();
		switch (action) {
		case R.id.menu_copy:
			copyFile(file, mContext);
			break;

		case R.id.menu_cut:
			cutFile(file, mContext);
			break;
			
		case R.id.menu_delete:
			deleteFile(file, mContext, callback);
			break;
			
		case R.id.menu_share:
			share(file, mContext);
			break;
			
		case R.id.menu_rename:
			rename(file, mContext, callback);
			break;
			
		case R.id.menu_zip:
			zip(file, mContext);
			break;
			
		case R.id.menu_rescan:
			rescanMedia(mContext);
			break;
			
		case R.id.menu_props:
			showProperties(entry, mContext);
			break;
		default:
			break;
		}
		
	}
	
	private static void rescanMedia(FileListActivity mContext) {

		mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory()))); 
		
		Toast.makeText(mContext, R.string.media_rescan_started, Toast.LENGTH_SHORT).show();
	}

	public static void share(File file, Context mContext) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
	
		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setType(type);
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(type==null?"*/*":type);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
	
		mContext.startActivity(Intent.createChooser(intent,mContext.getString(R.string.share_via)));
	
	}

}
