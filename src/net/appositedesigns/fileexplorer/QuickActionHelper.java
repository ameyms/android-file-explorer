package net.appositedesigns.fileexplorer;

import greendroid.widget.QuickAction;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

public final class QuickActionHelper {

	private QuickActionWidget mBar;

	private Context mContext;

	private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
            Toast.makeText(mContext, "Item " + position + " clicked", Toast.LENGTH_SHORT).show();
        }
    };
    
	public QuickActionHelper(Context mContext) {
		super();
		this.mContext = mContext;
		prepareQuickActionBar();
	}

	 public void onShowBar(View v) {
	        mBar.show(v);
	 }
	 
	private void prepareQuickActionBar() {
		
		mBar = new QuickActionBar(mContext);
		
		mBar.addQuickAction(new MyQuickAction(mContext,
				com.cyrilmottier.android.greendroid.R.drawable.gd_action_bar_slideshow, R.string.action_copy));
		
		mBar.addQuickAction(new MyQuickAction(mContext,
				com.cyrilmottier.android.greendroid.R.drawable.gd_action_bar_export, R.string.action_move));
		
		mBar.addQuickAction(new MyQuickAction(mContext,
				com.cyrilmottier.android.greendroid.R.drawable.gd_action_bar_edit, R.string.action_rename));
		
		mBar.addQuickAction(new MyQuickAction(mContext,
				com.cyrilmottier.android.greendroid.R.drawable.gd_action_bar_trashcan, R.string.action_delete));
		
		mBar.addQuickAction(new MyQuickAction(mContext,
				com.cyrilmottier.android.greendroid.R.drawable.gd_action_bar_info, R.string.action_prop));

		mBar.setOnQuickActionClickListener(mActionListener);
	}
	
	 private static class MyQuickAction extends QuickAction {
	        
	        private static final ColorFilter BLACK_CF = new LightingColorFilter(Color.BLACK, Color.BLACK);

	        public MyQuickAction(Context ctx, int drawableId, int titleId) {
	            super(ctx, buildDrawable(ctx, drawableId), titleId);
	        }
	        
	        private static Drawable buildDrawable(Context ctx, int drawableId) {
	            Drawable d = ctx.getResources().getDrawable(drawableId);
	            d.setColorFilter(BLACK_CF);
	            return d;
	        }
	        
	    }
}
