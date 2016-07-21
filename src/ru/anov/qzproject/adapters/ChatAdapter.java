package ru.anov.qzproject.adapters;

import java.util.ArrayList;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.fragments.UserFragment;
import ru.anov.qzproject.models.Message;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ChatAdapter extends ArrayAdapter<Message>{
	private Context context;
	
	private static int ITEM_TYPE_1 = 0;//user
	private static int ITEM_TYPE_2 = 1;//ruser
	private static int ITEM_TYPE_3 = 2;//gameresult
	
	private Typeface tf;
	private Typeface typeface;
	private String NOW;
	private String MINUTES_AGO;
	private String HOURS_AGO;
	private String DAYS_AGO;
	private String MONTHS_AGO;
	
	private String thumbnailImgUrlStr;
	private String id;
	
    public ChatAdapter(Context context, int textViewResourceId, ArrayList<Message> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
		thumbnailImgUrlStr = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_thumbnail_img_url", "");
		id = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_id", "");
		
		typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
		tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        NOW = context.getResources().getString(R.string.now);
        MINUTES_AGO = context.getResources().getString(R.string.minutes_ago);
        HOURS_AGO = context.getResources().getString(R.string.hours_ago);
        DAYS_AGO = context.getResources().getString(R.string.days_ago);
        MONTHS_AGO = context.getResources().getString(R.string.months_ago);
    }
    
    @Override
    public int getItemViewType(int position){

    	Message message = getItem(position);
    	String type = message.getType();
    	
    	return Integer.parseInt(type);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	final View row;
    	final int type = getItemViewType(position);
    	final Message item = getItem(position);
    	if(type == ITEM_TYPE_3){
    		TextView message;
    		TextView timestamp;
    		if(convertView != null){
        		row = convertView;
        		Type3Holder holder = (Type3Holder) row.getTag();
        		message = holder.message;
        		timestamp = holder.timestamp;
        	}else{
        		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	        row = inflater.inflate(R.layout.item_result_chat, parent, false);
            	message = (TextView) row.findViewById(R.id.item_result_message_textview);
            	timestamp = (TextView) row.findViewById(R.id.item_result_timestamp_textview);
            	Type3Holder holder = new Type3Holder();
            	holder.message = message;
            	holder.timestamp = timestamp;
            	row.setTag(holder);
        	}
    		
    		message.setTypeface(tf);
    		timestamp.setTypeface(typeface);
    		timestamp.setText(getTime(item.getTimestamp()));
    		message.setText(item.getMessage());
            return row;
    	}
    	TextView message;
    	TextView timestamp;
    	ImageView imageView;
    	
    	if(convertView != null){
    		row = convertView;
    		if(type == ITEM_TYPE_1){
    			Type1Holder holder = (Type1Holder) row.getTag();
    			message = holder.message;
    			timestamp = holder.timestamp;
    			imageView = holder.imageView;
    		}else{
    			Type2Holder holder = (Type2Holder) row.getTag();
    			message = holder.message;
    			timestamp = holder.timestamp;
    			imageView = holder.imageView;
    		}
    	}else{
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		if(type == ITEM_TYPE_1){
    			row = inflater.inflate(R.layout.item_user_chat, parent, false);
            	message = (TextView) row.findViewById(R.id.item_user_message_textview);
            	timestamp = (TextView) row.findViewById(R.id.item_user_timestamp_textview);
            	imageView = (ImageView) row.findViewById(R.id.item_user_imageview);
            	
            	Type1Holder holder = new Type1Holder();
            	holder.message = message;
            	holder.timestamp = timestamp;
            	holder.imageView = imageView;
            	row.setTag(holder);
    		}else{
    			row = inflater.inflate(R.layout.item_ruser_chat, parent, false);
	        	message = (TextView) row.findViewById(R.id.item_ruser_message_textview);
	        	timestamp = (TextView) row.findViewById(R.id.item_ruser_timestamp_textview);
	        	imageView = (ImageView) row.findViewById(R.id.item_ruser_imageview);
	        	
	        	Type2Holder holder = new Type2Holder();
	        	holder.message = message;
	        	holder.timestamp = timestamp;
	        	holder.imageView = imageView;
	        	row.setTag(holder);
    		}
    	}
    	
    	imageView.setImageBitmap(null);
		message.setTypeface(tf);
    	timestamp.setTypeface(typeface);
    	timestamp.setText(getTime(item.getTimestamp()));
    	message.setText(item.getMessage());
    	
    	if(type == ITEM_TYPE_1){
        	if(thumbnailImgUrlStr != null && thumbnailImgUrlStr.length() > 0){
        		Picasso.with(context).load(thumbnailImgUrlStr).into(imageView);
        	}
    	}else{
        	if(item.getThumbnailImgUrl() != null && item.getThumbnailImgUrl().length() > 0){
        		Picasso.with(context).load(item.getThumbnailImgUrl()).into(imageView);
        	}
    	}
 
    	imageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, BaseActivity.class);
				if(type == ITEM_TYPE_1){
					intent.putExtra("id", id);
					if(id.length() == 0){
						return;
					}
				}else{
					if(item.getRUserId().length() == 0){
						return;
					}
					intent.putExtra("id", item.getRUserId());
				}
				intent.putExtra("fragment", UserFragment.class.getSimpleName());
				context.startActivity(intent);
			}
    		
    	});
    	
        return row;
    }
    
    @Override
    public int getViewTypeCount() {
		return 3;
    }
    
    private static class Type1Holder{
    	TextView message;
    	TextView timestamp;
    	ImageView imageView;
    }
    
    private static class Type2Holder{
    	TextView message;
    	TextView timestamp;
    	ImageView imageView;
    }
    
    private static class Type3Holder{
    	TextView message;
    	TextView timestamp;
    }
    
    
    private String getTime(String timestamp){
    	try{
    	    long timeMillis = Long.parseLong(timestamp);
    	    long curTimeMillis = System.currentTimeMillis();
    	    
    	    long delta = curTimeMillis - timeMillis;
    	    if(delta < 0){
    	    	return "";
    	    }
    	    
    	    int minutes = (int)(delta/(60*1000));
    	    
    	    int hours = (int)(delta/(60*60*1000));
    	    
    	    int days = (int)((delta)/(24*60*60*1000));
    	    
    	    int months = (int)((delta)/(30*24*60*60*1000));
    	    
    	    if(months > 0){
    	    	return months + MONTHS_AGO;
    	    }
    	    
    	    if(days > 0){
    	    	return days + DAYS_AGO;
    	    }
    	    
    	    if(hours > 0){
    	    	return hours + HOURS_AGO;
    	    }
    	    
    	    if(minutes > 0){
    	    	return minutes + MINUTES_AGO;
    	    }

	    	return NOW;
    	    
    	}catch(Exception e){
    		return "";
    	}
    }
}
