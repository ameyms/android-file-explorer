package net.appositedesigns.fileexplorer.util;

public interface OperationCallback<T> {

	T onSuccess();
	void onFailure(Throwable e);
}
