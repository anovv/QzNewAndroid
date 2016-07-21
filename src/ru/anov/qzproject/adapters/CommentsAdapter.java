package ru.anov.qzproject.adapters;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.fragments.UserFragment;
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

public class CommentsAdapter extends ArrayAdapter<Map<String, String>>{
	
	private Context context;
	private Typeface tf;
	private Typeface typeface;
	private String NOW;
	private String MINUTES_AGO;
	private String HOURS_AGO;
	private String DAYS_AGO;
	private String MONTHS_AGO;
	
	public CommentsAdapter(Context context, int textViewResourceId, List<Map<String, String>> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
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
    	TextView comment;
    	TextView date;
    	TextView best_in;
    	ImageView imageView;
    	if(convertView != null){
    		row = convertView;
    		ViewHolder holder = (ViewHolder) row.getTag();
    		name = holder.name;
    		comment = holder.comment;
    		date = holder.date;
    		best_in = holder.best_in;
    		imageView = holder.imageView;
    	}else{
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.item_comment, parent, false);
        	name = (TextView) row.findViewById(R.id.item_comment_name_textview);
        	comment = (TextView) row.findViewById(R.id.item_comment_textview);
        	date = (TextView) row.findViewById(R.id.item_comment_date_textview);
        	best_in = (TextView) row.findViewById(R.id.item_comment_best_in_textview);
        	imageView = (ImageView) row.findViewById(R.id.item_comment_imageview);
        	
        	ViewHolder holder = new ViewHolder();
        	holder.name = name;
        	holder.comment = comment;
        	holder.date = date;
        	holder.best_in = best_in;
        	holder.imageView = imageView;
        	row.setTag(holder);
    	}
        
        Map<String, String> map = getItem(position);
    	final String userId = map.get("user_id");
    	imageView.setImageBitmap(null);
    	name.setText(map.get("name"));
    	comment.setText(map.get("comment"));
    	if(map.get("timestamp").equals(NOW)){
    		date.setText(NOW);
    	}else{
    		date.setText(getDate(map.get("timestamp")));
    	}
    	
    	if(map.containsKey("best_in") && map.get("best_in").trim().length() != 0 && !map.get("best_in").equals("null")){
    		best_in.setText(map.get("best_in"));
    	}else{
    		best_in.setText("");
    	}
    	
    	name.setTypeface(tf);
    	comment.setTypeface(tf);
    	date.setTypeface(tf);
    	best_in.setTypeface(typeface);
    	if(map.get("thumbnail_img_url").length() > 0){
    		Picasso.with(context).load(map.get("thumbnail_img_url")).into(imageView);
    	}
    	imageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, BaseActivity.class);
				intent.putExtra("id", userId);
				intent.putExtra("fragment", UserFragment.class.getSimpleName());
				context.startActivity(intent);
			}
    		
    	});
        return row;
    }
    
    private static class ViewHolder{
    	TextView name;
    	TextView comment;
    	TextView date;
    	TextView best_in;
    	ImageView imageView;
    }

    private String getDate(String timestamp){
    	try{
    	    long timeMillis = Long.parseLong(timestamp);
    	    //long curTimeMillis = System.currentTimeMillis();
    	    
    	    Date currentDate = new Date();
    	    long curTimeMillis = currentDate.getTime();
    	    
    	    long delta = curTimeMillis - timeMillis;
    	    if(delta < 0){
    	    	if(Math.abs(delta) < 60*1000){
    	    		return NOW;
    	    	}else{
    	    		return "";
    	    	}
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
