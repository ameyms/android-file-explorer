package net.appositedesigns.fileexplorer.activity;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.appositedesigns.fileexplorer.FileExplorerApp;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.adapters.FileListAdapter;
import net.appositedesigns.fileexplorer.callbacks.CancellationCallback;
import net.appositedesigns.fileexplorer.callbacks.FileActionsCallback;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.model.FileListing;
import net.appositedesigns.fileexplorer.util.FileActionsHelper;
import net.appositedesigns.fileexplorer.util.Util;
import net.appositedesigns.fileexplorer.workers.FileMover;
import net.appositedesigns.fileexplorer.workers.Finder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileListActivity extends BaseFileListActivity {

	private static final String TAG = FileListActivity.class.getName();
	
	private static final String CURRENT_DIR_DIR = "current-dir";
	
	private ListView explorerListView;
	private File currentDir;
	private List<FileListEntry> files;
	private FileListAdapter adapter;
	protected Object mCurrentActionMode;
	private ArrayAdapter<CharSequence> mSpinnerAdapter;
	private CharSequence[] gotoLocations;
	private boolean isPicker = false;
	private FileExplorerApp app;
	private File previousOpenDirChild;
	private boolean focusOnParent;
	private boolean excludeFromMedia  = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		app = (FileExplorerApp)getApplication();
		isPicker = getIntent().getBooleanExtra(FileExplorerApp.EXTRA_IS_PICKER, false);
		if(Intent.ACTION_GET_CONTENT.equals(getIntent().getAction()))
		{
			isPicker  = true;
			app.setFileAttachIntent(getIntent());
		}
		
		initUi();
		initGotoLocations();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prepareActionBar();
		initRootDir(savedInstanceState);

		files = new ArrayList<FileListEntry>();

		initFileListView();
		focusOnParent = getPreferenceHelper().focusOnParent();
		if (getPreferenceHelper().isEulaAccepted()) {
			listContents(currentDir);
		} else {
			EulaPopupBuilder.create(this).show();
		}

	}

	private void initUi() {
		if(isPicker)
		{
			getWindow().setUiOptions(0);
		}
		
	}

	private void initGotoLocations() {
		
		gotoLocations = getResources().getStringArray(R.array.goto_locations);
	}

	private void initFileListView() {
		explorerListView = (ListView) getListView();
		adapter = new FileListAdapter(this, files);
		explorerListView.setAdapter(adapter);
		explorerListView.setTextFilterEnabled(true);
		explorerListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (explorerListView.isClickable()) {
					FileListEntry file = (FileListEntry) explorerListView
							.getAdapter().getItem(position);
					select(file.getPath());
				}
			}

		});

		explorerListView.setOnItemLongClickListener(getLongPressListener());
		registerForContextMenu(explorerListView);		
	}

	private OnItemLongClickListener getLongPressListener() {
		return new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,
					final View view, int arg2, long arg3) {

				if(!explorerListView.isLongClickable())
					return true;
				if(isPicker)
				{
					return false;
				}
				 view.setSelected(true);

				final FileListEntry fileListEntry = (FileListEntry) adapter
						.getItem(arg2);
				
				
				if (mCurrentActionMode != null) {
					return false;
				}
				if (Util.isProtected(fileListEntry
						.getPath())) {
					return false;
				}
				explorerListView.setEnabled(false);

				mCurrentActionMode = FileListActivity.this
						.startActionMode(new FileActionsCallback(
								FileListActivity.this, fileListEntry) {

							@Override
							public void onDestroyActionMode(
									ActionMode mode) {
								view.setSelected(false);
								mCurrentActionMode = null;
								explorerListView.setEnabled(true);
							}

						});
				view.setSelected(true);
				return true;
			}

		};
	}

	private void initRootDir(Bundle savedInstanceState) {
		// If app was restarted programmatically, find where the user last left
		// it
		String restartDirPath = getIntent().getStringExtra(FileExplorerApp.EXTRA_FOLDER);
		
		if (restartDirPath != null) 
		{
			File restartDir = new File(restartDirPath);
			if (restartDir.exists() && restartDir.isDirectory()) {
				currentDir = restartDir;
				getIntent().removeExtra(FileExplorerApp.EXTRA_FOLDER);
			}
		}
		else if (savedInstanceState!=null && savedInstanceState.getSerializable(CURRENT_DIR_DIR) != null) {
			
			currentDir = new File(savedInstanceState
					.getSerializable(CURRENT_DIR_DIR).toString());
		} 
		else 
		{
			currentDir = getPreferenceHelper().getStartDir();
		}		
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(CURRENT_DIR_DIR, currentDir.getAbsolutePath());

	}

	private void prepareActionBar() {
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		mSpinnerAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, gotoLocations);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, getActionbarListener(actionBar));
		
	}

	private OnNavigationListener getActionbarListener(final ActionBar actionBar) {
		return new OnNavigationListener() {
			
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				
				int selectedIndex = actionBar.getSelectedNavigationIndex();
				
				if(selectedIndex == 0)
				{
					return false;
				}
				switch (selectedIndex) {
					
				case 1:
					listContents(getPreferenceHelper().getStartDir());
					break;
					
					
				case 2:
					listContents(new File("/sdcard"));
					break;
					
				case 3:
					listContents(Util.getDownloadsFolder());
					break;
					
				case 4:
					listContents(Util.getDcimFolder());
					break;
					
				case 5:
					openBookmarks(actionBar);
					break;
				case 6:
					Util.gotoPath(currentDir.getAbsolutePath(), FileListActivity.this, new CancellationCallback() {
						
						@Override
						public void onCancel() {
							 actionBar.setSelectedNavigationItem(0);
							
						}
					});
					break;

				default:
					break;
				}
				
				
				return true;
			}

		};
	}
	private void openBookmarks(final ActionBar actionBar) {
		Intent intent = new Intent();
		intent.setAction(FileExplorerApp.ACTION_OPEN_BOOKMARK);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(FileExplorerApp.EXTRA_IS_PICKER, isPicker);
		actionBar.setSelectedNavigationItem(0);
		startActivityForResult(intent, FileExplorerApp.REQ_PICK_BOOKMARK);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case FileExplorerApp.REQ_PICK_BOOKMARK:
			if(resultCode == RESULT_OK)
			{
				String selectedBookmark = data.getStringExtra(FileExplorerApp.EXTRA_SELECTED_BOOKMARK);
				listContents(new File(selectedBookmark));
			}
			break;

		default:
			break;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		if (shouldRestartApp) {
			shouldRestartApp = false;
			restartApp();
		}
	}

	@Override
	public void onBackPressed() {

		if(isPicker)
		{
			super.onBackPressed();
			return;
		}
		if (getPreferenceHelper().useBackNavigation()) {
			if (Util.isRoot(currentDir)) {
				finish();
			} else {
				gotoParent();
			}
		} else {
			super.onBackPressed();
		}

	}

	void select(File file) {
		if (Util.isProtected(file)){
			new Builder(this)
					.setTitle(getString(R.string.access_denied))
					.setMessage(
							getString(R.string.cant_open_dir, file.getName()))
					.show();
		} else if (file.isDirectory()) {
			
			listContents(file);
			
		} else {
			doFileAction(file);
		}
	}

	private void doFileAction(File file) {
		if (Util.isProtected(file) || file.isDirectory()) {
			return;
		}
		
		if(isPicker)
		{
			pickFile(file);
			return;
		}
		else
		{
			openFile(file);
			return;
		}
	}

	private void openFile(File file) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setDataAndType(uri, type == null ? "*/*" : type);
		startActivity((Intent.createChooser(intent,
				getString(R.string.open_using))));
	}

	private void pickFile(File file) {
		Intent fileAttachIntent = app.getFileAttachIntent();
		fileAttachIntent.setData(Uri.fromFile(file));
		setResult(Activity.RESULT_OK, fileAttachIntent);
		finish();
		return;
	}

	public void listContents(File dir)
	{
		listContents(dir, null);
	}
	public void listContents(File dir, File previousOpenDirChild) {
		if (!dir.isDirectory() || Util.isProtected(dir)) {
			return;
		}
		if(previousOpenDirChild!=null)
		{
			this.previousOpenDirChild = new File(previousOpenDirChild.getAbsolutePath());
		}
		else
		{
			this.previousOpenDirChild = null;
		}
		new Finder(this).execute(dir);
	}

	private void gotoParent() {

		if (Util.isRoot(currentDir)) {
			// Do nothing finish();
		} else {
			listContents(currentDir.getParentFile(), currentDir);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		if(isPicker)
		{
			inflater.inflate(R.menu.picker_options_menu, menu);
		}
		else 
		{
			inflater.inflate(R.menu.options_menu, menu);
		}
		return true;
		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if(!isPicker)
		{
			if(getPreferenceHelper().isMediaExclusionEnabled())
			{
				menu.findItem(R.id.menu_media_exclusion).setVisible(true);
				menu.findItem(R.id.menu_media_exclusion).setChecked(excludeFromMedia);
			}
			else
			{
				menu.findItem(R.id.menu_media_exclusion).setVisible(false);
			}
			menu.findItem(R.id.menu_bookmark_toggle).setChecked(bookmarker.isBookmarked(currentDir.getAbsolutePath()));
			if (Util.canPaste(currentDir)) {
				menu.findItem(R.id.menu_paste).setVisible(true);
			} else {
				menu.findItem(R.id.menu_paste).setVisible(false);
			}	
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {

		case android.R.id.home:
			gotoParent();
			return true;

		case R.id.menu_cancel:
			setResult(RESULT_CANCELED);
			finish();
			return true;
			
		case R.id.menu_bookmark_toggle:
			boolean setBookmark = item.isChecked();
			item.setChecked(!setBookmark);
			if(!setBookmark)
			{
				bookmarker.addBookmark(currentDir.getAbsolutePath());
			}
			else
			{
				bookmarker.removeBookmark(currentDir.getAbsolutePath());
			}
			return true;
			
		case R.id.menu_media_exclusion:
			item.setChecked(!excludeFromMedia);
			setMediaExclusionForFolder();
			return true;
			
		case R.id.menu_goto:
			Util.gotoPath(currentDir.getAbsolutePath(), this);
			return true;

		case R.id.menu_paste:
			confirmPaste();
			return true;

		case R.id.menu_refresh:
			refresh();
			return true;
			
		case R.id.menu_newfolder:
			confirmCreateFolder();
			return true;

		case R.id.menu_settings:
			Intent prefsIntent = new Intent(FileListActivity.this,
					SettingsActivity.class);
			startActivity(prefsIntent);
			return true;
		default:
			super.onOptionsItemSelected(item);
			break;
		}

		return true;
	}

	private void setMediaExclusionForFolder() {

		if(excludeFromMedia)
		{
			//Now include folder in media
			FileUtils.deleteQuietly(new File(currentDir, ".nomedia"));
			excludeFromMedia = false;
		}
		else
		{
			try
			{
				FileUtils.touch(new File(currentDir, ".nomedia"));
				excludeFromMedia = true;
			}
			catch(Exception e)
			{
				Log.e(TAG, "Error occurred while creating .nomedia file", e);
			}
		}
		FileActionsHelper.rescanMedia(this);
		refresh();
	}

	private void confirmPaste() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.confirm));
		alert.setMessage(getString(R.string.confirm_paste_text,
				Util.getFileToPaste().getName()));

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
						new FileMover(FileListActivity.this, Util
								.getPasteMode()).execute(currentDir);
					}
				});

		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
					}
				});

		alert.show();

	}

	private void confirmCreateFolder() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getString(R.string.create_folder));
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setHint(getString(R.string.enter_folder_name));
		input.setSingleLine();
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						CharSequence newDir = input.getText();
						if (Util.mkDir(
								currentDir.getAbsolutePath(), newDir)) {
							listContents(currentDir);
						}
					}
				});

		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
					}
				});

		alert.show();

	}

	public synchronized void setCurrentDirAndChilren(File dir, FileListing folderListing) {
		currentDir = dir;

		List<FileListEntry> children = folderListing.getChildren();
		excludeFromMedia   = folderListing.isExcludeFromMedia();
		TextView emptyText = (TextView) findViewById(android.R.id.empty);
		if (emptyText != null) {
			emptyText.setText(R.string.empty_folder);
		}
		files.clear();
		files.addAll(children);
		adapter.notifyDataSetChanged();
		getActionBar().setSelectedNavigationItem(0);
		
		if(Util.isRoot(currentDir))
		{
			gotoLocations[0] = getString(R.string.filesystem);	
		}
		else
		{
			gotoLocations[0] = currentDir.getName();
		}
		
		if(previousOpenDirChild!=null && focusOnParent)
		{
			int position = files.indexOf(new FileListEntry(previousOpenDirChild.getAbsolutePath()));
			if(position>=0)
			explorerListView.setSelection(position);
		}
		else
		{
			explorerListView.setSelection(0);
		}
		mSpinnerAdapter.notifyDataSetChanged();
		
		ActionBar ab = getActionBar();
		ab.setSelectedNavigationItem(0);
		
		ab.setSubtitle(getString(R.string.item_count_subtitle, children.size()));				
		if(Util.isRoot(currentDir) || currentDir.getParentFile()==null)
    	{
			ab.setDisplayHomeAsUpEnabled(false);
			ab.setTitle(getString(R.string.filesystem));
    	}
    	else
    	{
    		ab.setTitle(currentDir.getName());
    		ab.setDisplayHomeAsUpEnabled(true);
    	}
	}

	public void refresh() {
		listContents(currentDir);
	}

	private void restartApp() {
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra(FileExplorerApp.EXTRA_FOLDER, currentDir.getAbsolutePath());
		startActivity(i);
	}
	
	public boolean isInPickMode()
	{
		return isPicker;
	}

	public File getCurrentDir() {
		return currentDir;
	}

}