package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.billing.IabHelper;
import ru.anov.qzproject.billing.IabHelper.QueryInventoryFinishedListener;
import ru.anov.qzproject.billing.IabResult;
import ru.anov.qzproject.billing.Inventory;
import ru.anov.qzproject.billing.Purchase;
import ru.anov.qzproject.db.DbOpenHelper;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.MCrypt;
import ru.anov.qzproject.utils.Utils;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class SplashActivity extends ActionBarActivity {
	
	private IabHelper mHelper;
	private List<String> purchasedSkus;
	private QueryInventoryFinishedListener mQueryFinishedListener;
	private TextView titleTextView;
	private ProgressBar progressBar;
	
	private static final long QUIT_TIME_MILLIS = 2000;
	private static final long SHOW_PB_TIME_MILLIS = 4000;
	private static final long SHOW_SPLASH_TIME_MILLIS = 2000;
	private long startTime = 0;
	private volatile boolean isQuiting = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		titleTextView = (TextView) findViewById(R.id.activity_splash_textview);
		titleTextView.setTypeface(tf);
		progressBar = (ProgressBar) findViewById(R.id.activity_splash_progressbar);
		GCMIntentService.STATE = 2;

		if (savedInstanceState != null) {
			return;
		}
		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.crossfade(SplashActivity.this, progressBar, null);
				Utils.crossfade(SplashActivity.this, null, titleTextView);
			}
			
		}, SHOW_PB_TIME_MILLIS);
		
		mQueryFinishedListener = new QueryInventoryFinishedListener(){
			
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				if (result.isFailure()) { 
					new FetchServerInfo(new ArrayList<String>()).execute();
				}else{
					List<Purchase> purchases = inventory.getAllPurchases();
					
					if(purchases == null){
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
						finish();
					}
					
					purchasedSkus = new ArrayList<String>();
					
					for(Purchase p : purchases){
						purchasedSkus.add(p.getSku());
					}
					
					new FetchServerInfo(purchasedSkus).execute();
				}
			}
		};
		

		mHelper = new IabHelper(this, APIHandler.RSA_KEY);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					if(mHelper != null){
						mHelper.queryInventoryAsync(true, null, mQueryFinishedListener);
					}
				}else{
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
					new FetchServerInfo(new ArrayList<String>()).execute();
				}            
			}
		});
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void onDestroy() {
	   super.onDestroy();
	   if(mHelper != null){
		   mHelper.dispose();
	   }
	   mHelper = null;
	}
	
	private class FetchServerInfo extends AsyncTask<Void, Void, Void>{
		
		private List<Map<String, String>> res;
		private List<String> skus;
		
		public FetchServerInfo(List<String> skus){
			this.skus = skus;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			APIHandler.curTask = this;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String id = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_id", null);
			String gcmId = null;
			if(id != null){
				if(Utils.isGCMIdInvalid(SplashActivity.this)){
					try{
						GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(SplashActivity.this);
						gcmId = gcm.register(APIHandler.SENDER_ID);
					}catch(Exception e){
						gcmId = null;
					}
				}
			}
			res = APIHandler.fetchServerInfo(skus, id, gcmId);
			if(res != null){
				Utils.setRegistrationId(SplashActivity.this, gcmId);
				List<Map<String, String>> themes = new ArrayList<Map<String, String>>();
				for(Map<String, String> map : res){
					if(!map.containsKey("dynamic_server_ip") && !map.containsKey("new") && !map.containsKey("friends_count")){
						themes.add(map);
					}
				}
				if(themes.isEmpty()){
					ThemeDao.getInstance(SplashActivity.this).dropAndCreateFavorite();
				}
				ThemeDao.getInstance(SplashActivity.this).updateThemes(themes, DbOpenHelper.THEMES_TABLE_NAME);
			}
			
			long curTime = System.currentTimeMillis();
			if(curTime - startTime < SHOW_SPLASH_TIME_MILLIS){
				long sleepTime = SHOW_SPLASH_TIME_MILLIS - (curTime - startTime);
				try{
					Thread.sleep(sleepTime);
				}catch(Exception e){}
			}
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			if(res != null){
				Map<String, String> info = new HashMap<String, String>();
				String new_ids = "";
				String friendsCount = "0";
				for(Map<String, String> map : res){
					if(map.containsKey("dynamic_server_ip")){
						info = map;
					}else if(map.containsKey("new")){
						new_ids = map.get("new");
					}else if(map.containsKey("friends_count")){
						friendsCount = map.get("friends_count");
					}
				}
				
				getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
					.putString("qz_new_ids", new_ids)
					.putString("qz_new_friends_count", friendsCount)
					.commit();
				
				MCrypt mcrypt = new MCrypt();
				String dynamic_server_ip = (mcrypt.decrypt(info.get("dynamic_server_ip"))).trim();
				String dynamic_server_port = (mcrypt.decrypt(info.get("dynamic_server_port"))).trim();
				String random_wait_millis = (mcrypt.decrypt(info.get("random_wait_millis"))).trim();
				
				if(dynamic_server_ip != null && dynamic_server_port != null){
					
					APIHandler.dynamic_server_ip = dynamic_server_ip; 
					APIHandler.dynamic_server_port = Integer.parseInt(dynamic_server_port);
					
					getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_dynamic_server_ip", info.get("dynamic_server_ip"))
						.putString("qz_dynamic_server_port", info.get("dynamic_server_port"))
						.putString("random_wait_millis", random_wait_millis)
						.commit();
					
					Intent intent = null;
					
					if(getSharedPreferences("qz_pref", MODE_PRIVATE) != null && getSharedPreferences("qz_pref", MODE_PRIVATE).contains("qz_signature")){
						intent = new Intent(SplashActivity.this, MainActivity.class);//logged in
					}else{
						intent = new Intent(SplashActivity.this, LoginActivity.class);// not logged in
					}
					if(!isCancelled()){
						startActivity(intent);
						finish();
					}
				}else{
					if(!isCancelled()){
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
						finish();
					}
				}
			}else{
				if(!isCancelled()){
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
					finish();
				}
			}
		}
	}
	
    @Override
    public void onBackPressed(){
		if(isQuiting){
			if(APIHandler.curTask != null){
				APIHandler.curTask.cancel(false);
			}
			finish();
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
    }
}
