package net.appositedesigns.fileexplorer;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FileExplorerMain extends Activity {


	private ListView explorerListView;
	private File currentDir;
	private List<File> files;
	private FileListAdapter adapter;
	private ProgressDialog waitDialog;
	
	private DirWatcher watchDog;
	
	public FileExplorerMain() {

		currentDir = new File("/");
        watchDog = new DirWatcher(currentDir, this);
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        files = new ArrayList<File>();
        
        explorerListView = (ListView)findViewById(R.id.mainExplorer_list);
        
        adapter = new FileListAdapter(this, files);
        explorerListView.setAdapter(adapter);

        listContents(currentDir);
        watchDog.startWatching();
        explorerListView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
                 File file = (File)explorerListView.getAdapter().getItem(position);
                 select(file);
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
	void share(File resource) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
	
		try {
			intent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resource.toURL().toString())));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			intent.setType("application/x-octet-stream");
		}
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, resource);
	
		startActivity(Intent.createChooser(intent,"Send via"));
	
	}

	void deletePath(File path)
	{
		AsyncTask<File, Integer, Boolean> deletionTask = new AsyncTask<File, Integer, Boolean>(){
			
			protected void onPostExecute(Boolean result) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(FileExplorerMain.this);
				builder.setMessage(R.string.msg_delete_success)
				       .setCancelable(true).setTitle(R.string.msg_delete_success).setIcon(android.R.drawable.ic_dialog_info);
				
				if(!result)
				{
					builder = new AlertDialog.Builder(FileExplorerMain.this);
					builder.setMessage(R.string.msg_delete_fail)
					       .setCancelable(true).setTitle(R.string.msg_delete_fail).setIcon(android.R.drawable.ic_dialog_alert);
				}
				
				AlertDialog alert = builder.create();
				alert.show();
			}
			@Override
			protected Boolean doInBackground(File... params) {
				
				waitDialog = ProgressDialog.show(FileExplorerMain.this, "", 
		                "Querying file system. Please wait...", true);
				
				File fileToBeDeleted = params[0];
				
				if(fileToBeDeleted==null || !fileToBeDeleted.exists())
				{
					return false;
				}
				
				try {
					return FileExplorerUtils.delete(fileToBeDeleted);
				} catch (Exception e) {
					return false;
				}
			}
		}.execute(path);
	}
	
	private class Finder extends AsyncTask<File, Integer, List<File>>
	{

		@Override
		protected void onPreExecute() {
			waitDialog = ProgressDialog.show(FileExplorerMain.this, "", 
	                "Querying file system. Please wait...", true);
		
		}
		
		@Override
		protected void onPostExecute(List<File> result) {

			final List<File> childFiles = result;
			
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
		protected List<File> doInBackground(File... params) {
			File root = params[0];
			String[] children = root.list();
			List<File> childFiles = new ArrayList<File>();
			
			for(String fileName : children)
			{
				childFiles.add(new File(root.getAbsolutePath()+File.separator+fileName));
			}
			
			currentDir = root;
			return childFiles;
		}
	}
	
}