package net.appositedesigns.fileexplorer.model;

import java.util.List;

public class FileListing {

	
	private List<FileListEntry> children;
	private boolean isExcludeFromMedia = false;
	public List<FileListEntry> getChildren() {
		return children;
	}
	public void setChildren(List<FileListEntry> children) {
		this.children = children;
	}
	public boolean isExcludeFromMedia() {
		return isExcludeFromMedia;
	}
	public void setExcludeFromMedia(boolean isExcludeFromMedia) {
		this.isExcludeFromMedia = isExcludeFromMedia;
	}
	public FileListing(List<FileListEntry> children) {
		super();
		this.children = children;
	}
	
	
}
