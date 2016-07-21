package ru.anov.qzproject.fragments;

import java.util.ArrayList;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.ChatActivity;
import ru.anov.qzproject.adapters.ConversationsAdapter;
import ru.anov.qzproject.models.Message;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConversationsFragment extends ListFragment{
	
	private View mainView;
	private TextView errorTextView;
	private ProgressBar progressBar;
	private ConversationsAdapter adapter;
	
	private BroadcastReceiver broadcastReceiver;
	
	public static ConversationsFragment newInstance(){
		return new ConversationsFragment();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

		View view = inflater.inflate(R.layout.fragment_conversations, container, false);
		
		mainView = view.findViewById(R.id.fragment_conversations_view);
		errorTextView = (TextView) view.findViewById(R.id.fragment_conversations_error_textview);
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_conversations_progressbar);

		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
        setHasOptionsMenu(true);
		adapter = new ConversationsAdapter(getActivity(), android.R.id.list, new ArrayList<Message>());
		setListAdapter(adapter);
		return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.messages));
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		Message message = (Message) getListAdapter().getItem(position);
		intent.putExtra("ruserId", message.getRUserId());
		intent.putExtra("name", message.getName());
		intent.putExtra("thumbnail_img_url", message.getThumbnailImgUrl());
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.removeItem(R.id.action_search_userlist);
		menu.removeItem(R.id.action_search_themelist);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onResume(){
//		GCMIntentService.cFragment = this;
		registerReceiver();
    	new GetConversations().setContext(getActivity()).execute();
		super.onResume();
	}
	
	@Override
	public void onStop(){
//		GCMIntentService.cFragment = null;
		unregisterReceiver();
		super.onStop();
	}
	
	@Override 
	public void onDestroy(){
//		GCMIntentService.cFragment = null;
		unregisterReceiver();
		super.onDestroy();
	}
	
	public void onMessage(){
		if(getActivity() != null){
			getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					errorTextView.setText("");
			    	new GetConversations().setContext(getActivity()).execute();
				}
			});
		}
	}
	private class GetConversations extends AsyncTask<Void, Void, Void>{
		
		private Context context;
		private ArrayList<Message> list;
		
		public GetConversations setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			list = MessageDao.getInstance(context).getLastMessages();
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(isCancelled() || getActivity() == null){
				return;
			}
			if(list == null){
				list = new ArrayList<Message>();
			}
			adapter.clear();
			adapter.addAll(list);
			adapter.notifyDataSetChanged();
			if(list.isEmpty()){
				errorTextView.setText(context.getResources().getString(R.string.no_conversations));
				Utils.crossfade(context, errorTextView, progressBar);
			}else{
				Utils.crossfade(context, mainView, progressBar);
			}
		}
	}
	
	private void registerReceiver(){
		GCMIntentService.isConversationsFragmentAvailable = true;
		IntentFilter filter = new IntentFilter();
		filter.addAction(GCMIntentService.ACTION_CONVERSATION_NEW_MESSAGE);
		broadcastReceiver = new BroadcastReceiver() {
			
			@Override
		    public void onReceive(Context context, Intent intent) {
				//do something based on the intent's action
				onMessage();
			}
		};
		getActivity().registerReceiver(broadcastReceiver, filter);
	}
	
	private void unregisterReceiver(){
		GCMIntentService.isConversationsFragmentAvailable = false;
		if(broadcastReceiver == null){
			return;
		}
		getActivity().unregisterReceiver(broadcastReceiver);
		broadcastReceiver = null;
	}
}
