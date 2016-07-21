package ru.anov.qzproject.adapters;

import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PurchaseAdapter extends ArrayAdapter<Map<String, String>>{
	private Context context;
	
	public PurchaseAdapter(Context context, int textViewResourceId, List<Map<String, String>> objects) {//list is same as on web site
        super(context, textViewResourceId, objects);
        this.context = context;
    }
        
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View row;
        Map<String, String> item = getItem(position);
    	if(convertView != null){
    		row = convertView;
    	}else{
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		row = inflater.inflate(R.layout.item_purchase_theme, parent, false);
    	}
    	
    	TextView name = (TextView) row.findViewById(R.id.item_purchase_name_textview);
    	TextView description = (TextView) row.findViewById(R.id.item_purchase_description_textview);
    	name.setText(item.get("name"));
    	description.setText(item.get("description"));
    	
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
		name.setTypeface(tf);
		description.setTypeface(tf);
    	
        return row;
    }
}
