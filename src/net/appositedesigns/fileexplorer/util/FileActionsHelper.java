package net.appositedesigns.fileexplorer.util;

import java.io.File;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.workers.Trasher;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
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
	       }).setTitle(R.string.msg_title_confim).setIcon(android.R.drawable.ic_dialog_alert);
		
		AlertDialog confirm = builder.create();
		confirm.show();
	}

	public static int[] getContextMenuOptions(File file) {

		if(FileExplorerUtils.isSdCard(file))
		{
			return new int[]{R.string.action_format, R.string.action_unmount, R.string.action_prop};
		}
		else if(file.isDirectory())
		{
			return new int[]{R.string.action_copy, R.string.action_cut, R.string.action_delete, R.string.action_rename, R.string.action_prop};
			
		}
		else
		{
			return new int[]{R.string.action_share, R.string.action_copy, R.string.action_cut, R.string.action_delete,R.string.action_rename, R.string.action_prop};
		}
	}

	public static void rename(final File file, final FileExplorerMain mContext)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

		alert.setTitle(mContext.getString(R.string.rename_dialog_title, file.getName()));
		alert.setIcon(android.R.drawable.ic_dialog_info);
		// Set an EditText view to get user input 
		final EditText input = new EditText(mContext.getApplicationContext());
		input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		input.setHint(mContext.getString(R.string.enter_new_name));
		input.setSingleLine();
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  
			CharSequence newName = input.getText();
			try
			{
				file.renameTo(new File(newName.toString()));
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
		});

		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
		  }
		});

		alert.show();
	}
	public static void doOperation(File file,int action, FileExplorerMain mContext) {
		
		switch (action) {
		case R.string.action_copy:
			copyFile(file, mContext);
			break;

		case R.string.action_cut:
			cutFile(file, mContext);
			break;
			
		case R.string.action_delete:
			deleteFile(file, mContext);
			
		case R.string.action_share:
			share(file, mContext);
			
		case R.string.action_rename:
			rename(file, mContext);
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
