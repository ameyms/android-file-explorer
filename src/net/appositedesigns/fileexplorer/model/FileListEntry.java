package net.appositedesigns.fileexplorer.model;

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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileListEntry other = (FileListEntry) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	
	
}
