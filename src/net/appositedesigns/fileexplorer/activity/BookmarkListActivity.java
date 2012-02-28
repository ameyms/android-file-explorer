package net.appositedesigns.fileexplorer.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.appositedesigns.fileexplorer.FileExplorerApp;
import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.adapters.BookmarkListAdapter;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.PreferenceUtil;
import net.appositedesigns.fileexplorer.workers.BookmarkLoader;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkListActivity extends BaseFileListActivity {

	private BookmarkListAdapter adapter;
	private ArrayList<FileListEntry> bookmarks;
	private ListView bookmarkListView;
	private boolean isPicker = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		isPicker = getIntent().getBooleanExtra(FileExplorerApp.EXTRA_IS_PICKER, false);
		bookmarks = new ArrayList<FileListEntry>();
		initBookmarksList();
		new BookmarkLoader(this).execute();
		bookmarkListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				return false;
			}
			
			
		});
		registerForContextMenu(bookmarkListView);	

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(shouldRestartApp)
		{
			Intent intent = new Intent();
			intent.setAction(FileExplorerApp.ACTION_OPEN_BOOKMARK);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(FileExplorerApp.EXTRA_IS_PICKER, isPicker);
			startActivity(intent);
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(!isPicker)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.bookmarks_options_menu, menu);
			return true;
		}
		return false;
	}

	private void initBookmarksList() {
		bookmarkListView = (ListView) getListView();
		adapter = new BookmarkListAdapter(this, bookmarks);
		bookmarkListView.setAdapter(adapter);
		bookmarkListView.setTextFilterEnabled(true);
		bookmarkListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (bookmarkListView.isClickable()) {
					FileListEntry file = (FileListEntry) bookmarkListView
							.getAdapter().getItem(position);
					select(file.getPath());
				}
			}

		});

	}
	protected void select(File path) {
		Intent intent = new Intent();
		intent.setAction(FileExplorerApp.ACTION_OPEN_FOLDER);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.putExtra(PreferenceUtil.KEY_RESTART_DIR, path.getAbsolutePath());
		intent.putExtra(FileExplorerApp.EXTRA_IS_PICKER, isPicker);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(R.id.menu_settings == item.getItemId())
		{
			Intent prefsIntent = new Intent(this,
					SettingsActivity.class);
			startActivity(prefsIntent);
			return true;
		}
		
		return false;
	}

	public void setBookmarks(List<FileListEntry> childFiles) {
		TextView emptyText = (TextView) findViewById(android.R.id.empty);
		if (emptyText != null) {
			emptyText.setText(R.string.no_bookmarks);
		}
		bookmarks.clear();
		bookmarks.addAll(childFiles);
		adapter.notifyDataSetChanged();		
	}
}
