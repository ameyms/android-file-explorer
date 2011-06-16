package net.appositedesigns.fileexplorer;

import greendroid.app.GDActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FileExplorerMain extends GDActivity {


	private ListView explorerListView;
	private File currentDir;
	private List<File> files;
	private FileListAdapter adapter;
	private ProgressDialog waitDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        files = new ArrayList<File>();
        
        explorerListView = (ListView)findViewById(R.id.mainExplorer_list);
        
        adapter = new FileListAdapter(getApplicationContext(), files);
        explorerListView.setAdapter(adapter);

        listContents(new File("/"));
        explorerListView.setOnItemClickListener(new OnItemClickListener() {
        	
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	
                 File file = (File)explorerListView.getAdapter().getItem(position);
                 if(file.isDirectory() && !FileExplorerUtils.isProtected(file))
                 {
                	 listContents(file);
                 }
                 else
                 {
                	 openFile(file);
                 }
            }

         });
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
    private void listContents(File dir)
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
	public void share(File resource) {
    	final Intent intent = new Intent(Intent.ACTION_SEND);

    	intent.setType("application/x-octet-stream");
    	intent.putExtra(Intent.EXTRA_STREAM, resource);

    	startActivity(Intent.createChooser(intent,
    	"Send via"));
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