package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.adapters.ChatAdapter;
import ru.anov.qzproject.models.Message;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends ActionBarActivity {

	private static String rid;
	
	private String ruserId;
	private String thumbnailImgUrl = "";
	private String name = "";
	
	private ChatAdapter adapter;
	
	private View mainView;
	private ListView listView;
	private EditText editText;
	private Button sendButton;
	private TextView errorTextView;
	private ProgressBar progressBar;
	private MenuItem refreshingMenuItem;

	private volatile boolean isAsyncTaskRunning = false;
	
	private BroadcastReceiver broadcastReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		/*if(GCMIntentService.chatActivity != null){
			GCMIntentService.chatActivity.finish();
		}*/
		
		ruserId = getIntent().getExtras().getString("ruserId");
		thumbnailImgUrl = getIntent().getExtras().getString("thumbnail_img_url");
		name = getIntent().getExtras().getString("name");
		getActionBar().setTitle(name);
		mainView = findViewById(R.id.activity_chat_view);
		listView = (ListView) findViewById(R.id.activity_chat_listview);
		editText = (EditText) findViewById(R.id.activity_chat_edittext);
		sendButton = (Button) findViewById(R.id.activity_chat_send_button);
		errorTextView = (TextView) findViewById(R.id.activity_chat_error_textview);
		progressBar = (ProgressBar) findViewById(R.id.activity_chat_progressbar);

        Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		
		editText.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
	            listView.setSelection(adapter.getCount() - 1);
	        }
			
		});
		
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
	            listView.setSelection(adapter.getCount() - 1);
			}
		});
		
		sendButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String message = editText.getText().toString();
				if(!isAsyncTaskRunning && isValid(message)){
					new SendMessage(message).setContext(ChatActivity.this).execute();
				}
			}
		});

		View footer = new View(this);
        footer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 20));
        listView.addFooterView(footer, "footer", false);
	}
	
	@Override 
	public void onResume(){
		rid = ruserId;
//		GCMIntentService.chatActivity = this;
		registerReceiver();
		NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancelAll();
		SharedPreferences sp = getSharedPreferences("qz_pref", MODE_PRIVATE);
		APIHandler.user_id = sp.getString("qz_id", "");
		APIHandler.signature = sp.getString("qz_signature", "");
		if(sp.contains("message_count_" + ruserId)){
			int counter = sp.getInt("message_count_" + ruserId, 0);
			int msgCounter = sp.getInt("message_count", 0);
			msgCounter -= counter;
			sp.edit().putInt("message_count", msgCounter).commit();
			sp.edit().remove("message_count_" + ruserId).commit();
		}
		new GetMessages().setContext(this).execute();
		super.onResume();
	}
	
	@Override 
	public void onStop(){
//		GCMIntentService.chatActivity = null;
		unregisterReceiver();
		super.onStop();
	}
	
	@Override 
	public void onDestroy(){
//		GCMIntentService.chatActivity = null;
		unregisterReceiver();
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(isTaskRoot()){
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.chat, menu);
		refreshingMenuItem = menu.findItem(R.id.empty);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
	    case android.R.id.home:
	    	finish();
	    	if(isTaskRoot()){
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
			}
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);			
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public void onMessage(final Message message){
		if(message == null 
				|| adapter == null 
				|| listView == null
				|| editText == null
				|| errorTextView == null){
			return;
		}
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				adapter.add(message);
				adapter.notifyDataSetChanged();
		        listView.setSelection(adapter.getCount() - 1);
				errorTextView.setText("");
				
			}
			
		});
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
	
	private class SendMessage extends AsyncTask<Void, Void, Void>{
		private Context context;
		private String message;
		private Message msgToShow;
		private Message msgToSave;
		private Map<String, String> messageMap;
		private Map<String, String> res;
		
		public SendMessage(String message){
			this.message = message;
		}
		
		public SendMessage setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setMenuItemRefreshing(true);
			isAsyncTaskRunning = true;
			messageMap = new HashMap<String, String>();
			messageMap.put("ruser_id", ruserId);
			messageMap.put("name", name);
			messageMap.put("message", message);
			messageMap.put("type", "0");
			messageMap.put("thumbnail_img_url", thumbnailImgUrl);
			messageMap.put("timestamp", "");
			
			msgToShow = new Message(messageMap);
			editText.setText("");
			adapter.add(msgToShow);
			adapter.notifyDataSetChanged();
	        listView.setSelection(adapter.getCount() - 1);
			errorTextView.setText("");
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.sendMessage(ruserId, message);
			if(res != null){
				messageMap.put("timestamp", System.currentTimeMillis() + "");
				msgToSave = new Message(messageMap);
				MessageDao.getInstance(context).insertMessage(msgToSave);
			}
			return null;
		}
		
		public void onPostExecute(Void unused){
			isAsyncTaskRunning = false;
			setMenuItemRefreshing(false);
			adapter.remove(msgToShow);
			if(res != null){
				adapter.add(msgToSave);
			}else{
				Toast.makeText(context, context.getResources().getString(R.string.cant_send_message), Toast.LENGTH_SHORT).show();
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	
	
	private class GetMessages extends AsyncTask<Void, Void, Void>{
		private Context context;
		private ArrayList<Message> list;
		
		public GetMessages setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isAsyncTaskRunning = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			list = MessageDao.getInstance(context).getMessagesForUser(ruserId);
			return null;
		}
		
		public void onPostExecute(Void unused){
			isAsyncTaskRunning = false;
			adapter = new ChatAdapter(context, R.layout.item_ruser_chat, list);
			listView.setAdapter(adapter);
            listView.setSelection(adapter.getCount() - 1);
			if(list.isEmpty()){
				errorTextView.setText(getResources().getString(R.string.no_messages));
			}else{
				errorTextView.setText("");
			}
			Utils.crossfade(context, mainView, progressBar);
			Utils.crossfade(context, errorTextView, progressBar);
			
		}
	}
	
	private boolean isValid(String str){
		return str.trim().length() > 0 && str.trim().length() < 3000;
	}
	
	public static boolean isSameUser(String id){
		if(rid != null){
			return rid.equals(id);
		}
		return false;
	}
	
	private void registerReceiver(){
		GCMIntentService.isChatActivityAvailable = true;
		IntentFilter filter = new IntentFilter();
		filter.addAction(GCMIntentService.ACTION_CHAT_NEW_MESSAGE);
		filter.addAction(GCMIntentService.ACTION_CHAT_CLOSE);
		
		broadcastReceiver = new BroadcastReceiver() {
			
			@Override
		    public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if(GCMIntentService.ACTION_CHAT_CLOSE.equals(action)){
					String id = intent.getExtras().getString("rid");
					if(rid.equals(id)){
						finish();
						overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);	
					}
				}else if(GCMIntentService.ACTION_CHAT_NEW_MESSAGE.equals(action)){
				
					String ruserId = intent.getExtras().getString("ruser_id");
					String name = intent.getExtras().getString("name");
					String message = intent.getExtras().getString("message");
					String thumbnailImgUrl = intent.getExtras().getString("thumbnail_img_url");
					String timestamp = intent.getExtras().getString("timestamp");
					
					Map<String, String> messageMap = new HashMap<String, String>();
		    		messageMap.put("ruser_id", ruserId);
					messageMap.put("name", name);
		    		messageMap.put("message", message);
		    		messageMap.put("thumbnail_img_url", thumbnailImgUrl);
		    		messageMap.put("type", "1");
		    		messageMap.put("timestamp", timestamp);
		    		
		    		final Message msg = new Message(messageMap);
					onMessage(msg);
				}
			}
		};
		registerReceiver(broadcastReceiver, filter);
	}
	
	private void unregisterReceiver(){
		GCMIntentService.isChatActivityAvailable = false;
		if(broadcastReceiver == null){
			return;
		}
		unregisterReceiver(broadcastReceiver);
		broadcastReceiver = null;
	}
}
