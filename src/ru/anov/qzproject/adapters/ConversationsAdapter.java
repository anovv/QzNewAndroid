package ru.anov.qzproject.adapters;

import java.util.ArrayList;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.fragments.UserFragment;
import ru.anov.qzproject.models.Message;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ConversationsAdapter extends ArrayAdapter<Message>{
	private Context context;
	private Typeface tf;
	private Typeface tf2;
	private Typeface typeface;
	private String NOW;
	private String MINUTES_AGO;
	private String HOURS_AGO;
	private String DAYS_AGO;
	private String MONTHS_AGO;
	
    public ConversationsAdapter(Context context, int textViewResourceId, ArrayList<Message> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        tf2 = Typeface.createFromAsset(context.getAssets(), "Roboto-Bold.ttf");
		typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
        NOW = context.getResources().getString(R.string.now);
        MINUTES_AGO = context.getResources().getString(R.string.minutes_ago);
        HOURS_AGO = context.getResources().getString(R.string.hours_ago);
        DAYS_AGO = context.getResources().getString(R.string.days_ago);
        MONTHS_AGO = context.getResources().getString(R.string.months_ago);
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row;
    	TextView name;
    	TextView message;
    	TextView timestamp;
    	TextView messagesCounter;
    	ImageView imageView;
    	if(convertView != null){
    		row = convertView;
    		ViewHolder holder = (ViewHolder) row.getTag();
    		name = holder.name;
    		message = holder.message;
    		timestamp = holder.timestamp;
    		imageView = holder.imageView;
    		messagesCounter = holder.messagesCounter;
    	}else{
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.item_conversations, parent, false);
        	name = (TextView) row.findViewById(R.id.item_conversations_name_textview);
        	message = (TextView) row.findViewById(R.id.item_conversations_message_textview);
        	timestamp = (TextView) row.findViewById(R.id.item_conversations_timestamp_textview);
        	messagesCounter = (TextView) row.findViewById(R.id.item_conversations_counter_textview);
        	imageView = (ImageView) row.findViewById(R.id.item_conversations_imageview);
        	
        	ViewHolder holder = new ViewHolder();
        	holder.name = name;
        	holder.message = message;
        	holder.timestamp = timestamp;
        	holder.imageView = imageView;
        	holder.messagesCounter = messagesCounter;
        	row.setTag(holder);
    	}
        
        final Message msg = getItem(position);
    	
    	imageView.setImageBitmap(null);
    	name.setText(msg.getName());
    	message.setText(msg.getMessage());
    	timestamp.setText(getTime(msg.getTimestamp()));
    	
    	name.setTypeface(tf);
    	timestamp.setTypeface(typeface);
    	
    	if(msg.getThumbnailImgUrl() != null && msg.getThumbnailImgUrl().length() > 0){
    		Picasso.with(context).load(msg.getThumbnailImgUrl()).into(imageView);
    	}
    
    	imageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(msg == null){
					return;
				}
				String id = msg.getRUserId();
				if(id == null || id.length() == 0){
					return;
				}
				Intent intent = new Intent(context, BaseActivity.class);
				intent.putExtra("id", id);
				intent.putExtra("fragment", UserFragment.class.getSimpleName());
				context.startActivity(intent);
			}
    		
    	});
    	
    	SharedPreferences sp = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		if(sp.contains("message_count_" + msg.getRUserId())){
			int messageCount = sp.getInt("message_count_" + msg.getRUserId(), 0);
			if(messageCount != 0){
				messagesCounter.setText("+" + messageCount);
		    	message.setTypeface(tf2);
			}else{
	    		messagesCounter.setText("");
	        	message.setTypeface(tf);
			}
    	}else{
    		messagesCounter.setText("");
        	message.setTypeface(tf);
    	}
		
		if(msg.getType().equals("2")){
        	message.setTypeface(typeface);
		}
        return row;
    }
    
    private static class ViewHolder{
    	TextView name;
    	TextView message;
    	TextView timestamp;
    	TextView messagesCounter;
    	ImageView imageView;
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
