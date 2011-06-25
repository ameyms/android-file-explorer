package net.appositedesigns.fileexplorer;

public interface OperationCallback<T> {

	T onSuccess();
	void onFailure(Throwable e);
}
