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
	}

	 public void showQuickActions(final View view, final FileListEntry entry) {
		 
		final File file = entry.getPath();
		final QuickAction actions = new QuickAction(view);
		int[] availableActions = FileActionsHelper.getContextMenuOptions(file, mContext);
		
		ActionItem action = null;
		
//		for(int i=availableActions.length-1;i>=0;i--)
		for(int i=0;i<availableActions.length;i++)
		{
			int a = availableActions[i];
			action = null;
			switch (a) {
			case R.string.action_cut:
				action = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_cut));
				action.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						FileActionsHelper.cutFile(file, mContext);
						actions.dismiss();
					}
				});
				break;
				
			case R.string.action_copy:
				action  = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_copy));
				action.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						FileActionsHelper.copyFile(file, mContext);
						actions.dismiss();
					}
				});
				break;
				
			case R.string.action_delete:
				action  = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_delete));
				 
				action.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						
						actions.dismiss();
						FileActionsHelper.deleteFile(file, mContext);
						
					}
				});
				break;
				
			case R.string.action_share:
				action = new ActionItem(mContext.getResources().getDrawable(R.drawable.action_share));
				action.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						actions.dismiss();
						FileActionsHelper.share(file, mContext);
						
						
					}
				});

				break;

			default:
				break;
			}
			
			if(action!=null)
			{
				 actions.addActionItem(action);
			}
		}
		

		if(availableActions.length>0)
		{
			actions.setAnimStyle(QuickAction.ANIM_AUTO);
			actions.show();
		}
		 
	 }

}
