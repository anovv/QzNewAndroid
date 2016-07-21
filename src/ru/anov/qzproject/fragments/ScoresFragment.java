package ru.anov.qzproject.fragments;

import ru.anov.qzproject.R;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class ScoresFragment extends Fragment implements OnClickListener{
	
	private String themeId;
	//UI
	private Button topButton;
	private Button friendsButton;
	
	private ScoreFragment topScoreFragment;
	private ScoreFragment friendsScoreFragment;
	private ScoreFragment curFragment;
	
	public static ScoresFragment newInstance(String themeId){
		Bundle bundle = new Bundle();
		bundle.putString("themeId", themeId);
		ScoresFragment scoresFragment = new ScoresFragment();
		scoresFragment.setArguments(bundle);
		
		return scoresFragment;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		themeId = getArguments().getString("themeId");
		View view = inflater.inflate(R.layout.fragment_scores, container, false);
		topButton = (Button) view.findViewById(R.id.fragment_scores_top_button);
		friendsButton = (Button) view.findViewById(R.id.fragment_scores_friends_button);
		
	    return view;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null){
			return;
		}
		getActivity().getActionBar().setTitle(getActivity().getResources().getString(R.string.title_scores));
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
		topButton.setTypeface(tf);
		friendsButton.setTypeface(tf);
		topButton.setOnClickListener(this);
		friendsButton.setOnClickListener(this);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		topScoreFragment = ScoreFragment.newInstance(themeId, true);
		ft.add(R.id.fragment, topScoreFragment);
		ft.commit();
		curFragment = topScoreFragment;

	}

	@Override
	public void onClick(View v) {
		boolean isTopScore = false;
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if(curFragment != null){
			ft.hide(curFragment);
		}
		
		if(v.getId() == R.id.fragment_scores_top_button){
			isTopScore = true;
			if(topScoreFragment == null){
				topScoreFragment = ScoreFragment.newInstance(themeId, isTopScore);
				ft.add(R.id.fragment, topScoreFragment);
			}else{
				ft.show(topScoreFragment);
			}
			curFragment = topScoreFragment;
			
			topButton.setBackground(getResources().getDrawable(R.drawable.sec_button_left_pressed));
			friendsButton.setBackground(getResources().getDrawable(R.drawable.sec_button_right_normal));
			topButton.setTextColor(getResources().getColor(R.color.White));
			friendsButton.setTextColor(getResources().getColor(R.color.Gray));
		}else if(v.getId() == R.id.fragment_scores_friends_button){
			isTopScore = false;
			if(friendsScoreFragment == null){
				friendsScoreFragment = ScoreFragment.newInstance(themeId, isTopScore);
				ft.add(R.id.fragment, friendsScoreFragment);
			}else{
				ft.show(friendsScoreFragment);
			}
			curFragment = friendsScoreFragment;

			topButton.setBackground(getResources().getDrawable(R.drawable.sec_button_left_normal));
			friendsButton.setBackground(getResources().getDrawable(R.drawable.sec_button_right_pressed));
			friendsButton.setTextColor(getResources().getColor(R.color.White));
			topButton.setTextColor(getResources().getColor(R.color.Gray));
		}
		ft.commit();
	}
}
