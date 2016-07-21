package ru.anov.qzproject.adapters;

import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.models.Score;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ScoreAdapter extends ArrayAdapter<Score>{
	private Context context;
	private Typeface tf;
	
    public ScoreAdapter(Context context, int textViewResourceId, List<Score> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row;
    	TextView name;
    	TextView score;
    	ImageView imageView;
    	if(convertView != null){
    		row = convertView;
    		ViewHolder holder = (ViewHolder) row.getTag();
    		name = holder.name;
    		score = holder.score;
    		imageView = holder.imageView;
    	}else{
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.item_score, parent, false);
        	name = (TextView) row.findViewById(R.id.item_score_name_textview);
        	score = (TextView) row.findViewById(R.id.item_score_textview);
        	imageView = (ImageView) row.findViewById(R.id.item_score_imageview);
        	
        	ViewHolder holder = new ViewHolder();
        	holder.name = name;
        	holder.score = score;
        	holder.imageView = imageView;
        	row.setTag(holder);
    	}
        
        Score scr = getItem(position);
    	
    	imageView.setImageBitmap(null);
    	name.setText(scr.getUserName());
    	score.setText(scr.getScore());
    	
    	name.setTypeface(tf);
    	score.setTypeface(tf);
    	if(scr.getThumbnailImgUrl().length() > 0){
    		Picasso.with(context).load(scr.getThumbnailImgUrl()).into(imageView);
    	}
        return row;
    }
    
    private static class ViewHolder{
    	TextView name;
    	TextView score;
    	ImageView imageView;
    }

}
