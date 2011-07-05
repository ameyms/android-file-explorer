package net.appositedesigns.fileexplorer.quickactions;

import java.io.File;

import net.appositedesigns.fileexplorer.FileExplorerMain;
import net.appositedesigns.fileexplorer.FileListEntry;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.util.FileActionsHelper;
import android.view.View;
import android.view.View.OnClickListener;

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
				FileActionsHelper.copyFile(file, mContext);
				actions.dismiss();
			}
		});
		
		ActionItem cut = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_cut));
		cut.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				FileActionsHelper.cutFile(file, mContext);
				actions.dismiss();
			}
		});
		
		ActionItem trash = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_delete));
		 
		 trash.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				
				actions.dismiss();
				FileActionsHelper.deleteFile(file, mContext);
				
			}
		});
		
		 if(file.isFile())
		 {
				ActionItem share = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_share));
				share.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						FileActionsHelper.share(file, mContext);
						
					}
				});
				actions.addActionItem(share);
		 }
		 actions.addActionItem(cut);
		 actions.addActionItem(copy);
		 actions.addActionItem(trash);
		 actions.setAnimStyle(QuickAction.ANIM_AUTO);
		 actions.show();
		 
	 }
	 
	private void prepareQuickActionBar() {
		
	}
}
