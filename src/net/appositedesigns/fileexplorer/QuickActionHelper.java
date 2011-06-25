package net.appositedesigns.fileexplorer;

import java.io.File;

import net.appositedesigns.fileexplorer.quickactions.ActionItem;
import net.appositedesigns.fileexplorer.quickactions.QuickAction;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public final class QuickActionHelper {


	private FileExplorerMain mContext;

    
	public QuickActionHelper(FileExplorerMain mContext) {
		super();
		this.mContext = mContext;
		prepareQuickActionBar();
	}

	 public void showQuickActions(final View view, final FileListEntry entry) {
		 
		final File file = entry.getPath();
		final QuickAction actions = new QuickAction(view);
		
		ActionItem copy = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_copy));
		copy.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				FileExplorerUtils.COPIED_FILE = file;
				Toast.makeText(mContext, ""+file.getName()+" copied", Toast.LENGTH_SHORT);
				actions.dismiss();
			}
		});
		
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

			        	   actions.dismiss();
			        	   mContext.deletePath(file);
			           }
			       })
			       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                actions.dismiss();
			           }
			       }).setTitle(R.string.msg_title_confim).setIcon(android.R.drawable.ic_dialog_alert);
				
				AlertDialog confirm = builder.create();
				confirm.show();
			}
		});
		
		 if(file.isFile())
		 {
				ActionItem share = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_share));
				share.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						FileExplorerUtils.share(file, mContext);
						
					}
				});
				actions.addActionItem(share);
		 }
		 actions.addActionItem(copy);
		 actions.addActionItem(trash);
		 actions.setAnimStyle(QuickAction.ANIM_AUTO);
		 actions.show();
		 
	 }
	 
	private void prepareQuickActionBar() {
		
	}
}
