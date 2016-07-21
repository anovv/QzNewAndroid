package ru.anov.qzproject.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.activities.ChatActivity;
import ru.anov.qzproject.activities.PurchaseActivity;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.models.User;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import eu.janmuller.android.simplecropimage.CropImage;

public class UserFragment extends QuickReturnFragment{
	
	private static final int SCORES_TO_SHOW = 6;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CROP_IMAGE = 2;
	private static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	
	private String id;
	private User user;
	private ArrayList<Map<String, String>> scores;
	
	private boolean isFriend;
	private int friendsCount;
	
	private Button addFriendButton;
	private Button unfriendButton;
	private Button blockUserButton;
	private Button unblockButton;
	private Button friendsButton;
	private Button chatButton;
	private View uploadImageButton;
	private View buttonsView;
	
	private View userView;
	private TextView userNameTextView;
	private TextView userStatusTextView;
	private TextView userBestinTextView;
	private TextView boosterTextView;
	private View errorView;
	private Button errorButton;
	private TextView errorTextView;
	private ImageView profileImageView;
	private ProgressBar progressBar;
	
	private View curView;
	private View scoreView;
	
	private View floatingView;
	private Button floatingButton;
	
    private File tempFile;
    
	public static UserFragment newInstance(String id){
		Bundle bundle = new Bundle();
		bundle.putString("id", id);
		UserFragment userFragment = new UserFragment();
		userFragment.setArguments(bundle);
		
		return userFragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		id = getArguments().getString("id");
		View view = inflater.inflate(R.layout.fragment_user, container, false);
		addFriendButton = (Button) view.findViewById(R.id.fragment_user_addfriend_button);
		unfriendButton = (Button) view.findViewById(R.id.fragment_user_unfriend_button);
		blockUserButton = (Button) view.findViewById(R.id.fragment_user_blockuser_button);
		unblockButton = (Button) view.findViewById(R.id.fragment_user_unblockuser_button);
		friendsButton = (Button) view.findViewById(R.id.fragment_user_friends_button);
		chatButton = (Button) view.findViewById(R.id.fragment_user_chat_button);
		uploadImageButton = view.findViewById(R.id.fragment_user_uploadimage_button);
		buttonsView = view.findViewById(R.id.fragment_user_buttons_view);
		
		userNameTextView = (TextView) view.findViewById(R.id.fragment_user_name_textview);
		userStatusTextView = (TextView) view.findViewById(R.id.fragment_user_status_textview);
		userBestinTextView = (TextView) view.findViewById(R.id.fragment_user_bestin_textview);
		boosterTextView = (TextView) view.findViewById(R.id.fragment_user_booster_textview);
		floatingView = view.findViewById(R.id.floating_view);
		floatingButton = (Button) view.findViewById(R.id.floating_button);
		floatingButton.setVisibility(View.GONE);
		
		
		userView = view.findViewById(R.id.fragment_user_view);
		errorView = view.findViewById(R.id.fragment_user_error_view);
		errorButton = (Button) view.findViewById(R.id.fragment_user_error_button);
		errorTextView = (TextView) view.findViewById(R.id.fragment_user_error_textview);
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_user_progressbar);
		profileImageView = (ImageView) view.findViewById(R.id.fragment_user_profile_imageview);
		
		scoreView = view.findViewById(R.id.fragment_user_score_view);
		
