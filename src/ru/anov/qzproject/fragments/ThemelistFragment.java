package ru.anov.qzproject.fragments;

import java.util.ArrayList;
import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.PurchaseActivity;
import ru.anov.qzproject.activities.SubmitQuestionActivity;
import ru.anov.qzproject.adapters.ThemesAdapter;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.utils.Utils;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ThemelistFragment extends QuickReturnFragment{
	
	public static String unlockedId;
	public static boolean unlockAll = false;
	
	private ThemesAdapter themesAdapter;
	private EditText searchEditText;
	private List<Theme> themelist;
	private TextView titleTextView;
	private View searchView;
	private View titleHeaderView;
	
	private View emptyView;
	private TextView emptyTextView;
	private TextView helpTextView;
	
	private View footer;
	private TextView footerTextView;
	private TextView footerHelpTextView;
	
	private boolean isSearch;
	private boolean showLocked;
	private int childContainerId;
	private String title;
	
	public static ThemelistFragment newInstance(String title, boolean isSearch, int childContainerId, ArrayList<Theme> themes, boolean showLocked){
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		bundle.putBoolean("isSearch", isSearch);
		bundle.putBoolean("showLocked", showLocked);
		bundle.putParcelableArrayList("themes", themes);
		bundle.putInt("childContainerId", childContainerId);
		ThemelistFragment themelistFragment = new ThemelistFragment();
		themelistFragment.setArguments(bundle);
		return themelistFragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
//		unlockedId = null;
		isSearch = getArguments().getBoolean("isSearch");
		showLocked = getArguments().getBoolean("showLocked");
		title = getArguments().getString("title");
		themelist = getArguments().getParcelableArrayList("themes");
		childContainerId = getArguments().getInt("childContainerId");
		
        View view = inflater.inflate(R.layout.fragment_themelist, container, false);
        titleHeaderView = inflater.inflate(R.layout.layout_title, null);
        searchEditText = (EditText) view.findViewById(R.id.fragment_themelist_search_edittext);
        titleTextView = (TextView) titleHeaderView.findViewById(R.id.layout_title_textview);
        
        emptyView = view.findViewById(R.id.fragment_themelist_empty_view);
        emptyTextView = (TextView) view.findViewById(R.id.fragment_themelist_empty_textview);
        helpTextView = (TextView) view.findViewById(R.id.fragment_themelist_help_textview);
        
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
        titleTextView.setTypeface(tf);

        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
        emptyTextView.setTypeface(typeface);
        helpTextView.setTypeface(typeface);
        
        searchView = view.findViewById(R.id.fragment_themelist_search_view);
		searchView.setVisibility(View.GONE);
		if(isSearch){
			setHasOptionsMenu(true);
		}else{
		    setHasOptionsMenu(false);
		}
		if(themelist.isEmpty()){
			emptyView.setVisibility(View.VISIBLE);
		}else{
			emptyView.setVisibility(View.GONE);
		}
        return view;
    }
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		menu.removeItem(R.id.action_search_themelist);
		menu.removeItem(R.id.action_search_userlist);
		if(isSearch){
			inflater.inflate(R.menu.search_themelist, menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_search_themelist:
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
	public void onResume(){
		if(unlockedId != null && themelist != null && themesAdapter != null){
			for(Theme t : themelist){
				if(t != null && t.getId().equals(unlockedId)){
					t.setLocked("0");
				}
			}
			themesAdapter.notifyDataSetChanged();
			unlockedId = null;
		}
		if(unlockAll){
			for(Theme t : themelist){
				if(t != null){
					t.setLocked("0");
				}
			}
			themesAdapter.notifyDataSetChanged();
			unlockAll = false;
		}
		super.onResume();
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_themes));
		
		final ListView listView = getListView();
		if(!themelist.isEmpty()){
			footer = getActivity().getLayoutInflater().inflate(R.layout.layout_themelist_footer, listView, false);
	        listView.addFooterView(footer, "footer", false);
	        footerTextView = (TextView) footer.findViewById(R.id.layout_themelist_footer_textview);
	        footerHelpTextView = (TextView) footer.findViewById(R.id.layout_themelist_footer_help_textview);

	        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
	        footerTextView.setTypeface(tf);
	        footerHelpTextView.setTypeface(tf);
	        footerHelpTextView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
//					showHelpDialog();
					startActivity(new Intent(getActivity(), SubmitQuestionActivity.class));
				}
	        });
		}
        listView.addHeaderView(titleHeaderView, "header", false);
        
		if(title == null || title.length() == 0){
        	titleTextView.setVisibility(View.GONE);
        }else{
        	titleTextView.setText(title);
        }
		
		helpTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
