package net.appositedesigns.fileexplorer;

import java.io.File;
import java.util.Date;

public class FileListEntry {

	private File path;
	private String name;
	private long size;
	private Date lastModified;
	
	public FileListEntry(String fqpath) {
		
		path = new File(fqpath);
		name = path.getName();
		size = 0;
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


	public long getSize() {
		return size;
	}


	public void setSize(long size) {
		this.size = size;
	}


	public Date getLastModified() {
		return lastModified;
	}


	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	
}
