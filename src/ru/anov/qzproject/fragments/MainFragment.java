package ru.anov.qzproject.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.MainActivity;
import ru.anov.qzproject.activities.PurchaseActivity;
import ru.anov.qzproject.activities.SubmitQuestionActivity;
import ru.anov.qzproject.adapters.ThemesAdapter;
import ru.anov.qzproject.db.DbOpenHelper;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainFragment extends QuickReturnFragment{

	public static String unlockedId;
	public static boolean unlockAll = false;
	
	private SwipeRefreshLayout refreshLayout;
	private ThemesAdapter adapter;
	private View mainView;
	private View curView;
	private View headerViewOnline;
	private View headerViewOffline;
	private View headerViewUpTheme;
	private View errorView;
	
	private ProgressBar progressBar;
	private Button errorButton;
	private TextView errorTextView;
	
	private View floatingView;
	private Button floatingButton;

	private boolean isSet;
	private boolean hasNew;
	private ListView listView;
	
	private ArrayList<Map<String, String>> themeMaplist;
	private ArrayList<Theme> favoriteList;
	private List<Theme> all;
	
	private boolean isHiddenOnce = false;
	
	public static MainFragment newInstance(){
		return new MainFragment();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
//		unlockedId = null;
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        headerViewOnline = inflater.inflate(R.layout.layout_header_request, null);
        headerViewOffline = inflater.inflate(R.layout.layout_header_request, null);
        headerViewUpTheme = inflater.inflate(R.layout.layout_header_up_theme, null);
        errorView = view.findViewById(R.id.fragment_main_error_view);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_main_progressbar);
        errorButton = (Button) view.findViewById(R.id.fragment_main_error_button);
        errorTextView = (TextView) view.findViewById(R.id.fragment_main_error_textview);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_main_refresh_layout);
        refreshLayout.setColorScheme(R.color.teal, R.color.light_teal, R.color.red_orange, android.R.color.transparent);
        floatingView = view.findViewById(R.id.floating_view);
        floatingButton = (Button) view.findViewById(R.id.floating_button);

		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
        floatingView.setVisibility(View.GONE);
        setHasOptionsMenu(true);
        return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		QuickReturnFragment.quickReturnState = QuickReturnState.ON_SCREEN;
		setQuickReturnView(floatingView);
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_home));
		isSet = false;
		mainView = getListView();
		getListView().addHeaderView(headerViewOffline);
		getListView().addHeaderView(headerViewOnline);
		getListView().addHeaderView(headerViewUpTheme);
		headerViewOffline.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		headerViewOnline.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		headerViewUpTheme.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		errorButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new CheckRequests(false, false, false).setContext(getActivity()).execute();
			}
		});
		
		floatingButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
    			startActivity(new Intent(getActivity(), PurchaseActivity.class));
			}
		});
		
		if(savedInstanceState == null){
			new CheckRequests(false, false, false).setContext(getActivity()).execute();
		}
		
		listView = getListView();
		
		String oldFrindsCount = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_friends_count", "0");
        String newFrindsCount = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_new_friends_count", "0");
        
        int delta = Integer.parseInt(newFrindsCount) - Integer.parseInt(oldFrindsCount);
        
        if(MainActivity.adapter != null){
        	MainActivity.adapter.setFriendsDelta(delta);
        	MainActivity.adapter.notifyDataSetChanged();
        }
	}
	
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.removeItem(R.id.action_search_userlist);
		menu.removeItem(R.id.action_search_themelist);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		isSet = false;
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_home));
		if(unlockedId != null && all != null && adapter != null){
			for(Theme t : all){
				if(t != null && unlockedId.equals(t.getId())){
					t.setLocked("0");
				}
			}
			adapter.notifyDataSetChanged();
			unlockedId = null;
		}
		if(unlockAll && all != null && adapter != null){
			for(Theme t : all){
				if(t != null){
					t.setLocked("0");
				}
			}
			adapter.notifyDataSetChanged();
			unlockAll = false;
		}
	}
	
	private class CheckRequests extends AsyncTask<Void, Void, Void>{
		
		private Map<String, Map<String, String>> res;
		private Context context;
		private boolean isDeclined;
		private boolean notify;
		private boolean isOfflineMode;
		
		public CheckRequests setContext(Context context){
			this.context = context;
			return this;
		}
		
		public CheckRequests(boolean isDeclined, boolean notify, boolean isOfflineMode){
			this.isDeclined = isDeclined;
			this.notify = notify;
			this.isOfflineMode = isOfflineMode;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			refreshLayout.setRefreshing(true);
			refreshLayout.setEnabled(false);
			Utils.crossfade(context, progressBar, curView);
			Utils.crossfade(context, null, floatingView);
			curView = progressBar;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.checkRequests(isDeclined, notify, isOfflineMode);
			if(!isSet){
				String newIds = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_new_ids", "");
				hasNew = newIds.length() > 0;
				themeMaplist = ThemeDao.getInstance(context).getAllThemes(DbOpenHelper.THEMES_TABLE_NAME);
				favoriteList = ThemeDao.getInstance(context).getFavoriteThemes(4);
				List<Theme> firstList = (hasNew) ? Theme.toNewList(themeMaplist, newIds) : Theme.toPopularList(themeMaplist, 4);
				List<Theme> secondList = favoriteList;
				List<Theme> thirdList = Theme.toList(themeMaplist);
				
				all = new ArrayList<Theme>();
				if(!firstList.isEmpty()){
					all.add(new Theme((hasNew) ? context.getResources().getString(R.string.new_themes) : context.getResources().getString(R.string.popular)));
					all.addAll(firstList);
				}
				if(!secondList.isEmpty()){
					all.add(new Theme(context.getResources().getString(R.string.favorite)));
					all.addAll(secondList);
				}
				if(!thirdList.isEmpty()){
					all.add(new Theme(context.getResources().getString(R.string.all_themes)));
					all.addAll(thirdList);
				}
			}
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(getActivity() == null || isCancelled()){
				return;
			}
			
			refreshLayout.setRefreshing(false);
			refreshLayout.setEnabled(true);
							
			if(res != null){

				NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				notifManager.cancelAll();
				refreshLayout.setOnRefreshListener(new OnRefreshListener(){
					@Override
					public void onRefresh() {
						new CheckRequests(false, false, false).setContext(getActivity()).execute();
					}
					
				});
				
				if(all != null && getActivity() != null){
			    	adapter = new ThemesAdapter(getActivity(), android.R.id.list, true, all);
					isSet = true;
			    }
				
				AnimationSet set = new AnimationSet(true);

			    Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
			            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
			            50.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			    animation.setDuration(300);
			    set.addAnimation(animation);
			    LayoutAnimationController controller = new LayoutAnimationController(
			            set, 0.5f);
			    if(listView != null){
			    	listView.setLayoutAnimation(controller);
			    }
			    
			    if(adapter != null){
			    	setListAdapter(adapter);
			    }
				setHeaders(res);
				Utils.crossfade(context, mainView, progressBar);
				Utils.crossfade(context, floatingView, null);
				curView = mainView;
				
				Runnable fitsOnScreen = new Runnable() {
				    @Override
				    public void run() {
				        int last = listView.getLastVisiblePosition();
				        if(listView.getChildAt(last) == null){
				        	return;
				        }
				        boolean fits = last == listView.getCount() - 1 && listView.getChildAt(last).getBottom() <= listView.getHeight();
				        if(fits) {
				        	setEnabled(false);
				            return;
				        }
			        	setEnabled(true);
				        if(!isHiddenOnce){
							new Handler().postDelayed(new Runnable(){
								@Override
								public void run() {
									hideQuickReturnView();
								}
							}, 1000);
							isHiddenOnce = true;
						}
				    }
				};
				listView.post(fitsOnScreen);
				showCompletedChallengeDialog();
				showInfoDialog();
				showRateDialog();
				showShareDialog();
				showShareDialogInst();
				showRateDialogInst();
			}else{
				Utils.crossfade(context, errorView, progressBar);
				curView = errorView;
			}
		}
	}

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		position = position - 3;

    	final Theme theme = (Theme) adapter.getItem(position);
    	if(theme.getId().length() == 0){
    		return;
    	}
    	
    	if("1".equals(theme.getLocked())){
			startActivity(new Intent(getActivity(), PurchaseActivity.class));
    		return;
    	}
    	
    	if(theme.isParent()){
			ThemelistFragment child = ThemelistFragment.newInstance(theme.getName(), true, 0, theme.getChildren(), true);
			child.setQuickReturnView(ThemesFragment.buttonsView);
    		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.hide(getFragmentManager().findFragmentById(R.id.container));
			fragmentTransaction.add(R.id.container, child);
    		fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}else{
			if(GameLine.STATE == 0){
				if(adapter.isAnimating(position)){
					return;
				}
				if(adapter.isOpen(position)){
					adapter.collapse(v, position);
				}else{
					adapter.expand(v, position);
				}
			}else{
				GameLine.STATE = 0;
				if(GameLine.getInstance() != null && 
						GameLine.getInstance().getFriend() != null &&
						GameLine.getInstance().getTheme() != null){
					GameLine.getInstance().setTheme(theme);
					String rid = GameLine.getInstance().getFriend().getId();
					String themeId = GameLine.getInstance().getTheme().getId();
					String themeName = GameLine.getInstance().getTheme().getName();
					Utils.startGame(getActivity(), rid, themeId, themeName, false, true, true);
				}
			}
		}
	}
	private void setHeaders(Map<String, Map<String, String>> res){
		
		headerViewOnline.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		headerViewOffline.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		headerViewUpTheme.findViewById(R.id.layout_header_view).setVisibility(View.GONE);
		if(res == null){
			return;
		}
		
		Map<String, String> online = res.get("online");
		Map<String, String> offline = res.get("offline");
		
		if(online.size() != 0){
			final String id = online.get("id");
			String name = online.get("name");
			String status = online.get("status");
			final String themeId = online.get("theme_id");
			final String themeName = online.get("theme_name");
			Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
			
			TextView nameTextView = (TextView) headerViewOnline.findViewById(R.id.header_themes_name);
			TextView themeNameTextView = (TextView) headerViewOnline.findViewById(R.id.header_themes_themename);
			
			nameTextView.setTypeface(tf);
			themeNameTextView.setTypeface(tf);
			
			Button accept = (Button) headerViewOnline.findViewById(R.id.header_themes_accept_button);
			Button decline = (Button) headerViewOnline.findViewById(R.id.header_themes_decline_button);
			
			nameTextView.setText(name + getActivity().getResources().getString(R.string.wants_to_play));
			themeNameTextView.setText(themeName);
			
			accept.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					new CheckRequests(true, false, false).setContext(getActivity()).execute();
					Utils.startGame(getActivity(), id, themeId, themeName, false, false, false);
				}
				
			});

			decline.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					new CheckRequests(true, true, false).setContext(getActivity()).execute();
				}
				
			});
			headerViewOnline.findViewById(R.id.layout_header_view).setVisibility(View.VISIBLE);
		}
		
		if(offline.size() != 0){
			final String id = offline.get("id");
			String name = offline.get("name");
			String status = offline.get("status");
			final String themeId = offline.get("theme_id");
			final String themeName = offline.get("theme_name");
			final String ansSeq = offline.get("ans_seq");
			Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
			
			TextView nameTextView = (TextView) headerViewOffline.findViewById(R.id.header_themes_name);
			TextView themeNameTextView = (TextView) headerViewOffline.findViewById(R.id.header_themes_themename);
			
			nameTextView.setTypeface(tf);
			themeNameTextView.setTypeface(tf);
			
			Button accept = (Button) headerViewOffline.findViewById(R.id.header_themes_accept_button);
			Button decline = (Button) headerViewOffline.findViewById(R.id.header_themes_decline_button);
			
			nameTextView.setText(name + getActivity().getResources().getString(R.string.left_a_challenge));
			themeNameTextView.setText(themeName);
			accept.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					new CheckRequests(true, false, true).setContext(getActivity()).execute();
					Utils.startOfflineResponseGame(getActivity(), id, themeId, themeName, ansSeq);
				}
				
			});

			decline.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					new CheckRequests(true, true, true).setContext(getActivity()).execute();
				}
				
			});
			headerViewOffline.findViewById(R.id.layout_header_view).setVisibility(View.VISIBLE);
		}
		
		String upThemeId = null;
		String upThemeName = null;
		String upThemeCounter = null;
		String upThemeMessage = null;
		try{
			upThemeId = res.get("up_theme_id").get("up_theme_id");
			upThemeName = res.get("up_theme_name").get("up_theme_name");
			upThemeCounter = res.get("up_theme_counter").get("up_theme_counter");
			upThemeMessage = res.get("up_theme_message").get("up_theme_message");
		}catch(Exception e){
			upThemeId = null;
			upThemeName = null;
			upThemeCounter = null;
			upThemeMessage = null;
		}
		
		if(upThemeName != null && upThemeName.length() != 0){
			final Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
			final String message = upThemeMessage;
			TextView themeNameTextView = (TextView) headerViewUpTheme.findViewById(R.id.header_up_theme_name);
			TextView counterTextView = (TextView) headerViewUpTheme.findViewById(R.id.header_up_counter);
			final TextView qMark = (TextView) headerViewUpTheme.findViewById(R.id.header_up_qmark);
			
			themeNameTextView.setTypeface(tf);
			counterTextView.setTypeface(tf);
			qMark.setTypeface(tf);
			themeNameTextView.setText(upThemeName);
			counterTextView.setText(upThemeCounter);
			
			Button submit = (Button) headerViewUpTheme.findViewById(R.id.header_up_submit);
			final String tid = upThemeId;
			final String tname = upThemeName;
			submit.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(getActivity(), SubmitQuestionActivity.class);
					i.putExtra("up_theme_id", tid);
					i.putExtra("up_theme_name", tname);
        			startActivity(i);
				}
			});
			
			qMark.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(getActivity());
					Utils.popAnim(qMark, getActivity());
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.layout_help_dialog);
					TextView text = (TextView) dialog.findViewById(R.id.layout_help_textview);
					text.setTypeface(tf);
					text.setText(message);
					dialog.show();
				}
				
			});
			
			headerViewUpTheme.findViewById(R.id.layout_header_view).setVisibility(View.VISIBLE);
		}
	}
	
	private void showCompletedChallengeDialog(){
		if(getActivity() == null){
			return;
		}
		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		if(sp == null || !sp.contains("qz_offline_result_id")){
			return;
		}
		
		String rid = sp.getString("qz_offline_result_id", "0");
		String name = sp.getString("qz_offline_result_name", "");
		String themeId = sp.getString("qz_offline_result_theme_id", "0");
		String themeName = sp.getString("qz_offline_result_theme_name", "");
		String score = sp.getString("qz_offline_result_score", "0");
		String rscore = sp.getString("qz_offline_result_rscore", "0");
		String newScore = sp.getString("qz_offline_result_new_score", "0");
		

		getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
			.remove("qz_offline_result_id")
			.remove("qz_offline_result_name")
			.remove("qz_offline_result_theme_id")
			.remove("qz_offline_result_theme_name")
			.remove("qz_offline_result_score")
			.remove("qz_offline_result_rscore")
			.remove("qz_offline_result_new_score")
			.commit();
		

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_help_dialog);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_help_textview);
		text.setTypeface(tf);
		
		String title = name + getActivity().getResources().getString(R.string.completed_your_challenge);
		
		boolean draw = Long.parseLong(rscore) == Long.parseLong(score);
		boolean hasWon = Long.parseLong(rscore) < Long.parseLong(score);
		
		String result = (draw) ? getActivity().getResources().getString(R.string.draw) : ((hasWon) ? getActivity().getResources().getString(R.string.you_won) : getActivity().getResources().getString(R.string.you_lost));
		String scoreString = getActivity().getResources().getString(R.string.your_new_score) + newScore;
		
		String message = title + "\n" 
				+ themeName + "\n"
				+ result + "\n"
				+ scoreString;
		
		text.setText(message);
		
		dialog.show();
	}
	
	private void showInfoDialog(){
		if(getActivity() == null){
			return;
		}
		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		if(sp == null || !sp.contains("qz_info_long_text")){
			return;
		}
		
		String message = sp.getString("qz_info_long_text", "");
		String check = sp.getString("qz_info_check_purchase", "");
		

		getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
			.remove("qz_info_long_text")
			.remove("qz_info_check_purchase")
			.commit();
		

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		if("1".equals(check)){
			dialog.setContentView(R.layout.layout_booster_dialog);
			TextView text = (TextView) dialog.findViewById(R.id.layout_booster_textview);
			TextView shop = (TextView) dialog.findViewById(R.id.layout_booster_shop_textview);
			text.setTypeface(tf);
			shop.setTypeface(tf);
			shop.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
	    			startActivity(new Intent(getActivity(), PurchaseActivity.class));
					dialog.dismiss();
				}
				
			});
			text.setText(message);
			shop.setText(getActivity().getResources().getString(R.string.check_the_shop));
		}else{
			dialog.setContentView(R.layout.layout_help_dialog);
			TextView text = (TextView) dialog.findViewById(R.id.layout_help_textview);
			text.setTypeface(tf);
			text.setText(message);
		}
		
		dialog.show();
	}
	
	private void showRateDialog(){
		if(getActivity() == null){
			return;
		}

		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		
		if(sp.getBoolean("qz_not_rate", false)){
			return;
		}
		
		Editor editor = sp.edit();
		
        long firstLaunch = sp.getLong("qz_first_launch_rate", 0);
        if (firstLaunch == 0) {
            firstLaunch = System.currentTimeMillis();
            editor.putLong("qz_first_launch_rate", firstLaunch);
        }

        
        if (System.currentTimeMillis() < firstLaunch + 2 * 24 * 60 * 60 * 1000) {
            editor.commit();
        	return;
        }
        
        editor.remove("qz_first_launch_rate");
        editor.commit();
        
		final String packageName = getActivity().getPackageName();
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_rate);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_rate_textview);
		text.setTypeface(tf);
		
		Button b = (Button) dialog.findViewById(R.id.layout_rate_button);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(getActivity() != null){
					getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));

					SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
					sp.edit().putBoolean("qz_not_rate", true).commit();
				}
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
	}
	
	private void showShareDialog(){
		if(getActivity() == null){
			return;
		}

		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		
		if(sp.getBoolean("qz_not_share", false)){
			return;
		}
		
		Editor editor = sp.edit();
		
        long firstLaunch = sp.getLong("qz_first_launch_share", 0);
        if (firstLaunch == 0) {
            firstLaunch = System.currentTimeMillis();
            editor.putLong("qz_first_launch_share", firstLaunch);
        }

        
        if (System.currentTimeMillis() < firstLaunch + 3 * 12 * 60 * 60 * 1000) {
            editor.commit();
        	return;
        }
        
        editor.remove("qz_first_launch_share");
        editor.commit();
        
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_share);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_share_textview);
		text.setTypeface(tf);
		
		Button b = (Button) dialog.findViewById(R.id.layout_share_button);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(getActivity() != null){
					String packageName = getActivity().getPackageName();
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
				    sharingIntent.setType("text/plain");
				    String shareBody = getActivity().getResources().getString(R.string.app_name) + "\n" + "http://play.google.com/store/apps/details?id=" + packageName;
				    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getActivity().getResources().getString(R.string.share));
				    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				    getActivity().startActivity(Intent.createChooser(sharingIntent, getActivity().getResources().getString(R.string.share_via)));
					
					SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
					sp.edit().putBoolean("qz_not_share", true).commit();
				}
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
	}
	
	private void showShareDialogInst(){
		if(getActivity() == null){
			return;
		}

		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		String showShare = sp.getString("qz_info_show_share", "0");
		
		if(!"1".equals(showShare)){
			return;
		}
		
		sp.edit().putString("qz_info_show_share", "0").remove("qz_first_launch_share").commit();

		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_share);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_share_textview);
		text.setTypeface(tf);
		
		Button b = (Button) dialog.findViewById(R.id.layout_share_button);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(getActivity() != null){
					String packageName = getActivity().getPackageName();
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND); 
				    sharingIntent.setType("text/plain");
				    String shareBody = getActivity().getResources().getString(R.string.app_name) + "\n" + "http://play.google.com/store/apps/details?id=" + packageName;
				    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getActivity().getResources().getString(R.string.share));
				    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				    getActivity().startActivity(Intent.createChooser(sharingIntent, getActivity().getResources().getString(R.string.share_via)));
					
					SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
					sp.edit().putBoolean("qz_not_share", true).commit();
				}
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
	}
	
	private void showRateDialogInst(){
		if(getActivity() == null){
			return;
		}

		SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
		String showShare = sp.getString("qz_info_show_rate", "0");
		
		if(!"1".equals(showShare)){
			return;
		}
		
		sp.edit().putString("qz_info_show_rate", "0").remove("qz_first_launch_rate").commit();

		final String packageName = getActivity().getPackageName();
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_rate);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_rate_textview);
		text.setTypeface(tf);
		
		Button b = (Button) dialog.findViewById(R.id.layout_rate_button);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(getActivity() != null){
					getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));

					SharedPreferences sp = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE);
					sp.edit().putBoolean("qz_not_rate", true).commit();
				}
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
	}
}
