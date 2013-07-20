package net.appositedesigns.fileexplorer.callbacks;

import android.content.Intent;
import android.net.Uri;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.FileActionsHelper;

public abstract class FileActionsCallback implements Callback {

	private FileListActivity activity;
	private FileListEntry file;
	static int[] allOptions = {R.id.menu_copy,R.id.menu_cut, R.id.menu_delete, R.id.menu_props, R.id.menu_share, R.id.menu_rename, R.id.menu_zip, R.id.menu_unzip};
	
	public FileActionsCallback(FileListActivity activity,
			FileListEntry fileListEntry) {

		this.activity = activity;
		this.file = fileListEntry;

	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
		
		FileActionsHelper.doOperation(file, item.getItemId(), activity, new OperationCallback<Void>() {
			
			@Override
			public Void onSuccess() {
				return null;
			}
			
			@Override
			public void onFailure(Throwable e) {
				
			}
		});
		mode.finish();
		return true;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode actionMode, Menu menu) {

		int[] validOptions = FileActionsHelper.getContextMenuOptions(file.getPath(), activity);
		
		if(validOptions==null || validOptions.length ==0)
		{
			onDestroyActionMode(actionMode);
			return false;
		}
		actionMode.setTitle(activity.getString(R.string.selected_,
				file.getName()));

		MenuInflater inflater = activity.getMenuInflater();
		
		inflater.inflate(R.menu.context_menu, menu);
		
		for(int o :allOptions)
		{
			boolean valid = false;
			for(int v : validOptions)
			{
				if(o == v)
				{
					valid = true;
					break;
				}
			}
			if(!valid)
			{
				menu.removeItem(o);
			}
			else
			{
				if(o == R.id.menu_share)
				{
					 MenuItem menuItem = menu.findItem(R.id.menu_share);
					 ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
					 mShareActionProvider.setOnShareTargetSelectedListener(new OnShareTargetSelectedListener() {
						
						@Override
						public boolean onShareTargetSelected(ShareActionProvider source,
								Intent intent) {
							actionMode.finish();
							return false;
						}
					});
					 final Intent intent = new Intent(Intent.ACTION_SEND);
						
						Uri uri = Uri.fromFile(file.getPath());
						String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
						intent.setType(type);
						intent.setAction(Intent.ACTION_SEND);
						intent.setType(type==null?"*/*":type);
						intent.putExtra(Intent.EXTRA_STREAM, uri);
					
						
					 mShareActionProvider.setShareIntent(intent);
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

}
