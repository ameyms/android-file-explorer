package net.appositedesigns.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.appositedesigns.fileexplorer.quickactions.QuickActionHelper;
import net.appositedesigns.fileexplorer.util.Constants;
import net.appositedesigns.fileexplorer.util.FileExplorerUtils;
import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import net.appositedesigns.fileexplorer.workers.FileMover;
import net.appositedesigns.fileexplorer.workers.Finder;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FileExplorerMain extends ListActivity {

	private static final String CURRENT_DIR_DIR = "current-dir";
	private ListView explorerListView;
	private File currentDir;
	private PreferenceUtil prefs;
	private List<FileListEntry> files;
	private FileListAdapter adapter;
	private OnSharedPreferenceChangeListener listener;
	protected boolean shouldRestartApp = false;
	protected Object mCurrentActionMode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		prefs = new PreferenceUtil(this);

		setTheme(prefs.getTheme());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prepareActionBar();

		initRootDir(savedInstanceState);

		listenToThemeChange();
		files = new ArrayList<FileListEntry>();

		initFileListView();

		if (prefs.isEulaAccepted()) {
			listContents(currentDir);
		} else {
			EulaPopupBuilder.create(this).show();
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

		explorerListView
		.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,
					final View view, int arg2, long arg3) {

				final FileListEntry fileListEntry = (FileListEntry) adapter
						.getItem(arg2);
				if (mCurrentActionMode != null) {
					return false;
				}

				if (FileExplorerUtils.isProtected(fileListEntry
						.getPath())) {
					return false;
				}
				explorerListView.setClickable(false);
				explorerListView
						.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				QuickActionHelper.get(FileExplorerMain.this).setShowActions(false);
				mCurrentActionMode = FileExplorerMain.this
						.startActionMode(new FileActionsCallback(
								FileExplorerMain.this, fileListEntry) {

							@Override
							public void onDestroyActionMode(
									ActionMode mode) {
								view.setSelected(false);
								explorerListView
										.setChoiceMode(ListView.CHOICE_MODE_NONE);
								mCurrentActionMode = null;
								QuickActionHelper.get(FileExplorerMain.this).setShowActions(true);
								explorerListView.setClickable(true);
							}

						});

				view.setSelected(true);
				return true;
			}

		});
		registerForContextMenu(explorerListView);		
	}

	private void initRootDir(Bundle savedInstanceState) {
		// If app was restarted programmatically, find where the user last left
		// it
		String restartDirPath = getIntent().getStringExtra(
				Constants.RESTART_DIR);
		
		if (restartDirPath != null) 
		{
			File restartDir = new File(restartDirPath);
			if (restartDir.exists() && restartDir.isDirectory()) {
				currentDir = restartDir;
				getIntent().removeExtra(Constants.RESTART_DIR);
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
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	
	}

	private void listenToThemeChange() {

		listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (Constants.PREF_THEME.equals(key)) {

					shouldRestartApp = true;

				}
				if (Constants.PREF_USE_QUICKACTIONS.equals(key)) {

					shouldRestartApp = true;

				}
			}
		};

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(listener);
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

		if (prefs.useBackNavigation()) {
			if (FileExplorerUtils.isRoot(currentDir)) {
				finish();
			} else {
				gotoParent();
			}
		} else {
			super.onBackPressed();
		}

	}

	void select(File file) {
		if (FileExplorerUtils.isProtected(file)) {
			new Builder(this)
					.setTitle(getString(R.string.access_denied))
					.setMessage(
							getString(R.string.cant_open_dir, file.getName()))
					.show();
		} else if (file.isDirectory()) {
			listContents(file);
		} else {
			openFile(file);
		}
	}

	private void openFile(File file) {
		if (FileExplorerUtils.isProtected(file) || file.isDirectory()) {
			return;
		}
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				MimeTypeMap.getFileExtensionFromUrl(uri.toString()));
		intent.setDataAndType(uri, type == null ? "*/*" : type);
		startActivity((Intent.createChooser(intent,
				getString(R.string.open_using))));
	}

	public void listContents(File dir) {
		if (!dir.isDirectory() || FileExplorerUtils.isProtected(dir)) {
			return;
		}
		new Finder(this).execute(dir);
	}

	private void gotoParent() {

		if (FileExplorerUtils.isRoot(currentDir)) {
			// Do nothing finish();
		} else {
			listContents(currentDir.getParentFile());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (FileExplorerUtils.canPaste(currentDir)) {
			menu.findItem(R.id.menu_paste).setVisible(true);
		} else {
			menu.findItem(R.id.menu_paste).setVisible(false);
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

		case R.id.menu_goto:
			FileExplorerUtils.gotoPath(currentDir.getAbsolutePath(), this);
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
			Intent prefsIntent = new Intent(FileExplorerMain.this,
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
				FileExplorerUtils.getFileToPaste().getName()));

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
						new FileMover(FileExplorerMain.this, FileExplorerUtils
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
						if (FileExplorerUtils.mkDir(
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
	}

	public void refresh() {
		listContents(currentDir);
	}

	private void restartApp() {
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra(Constants.RESTART_DIR, currentDir.getAbsolutePath());
		startActivity(i);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(listener);
	}

}