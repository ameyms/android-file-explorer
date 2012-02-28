package net.appositedesigns.fileexplorer.adapters;

import java.util.List;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.activity.BookmarkListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.util.Util;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookmarkListAdapter extends BaseAdapter {

	public static class ViewHolder 
	{
	  public TextView resName;
	  public ImageView resIcon;
	  public TextView resMeta;
	}

	private static final String TAG = BookmarkListAdapter.class.getName();
	  
	private BookmarkListActivity mContext;
	private List<FileListEntry> files;
	private LayoutInflater mInflater;
	
	public BookmarkListAdapter(BookmarkListActivity context, List<FileListEntry> files) {
		super();
		mContext = context;
		this.files = files;
		mInflater = mContext.getLayoutInflater();
		
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

	public List<FileListEntry> getItems()
	{
	  return files;
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
        	convertView = mInflater.inflate(R.layout.bookmark_list_item, parent, false);
            holder = new ViewHolder();
            holder.resName = (TextView)convertView.findViewById(R.id.explorer_resName);
            holder.resMeta = (TextView)convertView.findViewById(R.id.explorer_resMeta);
            holder.resIcon = (ImageView)convertView.findViewById(R.id.explorer_resIcon);
            convertView.setTag(holder);
        } 
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        final FileListEntry currentFile = files.get(position);
        holder.resName.setText(currentFile.getName());
        if(Util.isRoot(currentFile.getPath()))
        {
        	holder.resName.setText(mContext.getString(R.string.filesystem_root));
        }
        holder.resIcon.setImageDrawable(Util.getIcon(mContext, currentFile.getPath()));
        holder.resMeta.setText(currentFile.getPath().getAbsolutePath());
        
        return convertView;
	}

}
