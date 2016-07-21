package ru.anov.qzproject.activities;

import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.anov.qzproject.R;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;


public class RegisterVKActivity extends ActionBarActivity {

	private WebView webView;
	private static ProgressDialog progressDialog;
	private ProgressBar progressBar;
	private TextView status;
	
	private static final long DELAY_TIME = 2*1000;
	private static final String API_ID = "4378147";
	private static final String API_VERSION = "5.5";
	private static final String REDIRECT_URL = "https://oauth.vk.com/blank.html";
	private static final String SETTINGS = "friends,email,offline";
	private static String ACCESS_TOKEN = "";
	private static String USER_ID = "";
	private String regId;
	private boolean isRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registervk);
		GCMIntentService.STATE = 2;

		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		webView = (WebView) findViewById(R.id.activity_registervk_webview);
		progressBar = (ProgressBar) findViewById(R.id.activity_registervk_progressbar);
		status = (TextView) findViewById(R.id.activity_registervk_status);
		status.setTypeface(tf);
		
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
			progressDialog = new ProgressDialog(RegisterVKActivity.this);
		    progressDialog.setMessage(getResources().getString(R.string.login_dots));
		    progressDialog.setCancelable(false);
		    progressDialog.show();
		}
		
		webView.setWebViewClient(new VKWebViewClient());
		
		String url = getUrl(API_ID, SETTINGS, REDIRECT_URL);
		
		if(!isRunning){
			if(url == null){
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_error), Toast.LENGTH_LONG).show();//TODO
				CookieManager.getInstance().removeAllCookie();
				finish();
			}else{
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
					    progressDialog = new ProgressDialog(RegisterVKActivity.this);
					    progressDialog.setMessage(getResources().getString(R.string.login_dots));
					    progressDialog.setCancelable(false);
					    progressDialog.show();
					}
				});			
				webView.loadUrl(url);
				isRunning = true;
			}
		}
	}
	
	public static String getUrl(String api_id, String settings, String redirect_url){
        
		String url = null;
		try {
			url = "https://oauth.vk.com/authorize?client_id=" + api_id + "&display=mobile&scope=" + settings + "&redirect_uri="
					+ URLEncoder.encode(redirect_url, "UTF-8")+"&response_type=token&v=" + URLEncoder.encode(API_VERSION, "UTF-8");
		} catch (Exception e) {
			url = null;
        	APIHandler.error = e.toString();
		}
 
        return url;
	}

	public static String[] parseRedirectUrl(String url){
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token = extractPattern(url, "access_token=(.*?)&");
        String user_id = extractPattern(url, "user_id=(\\d*)");
        if(user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0){
        	return null;
        }
        return new String[]{access_token, user_id};
    }
	
	public static String extractPattern(String string, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }
	
	private class VKWebViewClient extends WebViewClient{
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String url){
			super.onReceivedError(view, errorCode, description, url);
			Toast.makeText(RegisterVKActivity.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
			RegisterVKActivity.this.finish();
		}
		
		@Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(url.contains("error")){
                if(progressDialog != null && progressDialog.isShowing()){
                	progressDialog.dismiss();
                }
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_error), Toast.LENGTH_LONG).show();//TODO
    			CookieManager.getInstance().removeAllCookie();
    			finish();
            	return;
            }else if(url.contains("access_token")) {
                if(progressDialog != null && progressDialog.isShowing()){
                	progressDialog.dismiss();
                }

            	String[] s = parseRedirectUrl(url);
            	if(s != null && s.length > 1){
	            	ACCESS_TOKEN = s[0]; 
	            	USER_ID = s[1];
	            	
	            	if(!isRunning){
		            	new AsyncTask<String, String, String>(){
		            		
		            		Map<String, String> res;
		            		
		            		@Override
		            		protected void onPreExecute() {
		            			super.onPreExecute();	
		            			APIHandler.curTask = this;
		            			isRunning = true;
		            			progressBar.setVisibility(View.VISIBLE);
		            			webView.setVisibility(View.INVISIBLE);
		            		}
		            		
		            		@Override
							protected String doInBackground(String... params) {
		            			try{
	            					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(RegisterVKActivity.this);
		        	            	regId = gcm.register(APIHandler.SENDER_ID);
									res = APIHandler.registerVk(USER_ID, ACCESS_TOKEN, regId);
								}catch(Exception e){
									res = null;
									APIHandler.error = e.toString();
								}
								return null;
							}
		            		
		            		protected void onPostExecute(String unused) {
		            			isRunning = false;
		            			if(res == null){
		            				if(!isCancelled()){
				            			Toast.makeText(getApplicationContext(), getResources().getString(R.string.login_error), Toast.LENGTH_LONG).show();//TODO
			            				CookieManager.getInstance().removeAllCookie();
				            			finish();
		            				}
		            			}else{
		            				Utils.setRegistrationId(RegisterVKActivity.this, regId);
		            				APIHandler.user_id = res.get("id");
		            				APIHandler.name = res.get("name");
		            				String key = res.get("key");
		        					String profile_img_url = res.get("profile_img_url");
		        					String thumbnail_img_url = res.get("thumbnail_img_url");
		            	        	APIHandler.signature = APIHandler.getHash(APIHandler.user_id, key);
		            	        	
		            	        	getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
			        					.putString("qz_id", APIHandler.user_id)
			        					.putString("qz_name", APIHandler.name)
			        					.putString("qz_signature", APIHandler.signature)
			    						.putString("qz_is_available", "1")
			    						.putString("qz_profile_img_url", profile_img_url)
			    						.putString("qz_thumbnail_img_url", thumbnail_img_url)
			        					.commit();
		            	        	if(res.containsKey("vk_id")){
			            				String vk_id = res.get("vk_id");
			            				if(!isCancelled()){
			            					new SetVKFriends(vk_id, RegisterVKActivity.this).execute();
			            				}
		            				}else{
		            					if(!isCancelled()){
			            					progressBar.setVisibility(View.INVISIBLE);
			            					Intent intent = new Intent(RegisterVKActivity.this, MainActivity.class);
			            					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
			            					startActivity(intent);
			            					finish();
		            					}
		            				}
		            			}
		            		}
		            		
		            	}.execute();
	            	}
            	}
            	return;
            }
        }
		
		@Override
		public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);
            isRunning = false;
            if(progressDialog != null && progressDialog.isShowing()){
            	progressDialog.dismiss();
            }
		}
	}
	
	private class SetVKFriends extends AsyncTask<Void, Void, Void>{
		
		private Map<String, String> res;
		private String vkId;
		private Context context;
		
		public SetVKFriends(String vkId, Context context){
			this.vkId = vkId;
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			isRunning = true;
			APIHandler.curTask = this;
			status.setText(getResources().getString(R.string.looking_for_vk_friends));
			Utils.crossfade(context, status, null);
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.setVKFriends(vkId);
			return null;
		}
		
		protected void onPostExecute(Void unused) {
			isRunning = false;
			if(res == null){
				Utils.crossfade(context, null, status);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_to_find_friends), Toast.LENGTH_LONG).show();
				
				if(!isCancelled()){
					Intent intent = new Intent(RegisterVKActivity.this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}
			}else{
				String count = res.get("count");
				String message = null;
				if(count.equals("0")){
					message = getResources().getString(R.string.friends_are_not_playing);
				}else{
					message = getResources().getString(R.string.you_have) + count + getResources().getString(R.string.friends_playing);
					getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_new_friends_count", count)
						.commit();
				}
				
				if(!isCancelled()){
					final String msg = message;
					new Thread(new Runnable(){
						@Override
						public void run() {
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									status.setText(msg);
									Utils.crossfade(context, null, progressBar);
								}
							});
							
							try {
								Thread.sleep(DELAY_TIME);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									Utils.crossfade(context, null, status);
								}
							});
							
							Intent intent = new Intent(RegisterVKActivity.this, MainActivity.class);
        					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        					startActivity(intent);
        					finish();
						}
						
					}).start();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		if(APIHandler.curTask != null){
			APIHandler.curTask.cancel(true);
		}		
		CookieManager.getInstance().removeAllCookie();
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}
