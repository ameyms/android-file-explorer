package net.appositedesigns.fileexplorer;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {

	public static class ViewHolder 
	{
	  public TextView resName;
	  public ImageView resIcon;
	  public ImageView resActions;
	  public TextView resMeta;
	}
	  
	private FileExplorerMain mContext;
	private List<File> files;
	private LayoutInflater mInflater;
	 
	public FileListAdapter(FileExplorerMain context, List<File> files) {
		super();
		mContext = context;
		this.files = files;
		mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if(files == null)
		{
			return 0;
		}
		else
		{
			return files.size();
		}
	}

	@Override
	public Object getItem(int arg0) {

		if(files == null)
			return null;
		else
			return files.get(arg0);
	}

	@Override
	public long getItemId(int position) {

		return position;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.explorer_item, parent, false);
            holder = new ViewHolder();
            holder.resName = (TextView)convertView.findViewById(R.id.explorer_resName);
            holder.resMeta = (TextView)convertView.findViewById(R.id.explorer_resMeta);
            holder.resIcon = (ImageView)convertView.findViewById(R.id.explorer_resIcon);
            holder.resActions = (ImageView)convertView.findViewById(R.id.explorer_resActions);
            convertView.setTag(holder);
        } 
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final File currentFile = files.get(position);
        holder.resName.setText(currentFile.getName());
        holder.resIcon.setImageDrawable(FileExplorerUtils.getIcon(mContext, currentFile));
        holder.resMeta.setText(FileExplorerUtils.getSize(currentFile.length()));
        if(FileExplorerUtils.isProtected(currentFile))
        {
        	holder.resActions.setVisibility(View.INVISIBLE);
        }
        else
        {
        	holder.resActions.setVisibility(View.VISIBLE);
        	holder.resActions.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					QuickActionHelper helper = new QuickActionHelper(mContext);
					helper.showQuickActions(v, currentFile);

				}
			});
        }
        return convertView;
	}


}
