package ru.anov.qzproject.adapters;

import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.models.Friend;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class UserlistAdapter extends ArrayAdapter<Friend>{

	private Context context;
	private Typeface tf;
	
	private static final int ITEM_TYPE_1 = 0;//card top
	private static final int ITEM_TYPE_2 = 1;//card mid
	private static final int ITEM_TYPE_3 = 2;//card bot
	private static final int ITEM_TYPE_4 = 3;//card
	
    public UserlistAdapter(Context context, int textViewResourceId, List<Friend> objects) {
    	super(context, textViewResourceId, objects);
        this.context = context;
        tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View row;
    	TextView name;
    	ImageView imageView;
    	
    	if(convertView != null){
    		row = convertView;
    		ViewHolder holder = (ViewHolder) row.getTag();
    		name = holder.name;
    		imageView = holder.imageView;
    	}else{
	    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	    	int type = getItemViewType(position);
	    	if(type == ITEM_TYPE_1){
		    	row = inflater.inflate(R.layout.item_userlist_1, parent, false);
	    	}else if(type == ITEM_TYPE_2){
		    	row = inflater.inflate(R.layout.item_userlist_2, parent, false);
	    	}else if(type == ITEM_TYPE_3){
		    	row = inflater.inflate(R.layout.item_userlist_3, parent, false);
	    	}else{
		    	row = inflater.inflate(R.layout.item_userlist_4, parent, false);
	    	}
	    	name = (TextView) row.findViewById(R.id.item_userlist_name_textview);
	    	imageView = (ImageView) row.findViewById(R.id.item_userlist_imageview);

        	ViewHolder holder = new ViewHolder();
        	holder.name = name;
        	holder.imageView = imageView;
        	row.setTag(holder);
    	}
        
        Friend friend = getItem(position);
    	
    	imageView.setImageBitmap(null);
    	name.setText(friend.getName());
    	name.setTypeface(tf);
    	
    	if(friend.getThumbnailImgUrl().length() > 0){
    		Picasso.with(context).load(friend.getThumbnailImgUrl()).into(imageView);
    	}
        return row;
    }
    
    private static class ViewHolder{
    	TextView name;
    	ImageView imageView;
    }
    
    @Override
    public int getViewTypeCount() {
		return 4;
    }
    
    @Override
    public int getItemViewType(int position) {
    	if(getCount() == 1){
    		return ITEM_TYPE_4;
    	}else{
    		if(position == 0){
    			return ITEM_TYPE_1;
    		}else if(position == getCount() - 1){
    			return ITEM_TYPE_3;
    		}else{
    			return ITEM_TYPE_2;
    		}
    	}
    }
}
