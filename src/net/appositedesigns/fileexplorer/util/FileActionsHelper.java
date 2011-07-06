package net.appositedesigns.fileexplorer.util;

import java.io.File;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.FileListEntry;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.workers.Trasher;
import net.appositedesigns.fileexplorer.workers.Zipper;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

public class FileActionsHelper {

	
	protected static final String TAG = FileActionsHelper.class.getName();

	
	public static void copyFile(File file, FileExplorerMain mContext)
	{
		FileExplorerUtils.setPasteSrcFile(file,FileExplorerUtils.PASTE_MODE_COPY);
		Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.copied_toast, file.getName()), Toast.LENGTH_SHORT).show();
	}
	
	public static void cutFile(final File file, final FileExplorerMain mContext)
	{
		FileExplorerUtils.setPasteSrcFile(file,FileExplorerUtils.PASTE_MODE_MOVE);
		Toast.makeText(mContext.getApplicationContext(),  mContext.getString(R.string.cut_toast, file.getName()), Toast.LENGTH_SHORT).show();
	}
	
	public static void showProperties(final FileListEntry file, final FileExplorerMain mContext)
	{
		new Builder(mContext)
		.setTitle(mContext.getString(R.string.properties_for, file.getName()))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setItems(FileExplorerUtils.getFileProperties(file, mContext), new OnClickListener() {
			
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
	public static void deleteFile(final File file, final FileExplorerMain mContext)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setCancelable(true);
		builder.setMessage(mContext.getString(R.string.confirm_delete, file.getName()))
	       .setCancelable(false)
	       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {

	        	   new Trasher(mContext).execute(file);
	           }
	       })
	       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                
	           }
	       }).setTitle(R.string.confirm).setIcon(android.R.drawable.ic_dialog_alert);
		
		AlertDialog confirm = builder.create();
		confirm.show();
	}

	public static int[] getContextMenuOptions(File file, FileExplorerMain caller) {

		PreferenceUtil prefs = new PreferenceUtil(caller);
		
		if(FileExplorerUtils.isSdCard(file))
		{
			if(prefs.isEnableSdCardOptions())
			{
				return new int[]{};
			}
			return new int[]{R.string.action_format, R.string.action_unmount, R.string.action_prop};
		}
		else if(file.isDirectory())
		{
			if(prefs.isZipEnabled())
			{
				return new int[]{R.string.action_copy, R.string.action_cut, R.string.action_delete, R.string.action_rename, R.string.action_zip, R.string.action_prop};
			}
			return new int[]{R.string.action_copy, R.string.action_cut, R.string.action_delete, R.string.action_rename, R.string.action_prop};
			
		}
		else
		{
			if(prefs.isZipEnabled())
			{
				return new int[]{R.string.action_share, R.string.action_copy, R.string.action_cut, R.string.action_delete,R.string.action_rename,R.string.action_zip, R.string.action_prop};
			}
			return new int[]{R.string.action_share, R.string.action_copy, R.string.action_cut, R.string.action_delete,R.string.action_rename, R.string.action_prop};
		}
	}

	public static void rename(final File file, final FileExplorerMain mContext)
	{
		final EditText input = new EditText(mContext);
		input.setHint(mContext.getString(R.string.enter_new_name));
		input.setSingleLine();
		
		new Builder(mContext)
		.setTitle(mContext.getString(R.string.rename_dialog_title, file.getName()))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			CharSequence newName = input.getText();
			try
			{
				file.renameTo(new File(newName.toString()));
				mContext.refresh();
			}
			catch (Exception e) {
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
	
	public static void zip(final File file, final FileExplorerMain mContext)
	{
		try
		{
			File zipLoc = new PreferenceUtil(mContext).getZipDestinationDir();
			final EditText input = new EditText(mContext);
			input.setHint(mContext.getString(R.string.enter_zip_file_name));
			input.setSingleLine();
			new Builder(mContext)
			.setTitle(mContext.getString(R.string.zip_dialog, zipLoc.getAbsolutePath()))
			.setIcon(android.R.drawable.ic_dialog_info)
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
					.setIcon(android.R.drawable.ic_dialog_alert)
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
	public static void doOperation(FileListEntry entry,int action, FileExplorerMain mContext) {
		
		File file = entry.getPath();
		switch (action) {
		case R.string.action_copy:
			copyFile(file, mContext);
			break;

		case R.string.action_cut:
			cutFile(file, mContext);
			break;
			
		case R.string.action_delete:
			deleteFile(file, mContext);
			break;
			
		case R.string.action_share:
			share(file, mContext);
			break;
			
		case R.string.action_rename:
			rename(file, mContext);
			break;
			
		case R.string.action_zip:
			zip(file, mContext);
			break;
			
		case R.string.action_prop:
			showProperties(entry, mContext);
			break;
		default:
			break;
		}
		
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
