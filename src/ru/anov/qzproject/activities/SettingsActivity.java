package ru.anov.qzproject.activities;

import ru.anov.qzproject.R;
import ru.anov.qzproject.db.DbOpenHelper;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.utils.APIHandler;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private boolean isAvailable;
	private String username;
	private CheckBoxPreference isAvailableCheckboxPref;
	private EditTextPreference usernameEditTextPref;
	private Preference buttonConfirm;
	private Preference buttonLogout;
	private static ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		    progressDialog = new ProgressDialog(SettingsActivity.this);
		    progressDialog.setMessage(getResources().getString(R.string.logout_dots));
		    progressDialog.setCancelable(true);
		    progressDialog.show();
		}
		
        isAvailable = (getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_is_available", "")).equals("1");
        username = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_name", "");
        
        isAvailableCheckboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("qz_is_available_checkbox");
        usernameEditTextPref = (EditTextPreference) getPreferenceManager().findPreference("qz_name_edittext");
        
        isAvailableCheckboxPref.setChecked(isAvailable);
        usernameEditTextPref.setText(username);
        usernameEditTextPref.setSummary(username);
        
        usernameEditTextPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String newUsername = (String) newValue;
				if(isValid(newUsername) == null){
			        username = newUsername;
			        usernameEditTextPref.setSummary(username);
				}else{
					Toast.makeText(SettingsActivity.this, isValid(newUsername), Toast.LENGTH_SHORT).show();
				}
				return false;
			}
        	
        });
        
        buttonConfirm = (Preference) getPreferenceManager().findPreference("button_confirm");
        buttonConfirm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	isAvailable = isAvailableCheckboxPref.isChecked();
            	new SetSettings(username, isAvailable).execute();
                return true;
            }
        });
        

        buttonLogout = (Preference) getPreferenceManager().findPreference("button_logout");
        buttonLogout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) { 
            	new Logout().execute();
                return true;
            }
        });
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
	    case android.R.id.home:
	        this.finish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

	
	private class SetSettings extends AsyncTask<String, String, String>{
		
		private boolean res;
		private String name;
		private boolean isAvailable;
		
		public SetSettings(String name, boolean isAvailable){
			this.name = name;
			this.isAvailable = isAvailable;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		    progressDialog = new ProgressDialog(SettingsActivity.this);
		    progressDialog.setMessage(getResources().getString(R.string.saving_dots));
		    progressDialog.setCancelable(true);
		    progressDialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			res = APIHandler.setSettings(name, isAvailable);
			return null;
		}
		
		protected void onPostExecute(String unused) {
			if(progressDialog != null && progressDialog.isShowing()){
				progressDialog.dismiss();
			}
			if(res){
				getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
					.putString("qz_name", name)
					.putString("qz_is_available", (isAvailable ? "1" : "0"))
					.commit();
				if(!isCancelled()){
					finish();
				}
			}else{
				if(!isCancelled()){
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_to_save), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	}
	
	private class Logout extends AsyncTask<String, String, String>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();		
		    progressDialog = new ProgressDialog(SettingsActivity.this);
		    progressDialog.setMessage(getResources().getString(R.string.logout_dots));
		    progressDialog.setCancelable(true);
		    progressDialog.show();
		}


		@Override
		protected String doInBackground(String... arg0) {
			APIHandler.setAvailability(false);
			ThemeDao.getInstance(SettingsActivity.this).deleteTable(DbOpenHelper.THEMES_TABLE_NAME);
			MessageDao.getInstance(SettingsActivity.this).deleteAllMessages();
			return null;
		}
		
		protected void onPostExecute(String unused) {
			if(progressDialog != null){
				progressDialog.dismiss();	
			}
    		getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
    			//.remove("qz_id")
    			.remove("qz_name")
    			.remove("message_count")
    			.remove("qz_signature")
				.remove("qz_profile_img_url")
				.remove("qz_thumbnail_img_url")
    			.commit();
    		CookieManager.getInstance().removeAllCookie();
    		Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
    		
        	startActivity(intent);
        	finish();
		}
	}
	
	private String isValid(String username){
		if(username.length() < 5){
			return getResources().getString(R.string.register_warning_2);
		}
		
		if(username.length() > 20){
			return getResources().getString(R.string.register_warning_3);
		}
		
		if(username.contains("  ")){
			return getResources().getString(R.string.register_warning_4);
		}
		
		return null;
	}
}
