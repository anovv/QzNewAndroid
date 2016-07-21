package ru.anov.qzproject.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.adapters.UserlistAdapter;
import ru.anov.qzproject.models.Friend;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class UserlistFragment extends ListFragment{
	
	private String id;
	private boolean isSearch;
	private boolean isSearchRequest;
	private String name;
	private UserlistAdapter userlistAdapter;
	private View userlistView;
	private View errorView;
	private View searchView;
	private Button searchButton;
	private EditText searchEditText;
	private TextView errorTextView;
	private TextView findTextView;
	private Button errorButton;
	private ProgressBar progressBar;
	
	private static List<Friend> userlist;
	
	public static UserlistFragment newInstance(String id, boolean isSearch, boolean isSearchRequest, String name){
		Bundle bundle = new Bundle();
		bundle.putString("id", id);
		bundle.putString("name", name);
		bundle.putBoolean("isSearch", isSearch);
		bundle.putBoolean("isSearchRequest", isSearchRequest);
		UserlistFragment userlistFragment = new UserlistFragment();
		userlistFragment.setArguments(bundle);
		
		return userlistFragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		id = getArguments().getString("id");
		isSearch = getArguments().getBoolean("isSearch");
		isSearchRequest = getArguments().getBoolean("isSearchRequest");
		name = getArguments().getString("name");

		View view = inflater.inflate(R.layout.fragment_userlist, container, false);
		userlistView = view.findViewById(R.id.fragment_userlist_view);
		errorView = view.findViewById(R.id.fragment_userlist_error_view);
		errorTextView = (TextView) view.findViewById(R.id.fragment_userlist_error_textview);
		findTextView = (TextView) view.findViewById(R.id.fragment_userlist_find_textview);
		errorButton = (Button) view.findViewById(R.id.fragment_userlist_error_button);
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_userlist_progressbar);
		searchEditText = (EditText) view.findViewById(R.id.fragment_userlist_search_edittext);
		searchView = view.findViewById(R.id.fragment_userlist_search_view);
		searchButton = (Button) view.findViewById(R.id.fragment_userlist_search_button);
		
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
		findTextView.setTypeface(tf);
		
		if(isSearch){
			setHasOptionsMenu(true);
		}else{
		    setHasOptionsMenu(false);
		}
		return view;
	}
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeItem(R.id.action_search_themelist);
		menu.removeItem(R.id.action_search_userlist);
		if(isSearch){
			inflater.inflate(R.menu.search_userlist, menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_search_userlist:
	    	if(searchView.getVisibility() == View.GONE){
	    		searchView.setVisibility(View.VISIBLE);
	    	}else{
	    		searchView.setVisibility(View.GONE);
	    	}
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(id.equals(APIHandler.user_id)){
			getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.your_friends));
		}else{
			getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_friends));
		}
        
        ListView listView = getListView();
        View header = new View(getActivity());
        header.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
        header.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 50));
        View footer = new View(getActivity());
        footer.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
        footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 50));
        listView.addHeaderView(header, "header", false);
        listView.addFooterView(footer, "footer", false);
        
        if(!isSearchRequest){
        	searchView.setVisibility(View.GONE);
        }
        
        findTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(searchView.getVisibility() == View.GONE){
					searchView.setVisibility(View.VISIBLE);
				}else{
					searchView.setVisibility(View.GONE);
				}
			}
        	
        });
        
        errorButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				getFragmentManager().beginTransaction().replace(R.id.container, UserlistFragment.newInstance(id, isSearch, isSearchRequest, null)).commit();
			}
        	
        });
        
        searchButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(searchEditText.getText().toString().trim().length() == 0){
					return;
				}
				getFragmentManager().beginTransaction().replace(R.id.container, UserlistFragment.newInstance(id, true, true, searchEditText.getText().toString().trim())).commit();
        	}
        	
        });

        searchEditText.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				List<Friend> matches = getMatchingUsers(userlist, searchEditText.getText().toString().trim());
				userlistAdapter = new UserlistAdapter(getActivity(), android.R.id.list, matches);
				setListAdapter(userlistAdapter);
			}
		});
        
        searchEditText.setOnEditorActionListener(new OnEditorActionListener(){  

            @Override 
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) { 
            	
            	if(arg1 == EditorInfo.IME_ACTION_DONE){ 
            		getFragmentManager().beginTransaction().replace(R.id.container, UserlistFragment.newInstance(id, true, true, searchEditText.getText().toString().trim())).commit();
        		}
            	
                return false; 
            }
        }); 
        
        if(savedInstanceState == null){
        	new GetList(isSearchRequest).setContext(getActivity()).execute();
        }
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
		Friend friend = (Friend) getListAdapter().getItem(position - 1);
    	
    	if(GameLine.STATE == 1){
    		GameLine.STATE = 0;
    		GameLine.getInstance().setFriend(friend);

			String rid = GameLine.getInstance().getFriend().getId();
			String themeId = GameLine.getInstance().getTheme().getId();
			String themeName = GameLine.getInstance().getTheme().getName();
			Utils.startGame(getActivity(), rid, themeId, themeName, false, true, true);
    	}else{
			Intent intent = new Intent(getActivity(), BaseActivity.class);
			intent.putExtra("id", friend.getId());
			intent.putExtra("fragment", UserFragment.class.getSimpleName());
			startActivity(intent);
    	}
	}
	
	private class GetList extends AsyncTask<Void, Void, Void>{
		private Context context;
		private boolean isSearchRequest;
		
		public GetList(boolean isSearchRequest){
			this.isSearchRequest = isSearchRequest;
		}
		
		public GetList setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			List<Map<String, String>> res = null;
			if(isSearchRequest){
				res = APIHandler.findUser(name);
			}else{
				res = APIHandler.getFriends(id);
			}
			
			if(res != null){
				userlist = new ArrayList<Friend>();
				for(Map<String, String>  map : res){
					userlist.add(new Friend(map));
				}
			}else{
				userlist = null;
			}
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(isCancelled() || getActivity() == null){
				return;
			}
			if(userlist != null){
				if(id.equals(APIHandler.user_id)){
					String friendsCount = userlist.size() + "";
					context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
						.putString("qz_friends_count", friendsCount)
						.putString("qz_new_friends_count", friendsCount)
						.commit();
				}
				
				if(userlist.isEmpty()){
					Utils.crossfade(context, errorView, progressBar);
					Utils.crossfade(context, userlistView, null);
					if(isSearchRequest){
						errorTextView.setText(context.getResources().getString(R.string.not_found));
						findTextView.setVisibility(View.GONE);
					}else{
						if(id.equals(APIHandler.user_id)){
							errorTextView.setText(context.getResources().getString(R.string.no_friends));
						}else{
							errorTextView.setText(context.getResources().getString(R.string.user_no_friends));
						}
						if(isSearch){
							findTextView.setVisibility(View.VISIBLE);
						}
					}
					errorButton.setClickable(false);
					errorButton.setVisibility(View.INVISIBLE);
				}else{
					if(!isSearchRequest){
						int count = userlist.size();
						if(id.equals(APIHandler.user_id)){
							getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.your_friends) + "(" + count + ")");
						}else{
							getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_friends) + "(" + count + ")");
						}
					}
					userlistAdapter = new UserlistAdapter(getActivity(), android.R.id.list, userlist);
					setListAdapter(userlistAdapter);
					Utils.crossfade(context, userlistView, progressBar);
				}
			}else{
				errorTextView.setText(context.getResources().getString(R.string.no_connection));
				errorButton.setClickable(true);
				errorButton.setVisibility(View.VISIBLE);
				findTextView.setVisibility(View.GONE);
				Utils.crossfade(context, errorView, progressBar);
			}
		}
	}
	
	private List<Friend> getMatchingUsers(List<Friend> userlist, String name){
		List<Friend> res = new ArrayList<Friend>();
		if(userlist == null){
			return res;
		}
		for(Friend friend : userlist){
			if((friend.getName().toLowerCase()).contains(name.toLowerCase())){
				res.add(friend);
			}
		}
		
		return res;	
	}
}
