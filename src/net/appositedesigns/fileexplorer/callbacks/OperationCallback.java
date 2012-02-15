package net.appositedesigns.fileexplorer.callbacks;

public interface OperationCallback<T> {

	T onSuccess();
	void onFailure(Throwable e);
}
