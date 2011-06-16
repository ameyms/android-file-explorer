package net.appositedesigns.fileexplorer;

import java.io.File;

import net.appositedesigns.fileexplorer.quickactions.ActionItem;
import net.appositedesigns.fileexplorer.quickactions.QuickAction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;

public final class QuickActionHelper {


	private FileExplorerMain mContext;

    
	public QuickActionHelper(FileExplorerMain mContext) {
		super();
		this.mContext = mContext;
		prepareQuickActionBar();
	}

	 public void showQuickActions(final View view, final File file) {
		 
		 ActionItem item = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_copy));
		 
		 ActionItem trash = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_delete));
		 trash.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setCancelable(true);
				builder.setMessage("Are you sure you want to delete "+file.getName()+"?")
			       .setCancelable(false)
			       .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {

			        	   mContext.deletePath(file);
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
		});
		 QuickAction actions = new QuickAction(view);
		 actions.addActionItem(item);
		 actions.addActionItem(trash);
		 actions.setAnimStyle(QuickAction.ANIM_AUTO);
		 actions.show();
		 
	 }
	 
	private void prepareQuickActionBar() {
		
	}
}
