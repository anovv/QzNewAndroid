package ru.anov.qzproject.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.activities.CommentsActivity;
import ru.anov.qzproject.fragments.ScoresFragment;
import ru.anov.qzproject.fragments.UserlistFragment;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ThemesAdapter extends ArrayAdapter<Theme> {
	
	private Context context;
	private SparseBooleanArray openState;
	private SparseBooleanArray animState;
	private int menuViewHeight;
	private static final long EXPAND_DURATION_MILLIS = 500;
	private boolean isMain;
	private Map<String, Integer> ids;
	private Typeface tf;
	private Typeface typeface;
	private Map<View, Integer> openItems;
	
	private static final int ITEM_TYPE_1 = 0;//card with description
	private static final int ITEM_TYPE_2 = 1;//card no description
	private static final int ITEM_TYPE_3 = 2;//title
	private static final int ITEM_TYPE_4 = 3;//locked with description
	private static final int ITEM_TYPE_5 = 4;//locked no description
	
	private static final int MAX_NUMBER_OF_OPEN_ITEMS = 2;
	
    public ThemesAdapter(Context context, int textViewResourceId, boolean isMain, List<Theme> objects) {
    	super(context, textViewResourceId, objects);
        this.context = context;
        this.isMain = isMain;
        openState = new SparseBooleanArray();
        animState = new SparseBooleanArray();
        openItems = new HashMap<View, Integer>();
        ids = mapIds();
		tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
    	final View row;
    	int type = getItemViewType(position);
    	
    	if(type == ITEM_TYPE_4 || type == ITEM_TYPE_5){

        	ImageView mainImg;
        	TextView name;
        	TextView description = null;
        	ImageView arrowImg;
        	
        	if(convertView != null){
        		row = convertView;
    			Type45Holder holder = (Type45Holder) row.getTag();
    			mainImg = holder.mainImg;
    			name = holder.name;
    			arrowImg = holder.arrowImg;
    			if(type == ITEM_TYPE_4){
    				description = holder.description;
    			}
        	}else{
        		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		if(type == ITEM_TYPE_4){
            		row = inflater.inflate(R.layout.item_themelist, parent, false);	
        		}else{
            		row = inflater.inflate(R.layout.item_themelist_no_desc, parent, false);	
        		}
        		Type45Holder holder = new Type45Holder();
        		name = (TextView) row.findViewById(R.id.item_themelist_name_textview);
        		mainImg = (ImageView) row.findViewById(R.id.item_themelist_main_imageview);
        		arrowImg = (ImageView) row.findViewById(R.id.item_themelist_imageview);
        		holder.name = name;
               	holder.mainImg = mainImg;
               	holder.arrowImg = arrowImg;
               	if(type == ITEM_TYPE_4){
            		description = (TextView) row.findViewById(R.id.item_themelist_description_textview);
            		holder.description = description;
               	}
               	row.setTag(holder);
        	}
        	
        	Theme theme = getItem(position);
        	
            name.setTypeface(tf);
            name.setText(theme.getName());
            
            if(type == ITEM_TYPE_4){
    	    	description.setText(theme.getDescription());
    	    	description.setTypeface(typeface);
            }
            

            if(theme.isParent()){
            	if(ids.containsKey(theme.getId())){
            		mainImg.setBackgroundDrawable(context.getResources().getDrawable(ids.get(theme.getId())));
            	}else{
            		mainImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_topic_unknown));
            	}
            }else{
            	if(ids.containsKey(theme.getParentId())){
            		mainImg.setBackgroundDrawable(context.getResources().getDrawable(ids.get(theme.getParentId())));
            	}else{
            		mainImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_topic_unknown));
            	}
            }
            
    		arrowImg.setBackground(context.getResources().getDrawable(R.drawable.icon_lock));
        	
    		return row;
    	}
    	
    	if(type == ITEM_TYPE_3){
    		
    		TextView title;
    		
    		if(convertView != null){
        		row = convertView;
        		Type3Holder holder = (Type3Holder) row.getTag();
        		title = holder.titleTextView;
        	}else{
        		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    		row = inflater.inflate(R.layout.layout_title, parent, false);
	    		title = (TextView) row.findViewById(R.id.layout_title_textview);
	    		Type3Holder holder = new Type3Holder();
	    		holder.titleTextView = title;
	    		row.setTag(holder);
        	}
    		
    		Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
    		title.setTypeface(tf);
        	Theme theme = getItem(position);
    		title.setText(theme.getName());
    		return row;
    	}
    	
    	ImageView mainImg;
    	TextView name;
    	TextView description = null;
    	ImageView arrowImg;
    	Button random;
    	Button friend;
    	Button score;
    	Button comments;
    	
    	if(convertView != null){
    		row = convertView;
    		if(type == ITEM_TYPE_2){
    			Type2Holder holder = (Type2Holder) row.getTag();
    			mainImg = holder.mainImg;
    			name = holder.name;
    			arrowImg = holder.arrowImg;
    			random = holder.random;
    			friend = holder.friend;
    			score = holder.score;
    			comments = holder.comments;
        	}else{
    			Type1Holder holder = (Type1Holder) row.getTag();
    			mainImg = holder.mainImg;
    			name = holder.name;
    			description = holder.description;
    			arrowImg = holder.arrowImg;
    			random = holder.random;
    			friend = holder.friend;
    			score = holder.score;	
    			comments = holder.comments;
        	}
    	}else{
    		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if(type == ITEM_TYPE_2){
        		row = inflater.inflate(R.layout.item_themelist_no_desc, parent, false);	
        		Type2Holder holder = new Type2Holder();
        		name = (TextView) row.findViewById(R.id.item_themelist_name_textview);
        		mainImg = (ImageView) row.findViewById(R.id.item_themelist_main_imageview);
        		arrowImg = (ImageView) row.findViewById(R.id.item_themelist_imageview);
        		random = (Button) row.findViewById(R.id.item_themelist_random_button);
               	friend = (Button) row.findViewById(R.id.item_themelist_friend_button);
               	score = (Button) row.findViewById(R.id.item_themelist_score_button);
               	comments = (Button) row.findViewById(R.id.item_themelist_comments_button);
               	holder.name = name;
               	holder.mainImg = mainImg;
               	holder.arrowImg = arrowImg;
               	holder.random = random;
               	holder.friend = friend;
               	holder.score = score;
    			holder.comments = comments;
               	row.setTag(holder);
        	}else{
        		row = inflater.inflate(R.layout.item_themelist, parent, false);		
        		Type1Holder holder = new Type1Holder();
        		name = (TextView) row.findViewById(R.id.item_themelist_name_textview);
        		description = (TextView) row.findViewById(R.id.item_themelist_description_textview);
        		mainImg = (ImageView) row.findViewById(R.id.item_themelist_main_imageview);
        		arrowImg = (ImageView) row.findViewById(R.id.item_themelist_imageview);
        		random = (Button) row.findViewById(R.id.item_themelist_random_button);
               	friend = (Button) row.findViewById(R.id.item_themelist_friend_button);
               	score = (Button) row.findViewById(R.id.item_themelist_score_button);
               	comments = (Button) row.findViewById(R.id.item_themelist_comments_button);
               	holder.name = name;
               	holder.description = description;
               	holder.mainImg = mainImg;
               	holder.arrowImg = arrowImg;
               	holder.random = random;
               	holder.friend = friend;
               	holder.score = score;
    			holder.comments = comments;
               	row.setTag(holder);
        	}
        	openState.append(position, false);
            animState.append(position, false);
    	}
    	

    	final Theme theme = getItem(position);
    	
        name.setTypeface(tf);
        name.setText(theme.getName());
        
        if(type == ITEM_TYPE_1){
	    	description.setText(theme.getDescription());
	    	description.setTypeface(typeface);
        }
        
        if(theme.isParent()){
        	if(ids.containsKey(theme.getId())){
        		mainImg.setBackgroundDrawable(context.getResources().getDrawable(ids.get(theme.getId())));
        	}else{
        		mainImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_topic_unknown));
        	}
        }else{
        	if(ids.containsKey(theme.getParentId())){
        		mainImg.setBackgroundDrawable(context.getResources().getDrawable(ids.get(theme.getParentId())));
        	}else{
        		mainImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_topic_unknown));
        	}
        }
        
    	if(theme.isParent()){
    		arrowImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_more_black));
    	}else{
    		menuViewHeight = context.getResources().getDimensionPixelSize(R.dimen.menu_view_height);
    		if(GameLine.STATE == 0){
    			arrowImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_more_down_black));
    		}else{
    			arrowImg.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.icon_more_black));
    		}
    	}
    	
       	OnClickListener onClickListener = new OnClickListener(){
       		@Override
       		public void onClick(View v) {
       			switch(v.getId()){
       			case R.id.item_themelist_friend_button:
       				GameLine.STATE = 1;
       				GameLine.getInstance().setTheme(theme);

       				Intent intent = new Intent(context, BaseActivity.class);
       				intent.putExtra("id", APIHandler.user_id);
       				intent.putExtra("fragment", UserlistFragment.class.getSimpleName());
       				intent.putExtra("slide", true);
       				intent.putExtra("isSearch", false);
       				context.startActivity(intent);
       				((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
       				break;
       				
       			case R.id.item_themelist_random_button:
       				GameLine.STATE = 0;
       				GameLine.getInstance().setTheme(theme);
       				String themeId = GameLine.getInstance().getTheme().getId();
       				String themeName = GameLine.getInstance().getTheme().getName();
       				Utils.startGame(context, null, themeId, themeName, true, false, false);
       				break;
       			
       			case R.id.item_themelist_score_button:

       				Intent i = new Intent(context, BaseActivity.class);
       				i.putExtra("themeId", theme.getId());
       				i.putExtra("fragment", ScoresFragment.class.getSimpleName());
       				i.putExtra("slide", true);
       				context.startActivity(i);
       				((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
       				break;
       			
       			case R.id.item_themelist_comments_button:

	   				Intent in = new Intent(context, CommentsActivity.class);
	   				in.putExtra("themeId", theme.getId());
	   				context.startActivity(in);
	   				((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	   				break;
   				}
       			
       		}
       	};
       	
       	random.setOnClickListener(onClickListener);
       	friend.setOnClickListener(onClickListener);
       	score.setOnClickListener(onClickListener);
       	comments.setOnClickListener(onClickListener);
       	random.setTypeface(tf);
       	friend.setTypeface(tf);
       	score.setTypeface(tf);
       	comments.setTypeface(tf);

        return row;
    }
    
    @Override
    public int getViewTypeCount() {
    	if(!isMain){
    		return 4;
    	}else{
    		return 5;
    	}
    }
    
    @Override
    public int getItemViewType(int position) {
    	Theme theme = getItem(position);
    	if(!isMain){
        	if(theme.getDescription() == null || theme.getDescription().trim().length() == 0){
        		if("1".equals(theme.getLocked())){
        			return ITEM_TYPE_5;
        		}else{
        			return ITEM_TYPE_2;
        		}
        	}else{
        		if("1".equals(theme.getLocked())){
        			return ITEM_TYPE_4;
        		}else{
        			return ITEM_TYPE_1;
        		}
        	}
    	}
    	if(theme.getId().length() == 0){
    		return ITEM_TYPE_3;
    	}else{
        	if(theme.getDescription() == null || theme.getDescription().trim().length() == 0){
        		if("1".equals(theme.getLocked())){
        			return ITEM_TYPE_5;
        		}else{
        			return ITEM_TYPE_2;
        		}
        	}else{
        		if("1".equals(theme.getLocked())){
	    			return ITEM_TYPE_4;
	    		}else{
	    			return ITEM_TYPE_1;
	    		}
        	}
    	}
    }
    
    public boolean isOpen(int position){
    	return openState.get(position);
    }
    
    public boolean isAnimating(int position){
    	return animState.get(position);
    }
    
    public void expand(final View row, final int position) {
    	row.setHasTransientState(true);
    	final View menuView = row.findViewById(R.id.item_themelist_menu_view);
    	final View img = row.findViewById(R.id.item_themelist_imageview);
    	AnimationListener expandAnimationListener = new AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation) {
				animState.append(position, true);
				if(openItems.size() >= MAX_NUMBER_OF_OPEN_ITEMS){
					View firstOpen = ((Entry<View, Integer>)(openItems.entrySet().toArray()[0])).getKey();
					int pos = openItems.get(firstOpen);
					collapse(firstOpen, pos);
				}
			}
	
			@Override
			public void onAnimationEnd(Animation animation) {
				openState.append(position, true);
				animState.append(position, false);
		    	openItems.put(row, position);
			}
	
			@Override
			public void onAnimationRepeat(Animation animation) {}    			
		};

		menuView.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final int measuredHeight = menuViewHeight;
        menuView.getLayoutParams().height = 0;
        menuView.setVisibility(View.VISIBLE);
        Animation a = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
            	menuView.getLayoutParams().height = (int)(measuredHeight * interpolatedTime);
            	menuView.requestLayout();
            }
     
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
     
        a.setDuration(EXPAND_DURATION_MILLIS);
        a.setAnimationListener(expandAnimationListener);   

        RotateAnimation rotateAnim = new RotateAnimation(0, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(EXPAND_DURATION_MILLIS);
        rotateAnim.setFillAfter(true);
        
        img.startAnimation(rotateAnim);
		menuView.startAnimation(a);
    }
    
    public void collapse(final View row, final int position) {
    	row.setHasTransientState(true);
    	final View menuView = row.findViewById(R.id.item_themelist_menu_view);
    	final View img = row.findViewById(R.id.item_themelist_imageview);
        final int initialHeight = menuView.getMeasuredHeight();
        
        AnimationListener collapseAnimationListener = new AnimationListener(){
			@Override
			public void onAnimationStart(Animation animation) {
				animState.append(position, true);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				openState.append(position, false);
				animState.append(position, false);
		    	row.setHasTransientState(false);
		    	if(openItems.containsKey(row)){
		    		openItems.remove(row);
		    	}
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}    			
		};
		
    	Animation a = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                	menuView.setVisibility(View.GONE);
                }else{
                	menuView.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                	menuView.requestLayout();
                }
            }
     
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
     
        a.setDuration(EXPAND_DURATION_MILLIS);
        a.setAnimationListener(collapseAnimationListener);
        
        RotateAnimation rotateAnim = new RotateAnimation(-180, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(EXPAND_DURATION_MILLIS);
        rotateAnim.setFillAfter(true);
        img.startAnimation(rotateAnim);

		menuView.startAnimation(a);
    }
    
    
    private Map<String, Integer> mapIds(){
    	Map<String, Integer> res = new HashMap<String, Integer>();
    	
    	res.put("1", R.drawable.icon_topic_general);
    	res.put("2", R.drawable.icon_topic_exact_sciences);
    	res.put("3", R.drawable.icon_topic_humanities);
    	res.put("4", R.drawable.icon_topic_movies);
    	res.put("5", R.drawable.icon_topic_tv_shows);
    	res.put("6", R.drawable.icon_topic_books);
    	res.put("7", R.drawable.icon_topic_games);
    	res.put("8", R.drawable.icon_topic_sports);
    	res.put("9", R.drawable.icon_topic_music);
    	
    	return res;
    }
    
    private static class Type1Holder{
    	ImageView mainImg;
    	TextView name;
    	TextView description;
    	ImageView arrowImg;
    	Button random;
    	Button friend;
    	Button score;
    	Button comments;
    }
    
    private static class Type2Holder{
    	ImageView mainImg;
    	TextView name;
    	ImageView arrowImg;
    	Button random;
    	Button friend;
    	Button score;
    	Button comments;
    }

	private static class Type3Holder{
		TextView titleTextView;
	}
	
	private static class Type45Holder{
    	ImageView mainImg;
    	TextView name;
    	TextView description;
    	ImageView arrowImg;
	}
}
