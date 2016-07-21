package ru.anov.qzproject.fragments;

import java.util.ArrayList;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.db.DbOpenHelper;
import ru.anov.qzproject.fragments.QuickReturnFragment.QuickReturnState;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.utils.Utils;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

public class ThemesFragment extends Fragment implements OnClickListener{

	private ArrayList<Map<String, String>> themeMapList;
	private ArrayList<Theme> themelist;
	private ArrayList<Theme> popularThemelist;
	private ArrayList<Theme> favoriteThemelist;
	
	//UI
	private Button allButton;
	private Button popularButton;
	private Button favoriteButton;
	public static View buttonsView;
	public View fragmentView;
	public ProgressBar progressBar;
	private ThemelistFragment curFragment;
	
	public static ThemesFragment newInstance(){
		return new ThemesFragment();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	  	  
		View view = inflater.inflate(R.layout.fragment_themes, container, false);
		
		allButton = (Button) view.findViewById(R.id.fragment_themes_all_button);
		popularButton = (Button) view.findViewById(R.id.fragment_themes_popular_button);
		favoriteButton = (Button) view.findViewById(R.id.fragment_themes_favorite_button);
		buttonsView = view.findViewById(R.id.fragment_themes_buttons_layout);
		fragmentView = view.findViewById(R.id.fragment);
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_themes_progressbar);
		
		
	    return view;
	}

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		allButton.setTypeface(tf);
		popularButton.setTypeface(tf);
		favoriteButton.setTypeface(tf);
		QuickReturnFragment.quickReturnState = QuickReturnState.ON_SCREEN;
		super.onActivityCreated(savedInstanceState);
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_themes));
		
		if(savedInstanceState != null){
			return;
		}
		
		new GetThemes().execute();
	}

	@Override
	public void onClick(View v) {
		
		ThemelistFragment fragment = null;
		if(v.getId() == R.id.fragment_themes_all_button){
			fragment = ThemelistFragment.newInstance(getActivity().getString(R.string.all_themes), true, R.id.fragment, themelist, true);

			allButton.setBackground(getResources().getDrawable(R.drawable.sec_button_left_pressed));
			popularButton.setBackground(getResources().getDrawable(R.drawable.sec_button_mid_normal));
			favoriteButton.setBackground(getResources().getDrawable(R.drawable.sec_button_right_normal));
			allButton.setTextColor(getResources().getColor(R.color.White));
			popularButton.setTextColor(getResources().getColor(R.color.Gray));
			favoriteButton.setTextColor(getResources().getColor(R.color.Gray));
			
		}else if(v.getId() == R.id.fragment_themes_popular_button){
			fragment = ThemelistFragment.newInstance(getActivity().getString(R.string.popular), false, R.id.fragment, popularThemelist, true);
			
			allButton.setBackground(getResources().getDrawable(R.drawable.sec_button_left_normal));
			popularButton.setBackground(getResources().getDrawable(R.drawable.sec_button_mid_pressed));
			favoriteButton.setBackground(getResources().getDrawable(R.drawable.sec_button_right_normal));
			allButton.setTextColor(getResources().getColor(R.color.Gray));
			popularButton.setTextColor(getResources().getColor(R.color.White));
			favoriteButton.setTextColor(getResources().getColor(R.color.Gray));
			
		}else if(v.getId() == R.id.fragment_themes_favorite_button){
			fragment = ThemelistFragment.newInstance(getActivity().getString(R.string.favorite), false, R.id.fragment, favoriteThemelist, true);

			allButton.setBackground(getResources().getDrawable(R.drawable.sec_button_left_normal));
			popularButton.setBackground(getResources().getDrawable(R.drawable.sec_button_mid_normal));
			favoriteButton.setBackground(getResources().getDrawable(R.drawable.sec_button_right_pressed));
			allButton.setTextColor(getResources().getColor(R.color.Gray));
			popularButton.setTextColor(getResources().getColor(R.color.Gray));
			favoriteButton.setTextColor(getResources().getColor(R.color.White));
		}
		
		getFragmentManager().popBackStack();
		fragment.setQuickReturnView(buttonsView);
		curFragment = fragment;
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.remove(getFragmentManager().findFragmentById(R.id.fragment));
		ft.replace(R.id.fragment, fragment);
		
		ft.commit();
	}
	
	private class GetThemes extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			themeMapList = ThemeDao.getInstance(getActivity()).getAllThemes(DbOpenHelper.THEMES_TABLE_NAME);
			themelist = Theme.toList(themeMapList);
			popularThemelist = Theme.toPopularList(themeMapList, 10);
			favoriteThemelist = ThemeDao.getInstance(getActivity()).getFavoriteThemes(10);
			return null;
		}
		
		public void onPostExecute(Void unused){
			allButton.setOnClickListener(ThemesFragment.this);
			popularButton.setOnClickListener(ThemesFragment.this);
			favoriteButton.setOnClickListener(ThemesFragment.this);
			ThemelistFragment f = ThemelistFragment.newInstance(getActivity().getString(R.string.all_themes), true, R.id.fragment, themelist, true);
			f.setQuickReturnView(buttonsView);
			curFragment = f;
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.fragment, f);
			ft.commit();
			if(getActivity() != null){
				Utils.crossfade(getActivity(), fragmentView, progressBar);
				Utils.crossfade(getActivity(), buttonsView, null);
			}
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					curFragment.hideQuickReturnView();
				}
			}, 1500);
		}
		
	}
}
