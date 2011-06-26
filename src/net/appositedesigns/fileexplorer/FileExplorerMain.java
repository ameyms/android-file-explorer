package net.appositedesigns.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FileExplorerMain extends Activity {


	private ListView explorerListView;
	private File currentDir;
	private List<FileListEntry> files;
	private FileListAdapter adapter;
	private ProgressDialog waitDialog;
	
	private ProgressDialog moveProgressDialog;
	
	public FileExplorerMain() {

		currentDir = new File("/");
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        files = new ArrayList<FileListEntry>();
        
        explorerListView = (ListView)findViewById(R.id.mainExplorer_list);
        
        adapter = new FileListAdapter(this, files);
        explorerListView.setAdapter(adapter);

        listContents(currentDir);
        explorerListView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
            	FileListEntry file = (FileListEntry)explorerListView.getAdapter().getItem(position);
                 select(file.getPath());
            }

         });

    }

    void select(File file)
    {
        if(file.isDirectory() && !FileExplorerUtils.isProtected(file))
        {
       	 listContents(file);
        }
        else
        {
       	 openFile(file);
        }
    }
	private void openFile(File file) {
		
		if(file.getName().endsWith(".apk"))
		{
			Uri fileUri = Uri.fromFile(file); 
			Intent installationIntent = new Intent(Intent.ACTION_VIEW).setDataAndType(fileUri,"application/vnd.android.package-archive");
			startActivity(installationIntent);
		}
		else if(FileExplorerUtils.isMusic(file))
		{
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
			startActivity(intent);
		}
		else if(FileExplorerUtils.isPicture(file))
		{
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "image/*");
			startActivity(intent);
		}
		else if(file.getName().endsWith(".html"))
		{
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file), "text/html");
			startActivity(intent);
		}
	}
    void listContents(File dir)
    {
    	if(!dir.isDirectory() || FileExplorerUtils.isProtected(dir))
    	{
    		return;
    	}
    	new Finder().execute(dir);
	}

    @Override
    public void onBackPressed() {
    	
    	if(FileExplorerUtils.isRoot(currentDir))
    	{
    		super.onBackPressed();
    	}
    	else
    	{
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

    	 if(FileExplorerUtils.canPaste(currentDir))
         {
    		 menu.findItem(R.id.menu_paste).setVisible(true);
         }
    	 else
    	 {
    		 menu.findItem(R.id.menu_paste).setVisible(false);
    	 }
    	return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	
    	switch (item.getItemId()) {
		case  R.id.menu_paste:
			confirmPaste();
			return true;

		case R.id.menu_newfolder:
			confirmCreateFolder();
        	return true;
        	
		default:
			super.onOptionsItemSelected(item);
			break;
		}
        
        return true;
    }
    
	private void confirmPaste() {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Confirm");
		alert.setMessage("Are you sure you want to paste "+FileExplorerUtils.getFileToPaste().getName()+"?");
		alert.setIcon(android.R.drawable.ic_dialog_alert);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		 
				dialog.dismiss();
				new FileMover(FileExplorerUtils.getPasteMode()).execute(currentDir);
			}
		});

		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
		  }
		});

		alert.show();
		
		
	}
	private void confirmCreateFolder() {
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Create Folder");
		alert.setIcon(android.R.drawable.ic_dialog_info);
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		input.setHint("Enter folder name");
		input.setSingleLine();
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  CharSequence newDir = input.getText();
		  if(FileExplorerUtils.mkDir(currentDir.getAbsolutePath(), newDir))
		  {
			  listContents(currentDir);
		  }
		  }
		});

		alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		   
			  dialog.dismiss();
		  }
		});

		alert.show();
		
	}


	void deletePath(File path)
	{
		waitDialog = ProgressDialog.show(FileExplorerMain.this, "", 
                "Deleting "+path.getName()+"...", true);
		new Trasher().execute(path);
	}
	
	private class Finder extends AsyncTask<File, Integer, List<FileListEntry>>
	{

		@Override
		protected void onPreExecute() {
			waitDialog = ProgressDialog.show(FileExplorerMain.this, "", 
	                "Querying file system. Please wait...", true);
		
		}
		
		@Override
		protected void onPostExecute(List<FileListEntry> result) {

			final List<FileListEntry> childFiles = result;
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					waitDialog.dismiss();
					files.clear();
					files.addAll(childFiles);
					adapter.notifyDataSetChanged();					
				}
			});
		
		}
		@Override
		protected List<FileListEntry> doInBackground(File... params) {
			File root = params[0];
			String[] children = root.list();
			List<FileListEntry> childFiles = new ArrayList<FileListEntry>();
			
			for(String fileName : children)
			{
				File f = new File(root.getAbsolutePath()+File.separator+fileName);
				
				String fname = f.getName();
				
				FileListEntry child = new FileListEntry();
				child.setName(fname);
				child.setPath(f);
				child.setSize(f.length());
				child.setLastModified(new Date(f.lastModified()));
				childFiles.add(child);
			}
			
			currentDir = root;
			return childFiles;
		}
	}

	private class Trasher extends AsyncTask<File, Integer, Boolean>
	{
		
		private File fileToBeDeleted;
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						waitDialog.dismiss();
						Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG);
						listContents(currentDir);
					}
				});
			}
			else
			{
				FileExplorerUtils.setPasteSrcFile(fileToBeDeleted, FileExplorerUtils.getPasteMode());
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						waitDialog.dismiss();
						AlertDialog alert = new Builder(FileExplorerMain.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Error")
						.setMessage(fileToBeDeleted.getName()+" could not be deleted")
						.show();
						
						
					}
				});
			}
		}
		@Override
		protected Boolean doInBackground(File... params) {
			
			fileToBeDeleted = params[0];
			try
			{
				if(FileExplorerUtils.getFileToPaste().getCanonicalPath().equalsIgnoreCase(fileToBeDeleted.getCanonicalPath()))
				{
					FileExplorerUtils.setPasteSrcFile(null, FileExplorerUtils.getPasteMode());
				}
				return FileExplorerUtils.delete(fileToBeDeleted);
			}
			catch (Exception e) {
				return false;
			}
			
		}
	}
	
	private class FileMover extends AsyncTask<File, Integer, Boolean>
	{
		private int mode= 1;
		private AbortionFlag flag;
		public FileMover(int mode) {
			this.mode =mode;
			flag = new AbortionFlag();
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
			{
				if(mode==FileExplorerUtils.PASTE_MODE_MOVE)
				{
					FileExplorerUtils.setPasteSrcFile(null, 0);
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(moveProgressDialog.isShowing())
						{
							moveProgressDialog.dismiss();
						}
						if(mode==FileExplorerUtils.PASTE_MODE_COPY)
						{
							Toast.makeText(getApplicationContext(), "Copy complete ", Toast.LENGTH_LONG);
						}
						else
						{
							Toast.makeText(getApplicationContext(), "Resource has been moved ", Toast.LENGTH_LONG);
						}
						listContents(currentDir);
					}
				});
			}
			else
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(moveProgressDialog.isShowing())
						{
							moveProgressDialog.dismiss();
						}
						Toast.makeText(getApplicationContext(), "Could not perform operation", Toast.LENGTH_LONG);
					}
				});
			}
		}
		@Override
		protected Boolean doInBackground(File... params) {
			
			File destDir = params[0];
			return FileExplorerUtils.paste(mode, destDir, flag);
			
		}
		
		@Override
		protected void onPreExecute() {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					String message =  "Copying "+FileExplorerUtils.getFileToPaste().getName()+". Please wait...";
					if(mode==FileExplorerUtils.PASTE_MODE_MOVE)
					{
						message = 
				                "Moving "+FileExplorerUtils.getFileToPaste().getName()+". Please wait...";
					}
					moveProgressDialog = new ProgressDialog(FileExplorerMain.this);
					moveProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					moveProgressDialog.setMessage(message);
					moveProgressDialog.setButton("Run in Background", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dialog.dismiss();
							
						}
					});
					moveProgressDialog.setButton2("Cancel", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							dialog.dismiss();
							FileMover.this.flag.abort();
						}
					});
					moveProgressDialog.show();
					
				}
			});
		}
	}
}