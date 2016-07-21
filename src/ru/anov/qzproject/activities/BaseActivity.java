package ru.anov.qzproject.activities;

import ru.anov.qzproject.R;
import ru.anov.qzproject.db.DbOpenHelper;
import ru.anov.qzproject.fragments.ScoresFragment;
import ru.anov.qzproject.fragments.ThemelistFragment;
import ru.anov.qzproject.fragments.UserFragment;
import ru.anov.qzproject.fragments.UserlistFragment;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;


public class BaseActivity extends ActionBarActivity {
 
	private String id;
	private String fragmentName;
	private String themeId;
	private String title;
	
	private boolean isSearch;
	
	private Fragment currentFragment;
	
	private boolean slide = false;
	private boolean showLocked = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		GCMIntentService.STATE = 0;
		id = getIntent().getExtras().getString("id");
		fragmentName = getIntent().getExtras().getString("fragment");
		themeId = getIntent().getExtras().getString("themeId");
		
		isSearch = getIntent().getExtras().getBoolean("isSearch");
		title = getIntent().getExtras().getString("title");
		slide = getIntent().getExtras().getBoolean("slide");
		showLocked = getIntent().getExtras().getBoolean("showLocked");
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			if(fragmentName.equals(UserlistFragment.class.getSimpleName())){
				currentFragment = UserlistFragment.newInstance(id, false, false, null);
			}else if(fragmentName.equals(UserFragment.class.getSimpleName())){
				currentFragment = UserFragment.newInstance(id);
			}else if(fragmentName.equals(ScoresFragment.class.getSimpleName())){
				currentFragment = ScoresFragment.newInstance(themeId);
			}else if(fragmentName.equals(ThemelistFragment.class.getSimpleName())){
				currentFragment = ThemelistFragment.newInstance(title, isSearch, R.id.container, Theme.toList(ThemeDao.getInstance(this).getAllThemes(DbOpenHelper.THEMES_TABLE_NAME)), showLocked);
			}
			if(currentFragment != null){
				fragmentTransaction.add(R.id.container, currentFragment).commit();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
	    case android.R.id.home:
			if(APIHandler.curTask != null){
				APIHandler.curTask.cancel(true);
			}
	        finish();
	        animateFinish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDestroy(){
		if(GameLine.STATE == 1){
			GameLine.STATE = 0;
		}
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(APIHandler.curTask != null){
			APIHandler.curTask.cancel(true);
		}
		super.onBackPressed();
        animateFinish();
	}
	
	private void animateFinish(){
		if(slide){
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);			
		}
	}
}