	    setHasOptionsMenu(true);
		return view;
	}
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.removeItem(R.id.action_search_themelist);
		menu.removeItem(R.id.action_search_userlist);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		setQuickReturnView(floatingView);
		setDetectView(userView);
		super.onActivityCreated(savedInstanceState);

		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_profile));
		
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
		userBestinTextView.setTypeface(typeface);
		userNameTextView.setTypeface(tf);
		boosterTextView.setTypeface(tf);
		userStatusTextView.setTypeface(typeface);
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
		addFriendButton.setTypeface(tf);
		unfriendButton.setTypeface(tf);
		blockUserButton.setTypeface(tf);
		unblockButton.setTypeface(tf);
		friendsButton.setTypeface(tf);
		chatButton.setTypeface(tf);
		
		boosterTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Utils.popAnim(boosterTextView, getActivity());
				showBoosterInfoDialog();
			}
			
		});
		
		uploadImageButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
		    	if (Environment.MEDIA_MOUNTED.equals(state)) {
		    		tempFile = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
		    	}else{
		    		tempFile = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
		    	}
				
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        photoPickerIntent.setType("image/*");
		        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
			}
			
		});
		
		friendsButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), BaseActivity.class);
				intent.putExtra("id", id);
				intent.putExtra("fragment", UserlistFragment.class.getSimpleName());
				startActivity(intent);
			}
			
		});
		
		addFriendButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new AddFriend().setContext(getActivity()).execute();
			}
			
		});
		
		unfriendButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new Unfriend().setContext(getActivity()).execute();
			}
			
		});
		
		profileImageView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(profileImageView.getDrawable() != null){
					Bitmap bitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();
					Utils.showProfileImage(bitmap, getActivity());
				}
			}
			
		});
		
		blockUserButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new BlockUser().setContext(getActivity()).execute();
			}
			
		});
		
		unblockButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new Unblock().setContext(getActivity()).execute();
			}
			
		});

		errorButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(getActivity() == null){
					return;
				}
				new GetUser().setContext(getActivity()).execute();
			}
		});
		
		floatingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				GameLine.STATE = 1;
				GameLine.getInstance().setFriend(user.toFriend());

				Intent intent = new Intent(getActivity(), BaseActivity.class);
				intent.putExtra("fragment", ThemelistFragment.class.getSimpleName());
				intent.putExtra("isSearch", true);
				intent.putExtra("isGlobalSearch", true);
				intent.putExtra("slide", true);
				intent.putExtra("title", getResources().getString(R.string.all_themes));
				intent.putExtra("showLocked", false);
				startActivity(intent);

				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
			
		});
		
		chatButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				if(user == null){
					return;
				}
				intent.putExtra("ruserId", id);
				intent.putExtra("name", user.getName());
				intent.putExtra("thumbnail_img_url", user.getThumbnailImgUrl());
				startActivity(intent);
				getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});

		
		if(savedInstanceState == null){
			new GetUser().setContext(getActivity()).execute();
		}
	}
	
	private void startCropImage() {
	    Intent intent = new Intent(getActivity(), CropImage.class);
	    intent.putExtra(CropImage.IMAGE_PATH, tempFile.getPath());
	    intent.putExtra(CropImage.SCALE, true);
	
	    intent.putExtra(CropImage.ASPECT_X, 2);
	    intent.putExtra(CropImage.ASPECT_Y, 2);
	
	    startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != Activity.RESULT_OK) {
            return;
        }
    	
        Bitmap bitmap;
        
        switch (requestCode){
            case REQUEST_CODE_GALLERY:
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                    Utils.copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    startCropImage();
                } catch (Exception e) {}

                break;

            case REQUEST_CODE_CROP_IMAGE:
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }

                bitmap = BitmapFactory.decodeFile(tempFile.getPath());
				new UploadImage(bitmap, getActivity()).execute();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);        
    }

	private class GetUser extends AsyncTask<Void, Void, Void>{
		
		private User user;
		private Context context;
		private ArrayList<Map<String, String>> list;
		
		public GetUser setContext(Context context){
			this.context = context;
			return this;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Utils.crossfade(context, progressBar, curView);
			curView = progressBar;
			floatingButton.setVisibility(View.GONE);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			list = APIHandler.getUserById(id);
			try{
				Map<String, String> u = list.get(0);
				user = new User(u);
				if(!user.getId().equals(APIHandler.user_id)){
					MessageDao.getInstance(context).updateCredentials(user.getId(), user.getName(), user.getThumbnailImgUrl());
				}
				list.remove(0);
			}catch(Exception e){
				list = null;
				user = null;
			}
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null || isCancelled()){
				return;
			}	
			if(user != null && list != null){
				isFriend = user.isFriend();
				friendsCount = user.getFriendsCount();
				if(user.isFriend()){
					QuickReturnFragment.quickReturnState = QuickReturnState.ON_SCREEN;
				}else{
					QuickReturnFragment.quickReturnState = QuickReturnState.OFF_SCREEN;
				}
				UserFragment.this.user = user;
				UserFragment.this.scores = list;
				setScoreInfo(list, context, scoreView);
				setUserInfo(user, context);
			}else{
				Utils.crossfade(context, errorView, progressBar);
				curView = errorView;
			}
		}
	}
	
	private void setUserInfo(User user, Context context){
		if(user == null){
			return;
		}
		if(user.getId().equals(APIHandler.user_id)){
			Editor editor = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
			.putString("qz_profile_img_url", user.getProfileImgUrl())
			.putString("qz_thumbnail_img_url", user.getThumbnailImgUrl())
			.putString("qz_name", user.getName());
			if(user.getStatus() != null && user.getStatus().trim().length() != 0 && !user.getStatus().equals("null")){//lol
				editor.putString("qz_status", user.getStatus());
			}else{
				editor.putString("qz_status", "");
			}
			if(user.getBestIn() != null && user.getBestIn().trim().length() != 0 && !user.getBestIn().equals("null")){//lol
				editor.putString("qz_best_in", user.getBestIn());
			}else{
				editor.putString("qz_status", "");
			}
			editor.commit();
			floatingButton.setVisibility(View.GONE);
			buttonsView.setVisibility(View.GONE);
		}else if(user.isFriend()){
			addFriendButton.setVisibility(View.GONE);
			unblockButton.setVisibility(View.GONE);
			uploadImageButton.setVisibility(View.GONE);
			floatingButton.setVisibility(View.VISIBLE);
		}else if(user.isBlocked()){
			if(user.isBlockedByMe()){
				floatingButton.setVisibility(View.GONE);
				addFriendButton.setVisibility(View.GONE);
				unfriendButton.setVisibility(View.GONE);
				blockUserButton.setVisibility(View.GONE);
				uploadImageButton.setVisibility(View.GONE);
			}else{
				floatingButton.setVisibility(View.GONE);
				uploadImageButton.setVisibility(View.GONE);
				buttonsView.setVisibility(View.GONE);
			}
		}else{
			floatingButton.setVisibility(View.GONE);
			unfriendButton.setVisibility(View.GONE);
			unblockButton.setVisibility(View.GONE);
			uploadImageButton.setVisibility(View.GONE);
		}
		
		if(user.getId().equals(APIHandler.user_id)){
			boosterTextView.setText("x" + Utils.getBoosterValue(getActivity()));
			Utils.popAnim(boosterTextView, getActivity());
		}else{
			boosterTextView.setVisibility(View.GONE);
		}
		
		userNameTextView.setText(user.getName());
		if(user.getStatus() != null && user.getStatus().trim().length() != 0 && !user.getStatus().equals("null")){//lol
			userStatusTextView.setText(user.getStatus());
		}else{
			userStatusTextView.setVisibility(View.GONE);
		}
		Picasso picasso = Picasso.with(getActivity());
		if(user.getBestIn() != null && user.getBestIn().trim().length() != 0 && !user.getBestIn().equals("null")){//lol
			String bestIn = getActivity().getResources().getString(R.string.best_in) +"\n" + user.getBestIn();
			userBestinTextView.setText(bestIn);
		}else{
			userBestinTextView.setVisibility(View.GONE);
		}
		
		if(user.getProfileImgUrl().length() > 0){
			picasso.load(user.getProfileImgUrl()).into(profileImageView);
		}
		
		if(user != null && user.isBlocked()){
			View miniScoreView = scoreView.findViewById(R.id.layout_miniscore_view);
			View duelView = scoreView.findViewById(R.id.layout_miniscore_duel_view);
			TextView title = (TextView) scoreView.findViewById(R.id.layout_miniscore_duel_title_textview);
			miniScoreView.setVisibility(View.GONE);
			title.setVisibility(View.GONE);
			duelView.setVisibility(View.GONE);
			TextView emptyTextView = (TextView) scoreView.findViewById(R.id.layout_miniscore_empty_textview);
			emptyTextView.setVisibility(View.VISIBLE);
			Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
			emptyTextView.setTypeface(tf);
			if(user.isBlockedByMe()){
				emptyTextView.setText(context.getResources().getString(R.string.you_blocked));
			}else{
				emptyTextView.setText(context.getResources().getString(R.string.user_blocked));
			}
		}
		
		friendsButton.setText(getResources().getString(R.string.friends) + "(" + friendsCount + ")");
		
		Utils.crossfade(getActivity(), userView, progressBar);
		curView = userView;
	}
	
	private void setScoreInfo(List<Map<String, String>> scores, Context context, View scoreView){
		if(scores == null){
			return;
		}
		
		Map<String, String> duel = null;
		List<Map<String, String>> scoresWithoutDuels = new ArrayList<Map<String, String>>();
		for(Map<String, String> map : scores){
			if(map.containsKey("op_duel_score")){
				duel = map;
			}else{
				scoresWithoutDuels.add(map);
			}
		}
		
		scores = scoresWithoutDuels;
		
		View miniScoreView = scoreView.findViewById(R.id.layout_miniscore_view);
		Collections.sort(scores, new ScoreComparator());
		if(scores.size() > SCORES_TO_SHOW){
			long otherScores = 0;
			int i = SCORES_TO_SHOW - 1;
			while(scores.size() != SCORES_TO_SHOW - 1){
				String score = scores.get(i).get("score");
				otherScores += Long.parseLong(score);
				scores.remove(i);
			}
			Map<String, String> others = new HashMap<String, String>();
			others.put("score", otherScores + "");
			others.put("theme_name", getActivity().getResources().getString(R.string.others_themename));
			scores.add(others);
		}
		if(scores.size() != 0){
			miniScoreView.setVisibility(View.VISIBLE);
			LinearLayout layout = (LinearLayout) scoreView.findViewById(R.id.layout_miniscore_ll);
			TextView title = (TextView) scoreView.findViewById(R.id.layout_miniscore_title_textview);
			Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
			Typeface typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
			title.setTypeface(typeface);
			
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			layout.removeAllViews();
			
			for(Map<String, String> score : scores){
				String themeName = score.get("theme_name"); 
				String sc = score.get("score");
				View item = inflater.inflate(R.layout.layout_miniscore_view, null);
				TextView themeNameTextView = (TextView) item.findViewById(R.id.layout_miniscore_themename_textview);
				TextView scoreTextView = (TextView) item.findViewById(R.id.layout_miniscore_score_textview);
				themeNameTextView.setTypeface(tf);
				themeNameTextView.setText(themeName);
				scoreTextView.setText(sc);
				scoreTextView.setTypeface(tf);
				layout.addView(item);
			}
		}else{
			miniScoreView.setVisibility(View.GONE);
		}
		
		View duelView = scoreView.findViewById(R.id.layout_miniscore_duel_view);
		
		TextView title = (TextView) scoreView.findViewById(R.id.layout_miniscore_duel_title_textview);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Italic.ttf");
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
		title.setTypeface(typeface);
		
		if(!id.equals(APIHandler.user_id)){
			title.setVisibility(View.VISIBLE);
			duelView.setVisibility(View.VISIBLE);
			String userDuelScore = duel.get("user_duel_score");
			String opDuelScore = duel.get("op_duel_score");
			TextView userTextView = (TextView) duelView.findViewById(R.id.layout_miniscore_duel_user_title);
			TextView opTextView = (TextView) duelView.findViewById(R.id.layout_miniscore_duel_op_title);
			TextView userScoreTextView = (TextView) duelView.findViewById(R.id.layout_miniscore_duel_user_score);
			TextView opScoreTextView = (TextView) duelView.findViewById(R.id.layout_miniscore_duel_op_score);
			TextView vsTextView = (TextView) duelView.findViewById(R.id.layout_miniscore_vs_textview);
			
			userTextView.setTypeface(tf);
			opTextView.setTypeface(tf);
			userScoreTextView.setTypeface(tf);
			opScoreTextView.setTypeface(tf);
			vsTextView.setTypeface(typeface);
			
			userScoreTextView.setText(userDuelScore);
			opScoreTextView.setText(opDuelScore);
			if(userDuelScore.equals("0") && opDuelScore.equals("0")){
				title.setVisibility(View.GONE);
				duelView.setVisibility(View.GONE);
				TextView emptyTextView = (TextView) scoreView.findViewById(R.id.layout_miniscore_empty_textview);
				emptyTextView.setTypeface(tf);
				emptyTextView.setVisibility(View.VISIBLE);
				emptyTextView.setText(context.getResources().getString(R.string.you_havent_played_user));
				if(!scores.isEmpty()){
					emptyTextView.setVisibility(View.GONE);
				}
			}
		}else{
			title.setVisibility(View.GONE);
			duelView.setVisibility(View.GONE);
		}
		
		if(id.equals(APIHandler.user_id) && scores.size() == 0){
			miniScoreView.setVisibility(View.GONE);
			TextView emptyTextView = (TextView) scoreView.findViewById(R.id.layout_miniscore_empty_textview);
			emptyTextView.setVisibility(View.VISIBLE);
			emptyTextView.setTypeface(tf);
			emptyTextView.setText(context.getResources().getString(R.string.you_havent_played));
			if(!scores.isEmpty()){
				emptyTextView.setVisibility(View.GONE);
			}
		}
	}
	
	private class ScoreComparator implements Comparator<Map<String, String>>{

		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			int score1 = Integer.parseInt(lhs.get("score"));
			int score2 = Integer.parseInt(rhs.get("score"));
			return score2 - score1;
		}
		
	}
	
	private class AddFriend extends AsyncTask<Void, Void, Void>{
		
		private boolean res;
		private Context context;
		
		public AddFriend setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			addFriendButton.setClickable(false);
			Utils.crossfade(context, progressBar, null);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.addFriend(id);
			
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null){
				return;
			}
			Utils.crossfade(context, null, progressBar);
			addFriendButton.setClickable(true);
			if(res){
				addFriendButton.setVisibility(View.GONE);
				unfriendButton.setVisibility(View.VISIBLE);
				chatButton.setVisibility(View.VISIBLE);
				floatingButton.setVisibility(View.VISIBLE);
				showQuickReturnView();
				blockUserButton.setVisibility(View.VISIBLE);
				friendsCount++;
				isFriend = true;
				friendsButton.setText(getResources().getString(R.string.friends) + "(" + friendsCount + ")");
				Toast.makeText(context, context.getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class Unfriend extends AsyncTask<Void, Void, Void>{
		
		private boolean res;
		private Context context;
		
		public Unfriend setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			blockUserButton.setClickable(false);
			addFriendButton.setClickable(false);
			unfriendButton.setClickable(false);
			Utils.crossfade(context, progressBar, null);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.unfriend(id);
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null){
				return;
			}
			Utils.crossfade(context, null, progressBar);
			blockUserButton.setClickable(true);
			addFriendButton.setClickable(true);
			unfriendButton.setClickable(true);
			if(res){
				unfriendButton.setVisibility(View.GONE);
				floatingButton.setVisibility(View.GONE);
				blockUserButton.setVisibility(View.GONE);
				addFriendButton.setVisibility(View.VISIBLE);
				friendsCount--;
				isFriend = false;
				friendsButton.setText(getResources().getString(R.string.friends) + "(" + friendsCount + ")");
				Toast.makeText(context, context.getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class BlockUser extends AsyncTask<Void, Void, Void>{
		
		private boolean res;
		private Context context;
		
		public BlockUser setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			blockUserButton.setClickable(false);
			addFriendButton.setClickable(false);
			unfriendButton.setClickable(false);
			Utils.crossfade(context, progressBar, null);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.blockUser(id);
			
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null){
				return;
			}
			Utils.crossfade(context, null, progressBar);
			blockUserButton.setClickable(true);
			addFriendButton.setClickable(true);
			unfriendButton.setClickable(true);
			if(res){
				unfriendButton.setVisibility(View.GONE);
				floatingButton.setVisibility(View.GONE);
				blockUserButton.setVisibility(View.GONE);
				addFriendButton.setVisibility(View.GONE);
				chatButton.setVisibility(View.GONE);
				unblockButton.setVisibility(View.VISIBLE);
				View miniScoreView = scoreView.findViewById(R.id.layout_miniscore_view);
				View duelView = scoreView.findViewById(R.id.layout_miniscore_duel_view);
				TextView title = (TextView) scoreView.findViewById(R.id.layout_miniscore_duel_title_textview);
				miniScoreView.setVisibility(View.GONE);
				title.setVisibility(View.GONE);
				duelView.setVisibility(View.GONE);
				TextView emptyTextView = (TextView) scoreView.findViewById(R.id.layout_miniscore_empty_textview);
				emptyTextView.setVisibility(View.VISIBLE);
				emptyTextView.setText(context.getResources().getString(R.string.you_blocked));
				Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
				emptyTextView.setTypeface(tf);
				if(isFriend){
					friendsCount--;
				}
				isFriend = false;
				friendsButton.setText(getResources().getString(R.string.friends) + "(" + friendsCount + ")");
				Toast.makeText(context, context.getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class Unblock extends AsyncTask<Void, Void, Void>{
		
		private boolean res;
		private Context context;
		
		public Unblock setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			unblockButton.setClickable(false);
			Utils.crossfade(context, progressBar, null);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.unblock(id);
			
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null){
				return;
			}
			Utils.crossfade(context, null, progressBar);
			unblockButton.setClickable(true);
			if(res){
				blockUserButton.setVisibility(View.VISIBLE);
				addFriendButton.setVisibility(View.VISIBLE);
				chatButton.setVisibility(View.VISIBLE);
				unblockButton.setVisibility(View.GONE);
				setScoreInfo(scores, context, scoreView); 
				
				TextView emptyTextView = (TextView) scoreView.findViewById(R.id.layout_miniscore_empty_textview);
				emptyTextView.setVisibility(View.GONE);
				Toast.makeText(context, context.getResources().getString(R.string.done), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class UploadImage extends AsyncTask<Void, Void, Void>{
		
		private Bitmap original;
		private Context context;		
		private Map<String, String> res;
		
		public UploadImage(Bitmap original, Context context){
			this.context = context;
			this.original = original;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			uploadImageButton.setClickable(false);
			Utils.crossfade(context, progressBar, null);
		}
		
		@Override
		protected Void doInBackground(Void... path) {		
			if(original != null){	            	
            	String encoded = Utils.getBase64EncodedBitmap(original, 100);
            	res = APIHandler.uploadImage(encoded);
            }
            
			return null;			
		}     	
		
		protected void onPostExecute(Void unused) {
			if(getActivity() == null){
				return;
			}
			Utils.crossfade(context, null, progressBar);
			uploadImageButton.setClickable(true);
			if(res != null){
				context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
				.putString("qz_profile_img_url", res.get("profile_img_url"))
				.putString("qz_thumbnail_img_url", res.get("thumbnail_img_url"))
				.commit();	        
				Picasso picasso = Picasso.with(context);
				if(res.get("profile_img_url") != null && res.get("profile_img_url").trim().length() != 0){
					picasso.load(res.get("profile_img_url")).into(profileImageView);
				}
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void showBoosterInfoDialog(){
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_booster_dialog);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_booster_textview);
		TextView shop = (TextView) dialog.findViewById(R.id.layout_booster_shop_textview);
		text.setTypeface(tf);
		if(Utils.getBoosterValue(getActivity()) == 1){
			text.setText(getActivity().getResources().getString(R.string.booster_advantage));
			shop.setText(getActivity().getResources().getString(R.string.booster_buy));
		}else{
			text.setText(getActivity().getResources().getString(R.string.booster_expire) 
					+ ((Utils.getBooosterExpirationTime(getActivity()) - System.currentTimeMillis())/(60 * 1000) + 1) 
					+ getActivity().getResources().getString(R.string.minutes));
			shop.setText(getActivity().getResources().getString(R.string.booster_refresh));
		}
		shop.setTypeface(tf);
		shop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
    			startActivity(new Intent(getActivity(), PurchaseActivity.class));
				dialog.dismiss();
			}
			
		});
		dialog.show();
	}	
	
	@Override
	public void onResume(){
		if(user != null && user.getId().equals(APIHandler.user_id)){
			if(boosterTextView != null && getActivity() != null){
				boosterTextView.setText("x" + Utils.getBoosterValue(getActivity()));
			}
		}
		super.onResume();
	}
}
