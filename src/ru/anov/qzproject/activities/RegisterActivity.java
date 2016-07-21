package ru.anov.qzproject.activities;

import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterActivity extends ActionBarActivity {

	private EditText emailEditText;
	private EditText nameEditText;
	private EditText passwordEditText;
	private Button confirmButton;
	private View registerView;
	private ProgressBar registerProgressBar;
	private TextView errorTextView;
	private TextView titleTextView;
	
	private String email;
	private String password;
	private String username;
	private GoogleCloudMessaging gcm;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		GCMIntentService.STATE = 2;

		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		
		emailEditText = (EditText) findViewById(R.id.activity_register_email_edittext);
		nameEditText = (EditText) findViewById(R.id.activity_register_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.activity_register_password_edittext);
		confirmButton = (Button) findViewById(R.id.activity_register_button);
		registerView = findViewById(R.id.activity_register_view);
		registerProgressBar = (ProgressBar) findViewById(R.id.activity_register_progressbar);
		errorTextView = (TextView) findViewById(R.id.activity_register_error_textview);
		titleTextView = (TextView) findViewById(R.id.activity_register_title_textview);

		emailEditText.setTypeface(tf);
		nameEditText.setTypeface(tf);
		passwordEditText.setTypeface(tf);
		confirmButton.setTypeface(tf);
		errorTextView.setTypeface(tf);
		titleTextView.setTypeface(tf);
		
        gcm = GoogleCloudMessaging.getInstance(this);
		confirmButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				register();
			}			
		});
	}
	
	public void register(){

		email = emailEditText.getText().toString().trim();
		password = passwordEditText.getText().toString().trim();
		username = nameEditText.getText().toString().trim();
		
		String s = validate(email, password, username);
		
		if(s.length() == 0){
			Utils.crossfade(this, registerProgressBar, registerView);
			new Register().setContext(this).execute();
		}else{
			errorTextView.setText(s);
		}
	}
	
	private String validate(String email, String password, String username){
		if(email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			return getResources().getString(R.string.register_warning_1);
		}

		if(username.length() < 5){
			return getResources().getString(R.string.register_warning_2);
		}
		
		if(username.length() > 20){
			return getResources().getString(R.string.register_warning_3);
		}
		
		if(username.contains("  ")){
			return getResources().getString(R.string.register_warning_4);
		}
		
		if(password.contains(" ")){
			return getResources().getString(R.string.register_warning_5);
		}
		
		if(password.replaceAll(" ", "").trim().length() < 6){
			return getResources().getString(R.string.register_warning_6);
		}
		
		if(password.replaceAll(" ", "").trim().length() > 12){
			return getResources().getString(R.string.register_warning_7);
		}
		
		return "";
	}
	
	private class Register extends AsyncTask<String, String, String>{		
		
		private Map<String, String> res;
		private Context context;
		private String regId;
		
		
		public Register setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			APIHandler.curTask = this;
			confirmButton.setClickable(false);
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			
			try{
	            gcm = GoogleCloudMessaging.getInstance(context);
	            regId = gcm.register(APIHandler.SENDER_ID);
				res = APIHandler.register(email, password, username, regId);
			}catch(Exception e){
				APIHandler.error = e.toString();
				res = null;
			}
			return null;
		}
		
		protected void onPostExecute(String unused) {
			if(confirmButton != null){
				confirmButton.setClickable(true);
			}
			if(res != null){
				
				if(res.containsKey("code")){
					String code = res.get("code");
					String message = "";
					
					if(code.equals("1")){
						message = getResources().getString(R.string.register_warning_8);
					}
					
					if(code.equals("2")){
						message = getResources().getString(R.string.register_warning_9);
					}

					if(errorTextView != null){
						errorTextView.setText(message);
					}
					Utils.crossfade(context, registerView, registerProgressBar);
					
					return;
				}
				
				if(registerProgressBar != null){
					registerProgressBar.setVisibility(View.INVISIBLE);
				}
				if(context != null){
					Utils.setRegistrationId(context, regId);
					APIHandler.user_id = res.get("id");
					APIHandler.name = res.get("fullname");
					String key = res.get("key");
		        	APIHandler.signature = APIHandler.getHash(APIHandler.user_id, key);
		        	context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_id", APIHandler.user_id)
						.putString("qz_name", APIHandler.name)
						.putString("qz_signature", APIHandler.signature)
						.putString("qz_is_available", "1")
						.putString("qz_profile_img_url", "")
						.putString("qz_thumbnail_img_url", "")
						.commit();	        
		        	Intent intent = new Intent(context, MainActivity.class);
		            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
		            if(!isCancelled()){
			        	context.startActivity(intent);
			        	((Activity) context).finish();
		            }
				}
			}else{
				if(errorTextView != null){
					errorTextView.setText(getResources().getString(R.string.no_connection));
				}
				Utils.crossfade(context, registerView, registerProgressBar);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if(APIHandler.curTask != null){
			APIHandler.curTask.cancel(true);
		}		
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);			
	}
}
