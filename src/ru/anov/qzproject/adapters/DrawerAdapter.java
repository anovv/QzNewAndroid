package ru.anov.qzproject.adapters;

import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.MainActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends ArrayAdapter<String>{
	private Context context;
	private int delta = 0;
	
    public DrawerAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }
    
    public void setFriendsDelta(int delta){
    	if(delta < 0){
    		this.delta = 0;
    	}else{
    		this.delta = delta;
    	}
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row = null;
    	
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String item = getItem(position);
        
        if(position == 2){
    		if(position == MainActivity.selectedItemPosition){
            	row = inflater.inflate(R.layout.item_drawer_friends_selected, null);
            }else{
            	row = inflater.inflate(R.layout.item_drawer_friends_normal, null);
            }
    		TextView name = (TextView) row.findViewById(R.id.item_drawer_friends_textview);
        	ImageView pic = (ImageView) row.findViewById(R.id.item_drawer_friends_imageview);
        	TextView friendsCountTextView = (TextView) row.findViewById(R.id.item_drawer_friends_count_textview);
        	name.setText(item);
        	if(delta <= 0){
        		friendsCountTextView.setText("");
        	}else{
        		friendsCountTextView.setText("+" + delta);
        	}
        	
        	if(position == MainActivity.selectedItemPosition){
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_friends_icon_selected));
            }else{
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_friends_icon));
            }
    		
    		return row;
    	}
        

    	if(position == 3){
    		if(position == MainActivity.selectedItemPosition){
            	row = inflater.inflate(R.layout.item_drawer_friends_selected, null);
            }else{
            	row = inflater.inflate(R.layout.item_drawer_friends_normal, null);
            }
    		
    		TextView name = (TextView) row.findViewById(R.id.item_drawer_friends_textview);
        	ImageView pic = (ImageView) row.findViewById(R.id.item_drawer_friends_imageview);
        	
        	TextView friendsCountTextView = (TextView) row.findViewById(R.id.item_drawer_friends_count_textview);
        	name.setText(item);
        	
			int messagesCount = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getInt("message_count", 0);
			
        	if(messagesCount <= 0){
        		friendsCountTextView.setText("");
	    	}else{
	    		friendsCountTextView.setText("+" + messagesCount);
	    	}
        	
    		
    		if(position == MainActivity.selectedItemPosition){
	    		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_message_icon_selected));
			}else{
	    		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_message_icon));
			}
    		
    		return row;
    	}
        
        if(position == MainActivity.selectedItemPosition){
        	row = inflater.inflate(R.layout.item_drawer_selected, null);
        }else{
        	row = inflater.inflate(R.layout.item_drawer_normal, null);
        }
        
    	TextView name = (TextView) row.findViewById(R.id.item_drawer_textview);
    	ImageView pic = (ImageView) row.findViewById(R.id.item_drawer_imageview);
    	name.setText(item);
    	if(position == 0){
    		if(position == MainActivity.selectedItemPosition){
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_home_icon_selected));
    		}else{
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_home_icon));
    		}
    	}
    	
    	if(position == 1){
    		if(position == MainActivity.selectedItemPosition){
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_profile_icon_selected));
    		}else{
        		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_profile_icon));
    		}
    	}
    	
    	if(position == 4){
    		if(position == MainActivity.selectedItemPosition){
    			pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_themes_icon_selected));
    		}else{
    			pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_themes_icon));
    		}
    	}
    	
    	if(position == 5){
    		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_shop_icon));
    	}
    	
    	if(position == 6){
    		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_new));
    	}
    	
    	if(position == 7){
    		pic.setImageDrawable(context.getResources().getDrawable(R.drawable.drawer_setting_icon));
    	}
    	
    	
        return row;
    }
}