//				showHelpDialog();
				startActivity(new Intent(getActivity(), SubmitQuestionActivity.class));
			}
			
		});
		
		if(isSearch){
	        searchEditText.addTextChangedListener(new TextWatcher() {
	
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				
				@Override
				public void afterTextChanged(Editable s) {}
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					if(searchEditText.getText().toString().trim().length() == 0){
						themesAdapter = new ThemesAdapter(getActivity(), android.R.id.list, false, sort(themelist));
						setListAdapter(themesAdapter); 
						return;
					}
					
					List<Theme> matches = getMatchingThemes(themelist, searchEditText.getText().toString().trim());
					themesAdapter = new ThemesAdapter(getActivity(), android.R.id.list, false, sort(matches));
					setListAdapter(themesAdapter); 
				}
			});
		}else{
			searchView.setVisibility(View.GONE);
		}
		
		themesAdapter = new ThemesAdapter(getActivity(), android.R.id.list, false, sort(themelist));
    	setListAdapter(themesAdapter);
    	if(unlockedId != null && themelist != null && themesAdapter != null){
			for(Theme t : themelist){
				if(t != null && t.getId().equals(unlockedId)){
					t.setLocked("0");
				}
			}
			themesAdapter.notifyDataSetChanged();
			unlockedId = null;
		}
	}
	
	@Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
    	
		final ThemesAdapter adapter = (ThemesAdapter)getListAdapter();
    	final Theme theme = (Theme) adapter.getItem(position - 1);
		
    	if("1".equals(theme.getLocked())){
			startActivity(new Intent(getActivity(), PurchaseActivity.class));
    		return;
    	}	
    	
    	if(theme.isParent()){
			ThemelistFragment child = ThemelistFragment.newInstance(theme.getName(), isSearch, 0, theme.getChildren(), showLocked);
			child.setQuickReturnView(ThemesFragment.buttonsView);
    		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.hide(getFragmentManager().findFragmentById(childContainerId));
			fragmentTransaction.add(childContainerId, child);
    		fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}else{
			if(GameLine.STATE == 0){
				if(adapter.isAnimating(position - 1)){
					return;
				}
				if(adapter.isOpen(position - 1)){
					adapter.collapse(v, position - 1);
				}else{
					adapter.expand(v, position - 1);
				}
				
			}else{
				GameLine.STATE = 0;
				GameLine.getInstance().setTheme(theme);
				String rid = GameLine.getInstance().getFriend().getId();
				String themeId = GameLine.getInstance().getTheme().getId();
				String themeName = GameLine.getInstance().getTheme().getName();
				Utils.startGame(getActivity(), rid, themeId, themeName, false, true, true);
			}
		}
	}
	
	private List<Theme> getMatchingThemes(List<Theme> themes, String name){
		List<Theme> result = new ArrayList<Theme>();
		for(Theme theme : themes){
			if(theme.isParent()){
				if((theme.getName().toLowerCase()).contains(name.toLowerCase())){
					result.add(theme);
				}
				List<Theme> children = getMatchingThemes(theme.getChildren(), name);
				result.addAll(children);
			}else{
				if((theme.getName().toLowerCase()).contains(name.toLowerCase())){
					result.add(theme);
				}
			}
		}
		
		return result;
	}
	
	/*private void showHelpDialog(){
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_help_dialog);
		String email = getActivity().getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getString("support_email", "");
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		TextView text = (TextView) dialog.findViewById(R.id.layout_help_textview);
		String message = getActivity().getResources().getString(R.string.help_info) + "\n" + email;
		text.setTypeface(tf);
		text.setText(message);
		dialog.show();
	}*/
	
	private List<Theme> sort(List<Theme> list){
		List<Theme> res = new ArrayList<Theme>();
		
		for(Theme t : list){
			if(!"1".equals(t.getLocked())){
				res.add(t);
			}
		}
		
		if(showLocked){
			for(Theme t : list){
				if("1".equals(t.getLocked())){
					res.add(t);
				}
			}
		}
		
		return res;
	}
	
}
