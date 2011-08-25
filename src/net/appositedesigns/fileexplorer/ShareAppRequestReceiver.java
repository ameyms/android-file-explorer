package net.appositedesigns.fileexplorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShareAppRequestReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		
		final Intent intent = new Intent(Intent.ACTION_SEND);
	   	intent.setType("text/plain");
    	String text = context.getString(R.string.share_msg, "https://market.android.com/details?id=net.appositedesigns.fileexplorer");
    	intent.putExtra(Intent.EXTRA_TEXT, text);
    	intent.putExtra(Intent.EXTRA_SUBJECT, "FileExplorer");

    	context.startActivity(Intent.createChooser(intent, 	context.getString(R.string.share_via)));
	}

}
