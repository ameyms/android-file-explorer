package net.appositedesigns.fileexplorer;

public class AbortionFlag {

	private boolean aborted = false;
	
	synchronized void abort()
	{
		aborted = true;
	}
	synchronized boolean isAborted()
	{
		return aborted;
	}
}
