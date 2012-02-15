package net.appositedesigns.fileexplorer.adapters;

import java.util.ArrayList;
import java.util.List;

import net.appositedesigns.fileexplorer.R;
import net.appositedesigns.fileexplorer.R.id;
import net.appositedesigns.fileexplorer.R.layout;
import net.appositedesigns.fileexplorer.activity.FileListActivity;
import net.appositedesigns.fileexplorer.model.FileListEntry;
import net.appositedesigns.fileexplorer.quickactions.QuickActionHelper;
import net.appositedesigns.fileexplorer.util.Util;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter implements Filterable {

	public static class ViewHolder 
	{
	  public TextView resName;
	  public ImageView resIcon;
	  public ImageView resActions;
	  public TextView resMeta;
	}

	private static final String TAG = FileListAdapter.class.getName();
	  
	private FileListActivity mContext;
	private List<FileListEntry> files;
	private LayoutInflater mInflater;
	
	public FileListAdapter(FileListActivity context, List<FileListEntry> files) {
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
        final FileListEntry currentFile = files.get(position);
        holder.resName.setText(currentFile.getName());
        holder.resIcon.setImageDrawable(Util.getIcon(mContext, currentFile.getPath()));
        String meta = Util.prepareMeta(currentFile, mContext);
        holder.resMeta.setText(meta);
        if(!Util.canShowQuickActions(currentFile, mContext))
        {
        	holder.resActions.setVisibility(View.INVISIBLE);
        }
        else
        {
        	holder.resActions.setVisibility(View.VISIBLE);
        	holder.resActions.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					QuickActionHelper helper = QuickActionHelper.get(mContext);
					helper.showQuickActions((ImageView)v, currentFile);

				}
			});
        }
        
        return convertView;
	}


	@Override
	public Filter getFilter() {
		
		return new Filter(){

			@Override
			protected FilterResults performFiltering(CharSequence prefix) {
				
                FilterResults results = new FilterResults();
                String prefixString = prefix.toString().toLowerCase();

                final List<FileListEntry> values = files;
                final int count = values.size();

                final ArrayList<FileListEntry> newValues = new ArrayList<FileListEntry>(count);

                for (int i = 0; i < count; i++) {
                    final FileListEntry value = values.get(i);
                    final String valueText = value.getName()
                            .toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();

                return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				
				 files = (List<FileListEntry>) results.values;
                 if (results.count > 0) {
                     notifyDataSetChanged();
                 } else {
                     notifyDataSetInvalidated();
                 }
			}
			
		};
	}


}
