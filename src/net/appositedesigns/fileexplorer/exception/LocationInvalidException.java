package net.appositedesigns.fileexplorer.exception;

public class LocationInvalidException extends Exception {

	private static final long serialVersionUID = -4046926680600982016L;
	
	private String location;

	public LocationInvalidException(String location) {
		super();
		this.location = location;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
}
