package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.adapters.CommentsAdapter;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsActivity extends ActionBarActivity{

	private CommentsAdapter adapter;

	private View mainView;
	private ListView listView;
	private EditText editText;
	private Button sendButton;
	private TextView errorTextView;
	private ProgressBar progressBar;
	private View headerView;
	
	private MenuItem refreshingMenuItem;
	
	private String themeId;
	private int index = 0;
	private boolean isLastComment = false;
	private boolean isFirstLoading = true;
	private boolean isTouched = false;
	private int prevSize = 0;
	
	private volatile boolean isAsyncTaskRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(savedInstanceState != null){
			return;
		}

		themeId = getIntent().getExtras().getString("themeId");
		
		mainView = findViewById(R.id.activity_comments_view);
		listView = (ListView) findViewById(R.id.activity_comments_listview);
		editText = (EditText) findViewById(R.id.activity_comments_edittext);
		sendButton = (Button) findViewById(R.id.activity_comments_send_button);
		errorTextView = (TextView) findViewById(R.id.activity_comments_error_textview);
		progressBar = (ProgressBar) findViewById(R.id.activity_comments_progressbar);
		
		View headerViewContainer = getLayoutInflater().inflate(R.layout.layout_header_comments, null);
		headerView = headerViewContainer.findViewById(R.id.layout_header_comments_view);
		listView.addHeaderView(headerViewContainer);
		headerView.setVisibility(View.GONE);
		
        Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		adapter = new CommentsAdapter(this, R.layout.item_comment, new ArrayList<Map<String, String>>());
		listView.setAdapter(adapter);
		
		listView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isTouched = true;
				return false;
			}
			
		});
		
		listView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				if(!listView.canScrollVertically(-1) && isTouched && !isLastComment){
					isTouched = false;
					if(!isAsyncTaskRunning){
						new GetComments().setContext(CommentsActivity.this).execute();
					}
				}
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - 1;
				Map<String, String> map = adapter.getItem(position);
				String name = map.get("name");
				listView.setSelection(adapter.getCount() - 1);
				editText.setText(name + ", ");
				editText.requestFocus();
				editText.setSelection(editText.getText().toString().length());
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
			
		});
		
		sendButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String comment = editText.getText().toString();
				if(!isValid(comment)){
					return;
				}
				if(!isAsyncTaskRunning){
					new PostComment(comment).setContext(CommentsActivity.this).execute();
				}
			}
		});

		new GetComments().setContext(this).execute();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.comments, menu);
		refreshingMenuItem = menu.findItem(R.id.refresh);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
	    case android.R.id.home:
	    	finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);			
	    	return true;
	    case R.id.refresh:
	    	if(!isAsyncTaskRunning){
	    		new GetComments(0).setContext(this).execute();
	    	}
		    
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private class GetComments extends AsyncTask<Void, Void, Void>{
		private List<Map<String, String>> res;
		private Context context;
		private int ind = -1;
		
		public GetComments(){
			
		}
		
		public GetComments(int ind){
			this.ind = ind;
		}
		
		public GetComments setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setMenuItemRefreshing(true);
			isAsyncTaskRunning = true;
			if(headerView != null){
				headerView.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if(ind == -1){
				res = APIHandler.getComments(themeId, index);
			}else{
				res = APIHandler.getComments(themeId, ind);
			}
			
			return null;
		}
		
		public void onPostExecute(Void unused){
			isAsyncTaskRunning = false;
			setMenuItemRefreshing(false);
			if(isCancelled()){
				return;
			}
			
			if(headerView != null){
				headerView.setVisibility(View.GONE);
			}
			if(res != null){
				for(Map<String, String> map : res){
					if(map.containsKey("is_last")){
						String isLastString = map.get("is_last");
						isLastComment = (isLastString.equals("1"));
						res.remove(map);
						break;
					}
				}
				
				fillAdapter(res, ind != -1);
				adapter.notifyDataSetChanged();
				if(ind != -1){
					listView.setSelection(adapter.getCount() - 1);
				}else{
					listView.setSelection(adapter.getCount() - 1 - prevSize);
				}
				prevSize = adapter.getCount();
				if(adapter.isEmpty()){
					Utils.crossfade(context, errorTextView, progressBar);
					Utils.crossfade(context, mainView, null);
					errorTextView.setText(context.getResources().getString(R.string.no_comments));
					
				}else{
					errorTextView.setText("");
					if(isFirstLoading){
						Utils.crossfade(context, mainView, progressBar);
						isFirstLoading = false;
					}
					if(ind == -1){
						index++;
					}
				}
			}else{
				if(isFirstLoading){
					Utils.crossfade(context, errorTextView, progressBar);
					Utils.crossfade(context, mainView, null);
					errorTextView.setText(context.getResources().getString(R.string.no_connection));
				}else{
					Toast.makeText(context, context.getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	private class PostComment extends AsyncTask<Void, Void, Void>{
		private Map<String, String> res;
		private Context context;
		private String comment;
		private Map<String, String> commentMap;
		
		public PostComment(String comment){
			this.comment = comment;
			commentMap = new HashMap<String, String>();
		}
		
		public PostComment setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setMenuItemRefreshing(true);
			isAsyncTaskRunning = true;
			commentMap.put("id", "");
			commentMap.put("user_id", APIHandler.user_id);
			commentMap.put("comment", comment);
			String nowTimestamp = getResources().getString(R.string.now);
			commentMap.put("timestamp", nowTimestamp);
			
			String name = getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_name", "");
			String thumbnailImgUrl = getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_thumbnail_img_url", "");
			
			commentMap.put("name", name);
			commentMap.put("thumbnail_img_url", thumbnailImgUrl);
			
			String status = getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_status", "");
			String best_in = getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("qz_best_in", "");
			commentMap.put("status", status);
			commentMap.put("best_in", best_in);
			
			adapter.add(commentMap);
			adapter.notifyDataSetChanged();
            listView.setSelection(adapter.getCount() - 1);
			prevSize = adapter.getCount();
			editText.setText("");
			errorTextView.setText("");
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.postComment(themeId, comment);
			return null;
		}
		
		public void onPostExecute(Void unused){
			isAsyncTaskRunning = false;
			setMenuItemRefreshing(false);
			if(isCancelled()){
				return;
			}
			if(res == null){
				adapter.remove(commentMap);
				adapter.notifyDataSetChanged();
				prevSize = adapter.getCount();
				Toast.makeText(context, getResources().getString(R.string.unable_to_post), Toast.LENGTH_SHORT).show();
			}else{
				String best_in = res.get("best_in");
				String status = res.get("status");
				Editor editor = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit();
				if(status != null && status.trim().length() != 0 && !status.equals("null")){//lol
					commentMap.put("status", status);
					editor.putString("qz_status", status);
				}else{
					editor.putString("qz_status", "");
					commentMap.put("status", "");
				}
				if(best_in != null && best_in.trim().length() != 0 && !best_in.equals("null")){//lol
					commentMap.put("best_in", best_in);
					editor.putString("qz_best_in", best_in);
				}else{
					editor.putString("qz_best_in", "");
					commentMap.put("qz_best_in", "");
				}
				editor.commit();
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);	
	}
	
	private void setMenuItemRefreshing(boolean refreshing) {
		if(isAsyncTaskRunning){
			return;
		}
	    if(refreshingMenuItem == null){
	    	return;
	    }

	    if(refreshing){
	    	refreshingMenuItem.setActionView(R.layout.layout_actionbar_refresh);
	    }else{
	        refreshingMenuItem.setActionView(null);
	    }
	}
	
	private void fillAdapter(List<Map<String, String>> list, boolean full){
		if(!full){
			for(int i = list.size() - 1; i >= 0; i--){
				Map<String, String> map = list.get(i);
				adapter.insert(map, 0);
			}
			return;
		}
		
		//add new post, replacing old ones
		for(Map<String, String> map: list){
			if(!map.containsKey("id") || map.get("id").equals("")){
				continue;
			}
			
			int index = containsId(map.get("id"));
			
			if(index != -1){
				adapter.remove(adapter.getItem(index));
			}
			adapter.add(map);
		}
		
		//remove empty ids
		for(int i = 0; i < adapter.getCount(); i++){
			Map<String, String> m = adapter.getItem(i);
			if(!m.containsKey("id") || m.get("id").equals("")){
				adapter.remove(m);
			}
		}
		
		//sort
		adapter.sort(new Comparator<Map<String, String>>(){

			@Override
			public int compare(Map<String, String> lhs, Map<String, String> rhs) {
				
				String lid = lhs.get("id");
				String rid = rhs.get("id");
				try{
					return Integer.parseInt(lid) - Integer.parseInt(rid);
				}catch(Exception e){
					return 1;
				}
			}
			
		});
	}
	
	private int containsId(String id){
		for(int i = 0; i < adapter.getCount(); i++){
			Map<String, String> m = adapter.getItem(i);
			if(m.containsKey("id") && m.get("id").equals(id)){
				return i;
			}
		}
		
		return -1;
	}
	
	private boolean isValid(String str){
		return str.trim().length() > 0 && str.trim().length() < 3000;
	}
}
