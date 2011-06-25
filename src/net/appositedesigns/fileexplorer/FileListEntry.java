package net.appositedesigns.fileexplorer;

import java.io.File;

public class FileListEntry {

	private File path;
	private String name;
	private String meta;
	
	public FileListEntry(String fqpath) {
		
		path = new File(fqpath);
		name = path.getName();
		meta = "";
	}
	
	public String getMeta() {
		return meta;
	}
	public void setMeta(String meta) {
		this.meta = meta;
	}
	public FileListEntry() {
		// TODO Auto-generated constructor stub
	}
	public File getPath() {
		return path;
	}
	public void setPath(File path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
}
