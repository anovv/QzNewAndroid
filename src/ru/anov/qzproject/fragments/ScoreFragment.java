package ru.anov.qzproject.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.BaseActivity;
import ru.anov.qzproject.adapters.ScoreAdapter;
import ru.anov.qzproject.models.Score;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScoreFragment extends ListFragment{
	
	private String themeId;
	private boolean isTopScore;
	
	private View curView;
	
	private ScoreAdapter scoreAdapter;
	private View scoreView;
	private View errorView;
	private TextView errorTextView;
	private Button errorButton;
	private ProgressBar progressBar;
	
	public static ScoreFragment newInstance(String themeId, boolean isTopScore){
		Bundle bundle = new Bundle();
		bundle.putString("themeId", themeId);
		bundle.putBoolean("isTopScore", isTopScore);
		ScoreFragment scoreFragment = new ScoreFragment();
		scoreFragment.setArguments(bundle);
		
		return scoreFragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		themeId = getArguments().getString("themeId");
		isTopScore = getArguments().getBoolean("isTopScore");
		
        View view = inflater.inflate(R.layout.fragment_score, container, false);
	
		scoreView = view.findViewById(R.id.fragment_score_view);
		errorView = view.findViewById(R.id.fragment_score_error_view);
		errorTextView = (TextView) view.findViewById(R.id.fragment_score_error_textview);
		errorButton = (Button) view.findViewById(R.id.fragment_score_error_button);
		progressBar = (ProgressBar) view.findViewById(R.id.fragment_score_progressbar);

		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
        return view;	
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View header = new View(getActivity());
        header.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
        header.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 10));
        View footer = new View(getActivity());
        footer.setBackgroundColor(getActivity().getResources().getColor(android.R.color.transparent));
        footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 10));
        getListView().addHeaderView(header, "header", false);
        getListView().addFooterView(footer, "footer", false);
        errorButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
	        	new GetScore().setContext(getActivity()).execute();
			}
        	
        });

        if(savedInstanceState == null){
        	new GetScore().setContext(getActivity()).execute();
        }
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
		Intent intent = new Intent(getActivity(), BaseActivity.class);
		Score score = (Score) getListAdapter().getItem(position - 1);
		intent.putExtra("id", score.getUserId());
		intent.putExtra("fragment", UserFragment.class.getSimpleName());
		
		startActivity(intent);
	}
	
	private class GetScore extends AsyncTask<Void, Void, Void>{
		private List<Score> scorelist;
		private Context context;
		
		public GetScore setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();	
			Utils.crossfade(context, progressBar, curView);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			List<Map<String, String>> res = null;
			res = APIHandler.getScore(themeId, isTopScore);
			
			if(res != null){
				scorelist = new ArrayList<Score>();
				for(Map<String, String>  map : res){
					scorelist.add(new Score(map));
				}
			}else{
				scorelist = null;
			}
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(isCancelled() || getActivity() == null){
				return;
			}
			if(scorelist != null){
				if(scorelist.isEmpty()){
					Utils.crossfade(context, errorView, progressBar);
					errorTextView.setText(context.getResources().getString(R.string.nobody_played));
					errorButton.setVisibility(View.GONE);
					curView = errorView;
				}else{
					Collections.sort(scorelist, new ScoreComparator());
					scoreAdapter = new ScoreAdapter(getActivity(), android.R.id.list, scorelist);
					setListAdapter(scoreAdapter);
					Utils.crossfade(context, scoreView, progressBar);
					curView = scoreView;
				}
			}else{
				Utils.crossfade(context, errorView, progressBar);
				errorTextView.setText(context.getResources().getString(R.string.no_connection));
				errorButton.setVisibility(View.VISIBLE);
				curView = errorView;
			}
		}
	}
	
	private class ScoreComparator implements Comparator<Score>{

		@Override
		public int compare(Score lhs, Score rhs) {
			int s1 = Integer.parseInt(lhs.getScore());
			int s2 = Integer.parseInt(rhs.getScore());
			
			return s2 - s1;
		}
	}
}
