package net.appositedesigns.fileexplorer;

import java.io.File;

import android.os.FileObserver;

public class DirWatcher extends FileObserver {

	File dir;
	FileExplorerMain context;
	

	public DirWatcher(File dir, FileExplorerMain context) {
		super(dir.getAbsolutePath(), 
		CLOSE_WRITE | CREATE | DELETE | MODIFY | MOVED_FROM | MOVED_TO | OPEN);
		this.dir = dir;
		this.context = context;
	}

	@Override
	public void onEvent(int event, String path) {
		
		context.listContents(dir);
		
	}

}
