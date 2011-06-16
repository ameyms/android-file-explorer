package net.appositedesigns.fileexplorer;

import greendroid.app.GDApplication;

public class FileExplorerApp extends GDApplication {

	public FileExplorerApp() {}
	
    @Override
    public Class<?> getHomeActivityClass() {
        return FileExplorerMain.class;
    }

}
