package net.appositedesigns.fileexplorer;

import net.appositedesigns.fileexplorer.util.FileActionsHelper;
import android.content.Intent;
import android.net.Uri;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ShareActionProvider;

public abstract class FileActionsCallback implements Callback {

	private FileExplorerMain activity;
	private FileListEntry file;
	static int[] allOptions = {R.id.menu_rescan, R.id.menu_copy,R.id.menu_cut, R.id.menu_delete, R.id.menu_props, R.id.menu_share, R.id.menu_rename, R.id.menu_zip};
	
	public FileActionsCallback(FileExplorerMain activity,
			FileListEntry fileListEntry) {

		this.activity = activity;
		this.file = fileListEntry;

	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		
		FileActionsHelper.doOperation(file, item.getItemId(), activity);
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

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
				      // Get the provider and hold onto it to set/change the share intent.
					 ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
					 
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
