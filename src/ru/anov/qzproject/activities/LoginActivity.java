package ru.anov.qzproject.activities;

import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class LoginActivity extends ActionBarActivity {

	private static final long HANDLER_DELAY_TIME = 1000;
	
	private EditText emailEditText;
	private EditText passwordEditText;
	private Button confirmButton;
	private TextView registerTextView;
	private TextView titleTextView;
	private Button registerVKButton;
	private ProgressBar progressBar;
	private View loginView; 
	private View curView;
	private View bg;
	
	private Map<String, String> loginInfo;
	
	private String email;
	private String password;
	private String regId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);	
		GCMIntentService.STATE = 2;
		
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");

		emailEditText = (EditText) findViewById(R.id.activity_login_email_edittext);
		passwordEditText = (EditText) findViewById(R.id.activity_login_password_edittext);
		confirmButton = (Button) findViewById(R.id.activity_login_login_button);
		registerTextView = (TextView) findViewById(R.id.activity_login_register_button);
		titleTextView = (TextView) findViewById(R.id.activity_login_title_textview);
		registerVKButton = (Button) findViewById(R.id.activity_login_registervk_button);
		progressBar = (ProgressBar) findViewById(R.id.activity_login_progressbar);
		loginView = findViewById(R.id.activity_login_view);
		bg = findViewById(R.id.activity_login_bg);
		curView = loginView;
		
		emailEditText.setTypeface(tf);
		passwordEditText.setTypeface(tf);
		confirmButton.setTypeface(tf);
		registerTextView.setTypeface(tf);
		titleTextView.setTypeface(tf);
		registerVKButton.setTypeface(tf);
		
		confirmButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				login();
			}			
		});
		
		registerTextView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		registerVKButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {	
				startActivity(new Intent(LoginActivity.this, RegisterVKActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		overridePendingTransition(0, 0);
		animate();
	}

	private void login(){
		email = emailEditText.getText().toString();
		password = passwordEditText.getText().toString();
		if(isValid(email, password) == null){
			new Login().setContext(this).execute();
		}else{
			Toast.makeText(LoginActivity.this, isValid(email, password), Toast.LENGTH_SHORT).show();
		}
	}
	
	private class Login extends AsyncTask<String, String, String>{
		
		private Context context;
		
		private boolean isNewRegId = false;
		
		public Login setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			APIHandler.curTask = this;
			Utils.crossfade(context, progressBar, curView);			
			curView = progressBar;
			if(confirmButton != null && registerTextView != null && registerVKButton != null){
				confirmButton.setClickable(false);
				registerTextView.setClickable(false);
				registerVKButton.setClickable(false);
			}
		}


		@Override
		protected String doInBackground(String... arg0) {
			regId = Utils.getRegistrationId(context);
        	GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        	isNewRegId = true;
            try {
				regId = gcm.register(APIHandler.SENDER_ID);
	        	loginInfo = APIHandler.login(email, password, regId);
			} catch (Exception e) {
				loginInfo = null;
			}
            
			return null;
		}
		
		protected void onPostExecute(String unused) {
			if(confirmButton != null && registerTextView != null && registerVKButton != null){
				confirmButton.setClickable(true);
				registerTextView.setClickable(true);
				registerVKButton.setClickable(true);
			}
			
	        if(loginInfo != null){
	        	
	        	if(loginInfo.containsKey("code")){
					Utils.crossfade(context, loginView, curView);
		        	Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_credentials), Toast.LENGTH_LONG).show();	
		        	curView = loginView;
	        		return;
	        	}
	        	
	        	if(isNewRegId){
	        		Utils.setRegistrationId(context, regId);
	        	}
	        	if(context != null){
		        	APIHandler.user_id = loginInfo.get("id");
					APIHandler.name = loginInfo.get("name");
					String key = loginInfo.get("key");
					String profile_img_url = loginInfo.get("profile_img_url");
					String thumbnail_img_url = loginInfo.get("thumbnail_img_url");
		        	APIHandler.signature = APIHandler.getHash(APIHandler.user_id, key);
		        	context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_id", APIHandler.user_id)
						.putString("qz_name", APIHandler.name)
						.putString("qz_signature", APIHandler.signature)
						.putString("qz_is_available", "1")
						.putString("qz_profile_img_url", profile_img_url)
						.putString("qz_thumbnail_img_url", thumbnail_img_url)
						.commit();	        
					Utils.crossfade(context, null, curView);
					if(!isCancelled()){
						Handler handler = new Handler();
						handler.postDelayed(new Runnable(){
		
							@Override
							public void run() {
								if(!isCancelled()){
									Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
						        	startActivity(intent);
						        	finish();
								}
							}
							
						}, HANDLER_DELAY_TIME);
					}
	        	}
	        }else{
	        	if(!isCancelled()){
					Utils.crossfade(context, loginView, curView);
		        	Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();	
		        	curView = loginView;
	        	}
	        }
		}
	}

	@Override
	public void onBackPressed() {
		if(APIHandler.curTask != null){
			APIHandler.curTask.cancel(true);
		}		
		super.onBackPressed();
	}
	
	private void animate(){
//		final TextView logo = (TextView) findViewById(R.id.activity_login_logo_textview);

		final ImageView logo = (ImageView) findViewById(R.id.activity_login_logo_imageview);
//		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
//		logo.setTypeface(tf);
	    
	    Integer colorFrom = getResources().getColor(R.color.teal);
		Integer colorTo = getResources().getColor(R.color.White);
		final ValueAnimator toWhite = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
		toWhite.addUpdateListener(new AnimatorUpdateListener() {

		    @Override
		    public void onAnimationUpdate(ValueAnimator animator) {
	    		bg.setBackgroundColor((Integer)animator.getAnimatedValue());
		    }
		});
		toWhite.setDuration(500);
	    
	    new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				toWhite.start();
			}
	    	
	    }, 200);
	    
	    new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				loginView.setVisibility(View.VISIBLE);
				loginView.setAlpha(0.0f);
				loginView.animate().alpha(1.0f).setDuration(600);
			}
	    	
	    }, 300);

	    DisplayMetrics displaymetrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    int height = displaymetrics.heightPixels;
	    int width = displaymetrics.widthPixels;
	    
	    float x = logo.getX();
	    float y = logo.getY();
	    
	    float deltaX_dp = 50;
	    float deltaY_dp = 50;
	    
	    float deltaX_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, deltaX_dp, getResources().getDisplayMetrics());
	    float deltaY_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, deltaY_dp, getResources().getDisplayMetrics());
		
	    logo.animate()
			.scaleXBy(-0.5f)
			.scaleYBy(-0.5f)
			.translationX(-(width - x)/2 + deltaX_px)
			.translationYBy((height - y)/2 - deltaY_px)
			.setDuration(500)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if(logo != null){
						//logo.setTextColor(getResources().getColor(R.color.teal));
					}
				}
			});
	}
	
	private String isValid(String email, String password){
		if(email.trim().length() == 0){
			return getResources().getString(R.string.login_warning_1);
		}

		if(password.trim().length() == 0){
			return getResources().getString(R.string.login_warning_2);
		}
		
		return null;
	}
}
