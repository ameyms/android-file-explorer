package net.appositedesigns.fileexplorer.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.appositedesigns.fileexplorer.FileExplorerApp;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.adapters.FileListAdapter;
import net.appositedesigns.fileexplorer.callbacks.CancellationCallback;
import net.appositedesigns.fileexplorer.callbacks.FileActionsCallback;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.quickactions.QuickActionHelper;
import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import net.appositedesigns.fileexplorer.util.Util;
import net.appositedesigns.fileexplorer.workers.FileMover;
import net.appositedesigns.fileexplorer.workers.Finder;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class FileListActivity extends BaseFileListActivity {

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

		if (prefs.isEulaAccepted()) {
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
		if(!isPicker)
		{
			gotoLocations = getResources().getStringArray(R.array.goto_locations);
		}
		else
		{
			gotoLocations = getResources().getStringArray(R.array.goto_locations_no_bookmark);
		}
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
				QuickActionHelper.get(FileListActivity.this).setShowActions(false);
				mCurrentActionMode = FileListActivity.this
						.startActionMode(new FileActionsCallback(
								FileListActivity.this, fileListEntry) {

							@Override
							public void onDestroyActionMode(
									ActionMode mode) {
								view.setSelected(false);
								mCurrentActionMode = null;
								QuickActionHelper.get(FileListActivity.this).setShowActions(true);
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
		String restartDirPath = getIntent().getStringExtra(
				PreferenceUtil.KEY_RESTART_DIR);
		
		if (restartDirPath != null) 
		{
			File restartDir = new File(restartDirPath);
			if (restartDir.exists() && restartDir.isDirectory()) {
				currentDir = restartDir;
				getIntent().removeExtra(PreferenceUtil.KEY_RESTART_DIR);
			}
		}
		else if (savedInstanceState!=null && savedInstanceState.getSerializable(CURRENT_DIR_DIR) != null) {
			
			currentDir = new File(savedInstanceState
					.getSerializable(CURRENT_DIR_DIR).toString());
		} 
		else 
		{
			currentDir = prefs.getStartDir();
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
					listContents(prefs.getStartDir());
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
					if(!isPicker)
					{
						openBookmarks(actionBar);
						break;
					}
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
		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		actionBar.setSelectedNavigationItem(0);
		startActivity(intent);
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
		if (prefs.useBackNavigation()) {
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

	public void listContents(File dir) {
		if (!dir.isDirectory() || Util.isProtected(dir)) {
			return;
		}
		new Finder(this).execute(dir);
	}

	private void gotoParent() {

		if (Util.isRoot(currentDir)) {
			// Do nothing finish();
		} else {
			listContents(currentDir.getParentFile());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		if(isPicker)
		{
			inflater.inflate(R.menu.picker_options_menu, menu);
		}
		else if(prefs.getTheme() == FileExplorerApp.THEME_WHITE)
		{
			inflater.inflate(R.menu.options_menu_light, menu);
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
			menu.findItem(R.id.menu_bookmark_toggle).setChecked(prefs.isBookmarked(currentDir.getAbsolutePath()));
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
				prefs.addBookmark(currentDir.getAbsolutePath());
			}
			else
			{
				prefs.removeBookmark(currentDir.getAbsolutePath());
			}
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

	public void setCurrentDir(File dir) {
		currentDir = dir;
	}

	public void setNewChildren(List<FileListEntry> children) {
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
		mSpinnerAdapter.notifyDataSetChanged();
		getActionBar().setSelectedNavigationItem(0);
		
	}

	public void refresh() {
		listContents(currentDir);
	}

	private void restartApp() {
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra(PreferenceUtil.KEY_RESTART_DIR, currentDir.getAbsolutePath());
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