package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.adapters.DrawerAdapter;
import ru.anov.qzproject.fragments.ConversationsFragment;
import ru.anov.qzproject.fragments.MainFragment;
import ru.anov.qzproject.fragments.ThemesFragment;
import ru.anov.qzproject.fragments.UserFragment;
import ru.anov.qzproject.fragments.UserlistFragment;
import ru.anov.qzproject.models.GameLine;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.MCrypt;
import ru.anov.qzproject.utils.Utils;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView navigationListView;
	public static int selectedItemPosition;
	public static DrawerAdapter adapter;
	
	private volatile boolean isQuiting = false;
	
	private boolean fromNotification = false;
	
	private static long QUIT_TIME_MILLIS = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        GameLine.STATE = 0;
		GCMIntentService.STATE = 0;
		selectedItemPosition = 0;
		if(Utils.hasParams(this)){
			APIHandler.name = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_name", "");
			APIHandler.user_id = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_id", "");
			APIHandler.signature = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_signature", "");
			try{
				MCrypt mcrypt = new MCrypt();
				APIHandler.dynamic_server_port = Integer.parseInt((mcrypt.decrypt(getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_dynamic_server_port", ""))).trim());
				APIHandler.dynamic_server_ip = (mcrypt.decrypt(getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_dynamic_server_ip", ""))).trim();
//				APIHandler.api_url = (mcrypt.decrypt(getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_api_url", ""))).trim();
				
			}catch(Exception e){}
		}else{
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}

		setContentView(R.layout.activity_main);

		List<String> strs = new ArrayList<String>();
		
		strs.add(getResources().getString(R.string.home));
		strs.add(getResources().getString(R.string.my_profile));
		strs.add(getResources().getString(R.string.friends)); 
		strs.add(getResources().getString(R.string.messages));
		strs.add(getResources().getString(R.string.themes));  
		strs.add(getResources().getString(R.string.shop));
		strs.add(getResources().getString(R.string.submit_question));
		strs.add(getResources().getString(R.string.settings));
		
		adapter = new DrawerAdapter(this, R.layout.item_drawer_normal, strs);
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        navigationListView = (ListView) findViewById(R.id.activity_main_drawer);
        navigationListView.setAdapter(adapter);
        
        navigationListView.setOnItemClickListener(new OnItemClickListener(){
            
        	@Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos,long id){
        		if(pos == 5){
        			startActivity(new Intent(MainActivity.this, PurchaseActivity.class));
        			return;
        		}
        		
        		if(pos == 6){
        			startActivity(new Intent(MainActivity.this, SubmitQuestionActivity.class));
        			return;
        		}
        		
        		if(pos == 7){
        			startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        			return;
        		}
        		
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                
                Fragment fragment = null;
                if(pos == 0){
                	fragment = MainFragment.newInstance();
                }else if(pos == 1){
                	fragment = UserFragment.newInstance(APIHandler.user_id);
                }else if(pos == 2){
                	fragment = UserlistFragment.newInstance(APIHandler.user_id, true, false, null);
                	adapter.setFriendsDelta(0);
                	GCMIntentService.dropFriendIds();
                }else if(pos == 3){
                	fragment = ConversationsFragment.newInstance();
                }else if(pos == 4){
                	fragment = ThemesFragment.newInstance();
                }
                
        		getSupportFragmentManager().popBackStack();
                
        		if(fragment != null){
                	fragmentTransaction = getSupportFragmentManager().beginTransaction();
                	fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_top, R.anim.slide_out_to_bottom);
                	
                	fragmentTransaction.replace(R.id.container, fragment);
                	fragmentTransaction.commit();
                }
                
                drawerLayout.closeDrawer(navigationListView);   
                selectedItemPosition = pos;
                adapter.notifyDataSetChanged();
            }
        });

    	
    	drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close){
	    		
    		@Override
    		public void onDrawerOpened(View view) {
    			adapter.notifyDataSetChanged();
    			super.onDrawerOpened(view);
			}    		
    	};
    	
    	drawerLayout.setDrawerListener(drawerToggle);
    	getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		if (savedInstanceState == null) {
	    	getSupportFragmentManager().popBackStack();
	    	getSupportFragmentManager().beginTransaction()
	    			.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom)
					.replace(R.id.container, MainFragment.newInstance()).commit();
		}
	}
	
	@Override
    public void onResume() {
		super.onResume();
        if(fromNotification){
        	if(GCMIntentService.STATE != 0 && GCMIntentService.STATE != 1){
    			return;
    		}
        	getSupportFragmentManager().popBackStack();
        	if(drawerLayout != null && navigationListView != null){
        		drawerLayout.closeDrawer(navigationListView);   
        	}
        	getSupportFragmentManager().beginTransaction()
        			.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom)
    				.replace(R.id.container, MainFragment.newInstance()).commit();
            fromNotification = false;
    		selectedItemPosition = 0;
    		if(adapter != null){
    			adapter.notifyDataSetChanged();
    		}
        }
        if(adapter != null){
			adapter.notifyDataSetChanged();
		}
     }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onBackPressed(){
    	
    	if(getSupportFragmentManager().getBackStackEntryCount() == 0){
    		if(isQuiting){
    			finish();
    			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);	
    		}else{
    			isQuiting = true;
    			Toast.makeText(this, getResources().getString(R.string.press_back), Toast.LENGTH_SHORT).show();
    			new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						isQuiting = false;
					}
    				
    			}, QUIT_TIME_MILLIS);
    		}
    	}else{
        	super.onBackPressed();
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	fromNotification = true;
    }
}
