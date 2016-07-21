package ru.anov.qzproject.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.GameActivity;
import ru.anov.qzproject.asynctasks.Notify;
import ru.anov.qzproject.services.GCMIntentService;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class Utils {

	private static final long CROSSFADE_TIME = 200;
	
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000*3600*24*7;
	public static final long BOOSTER_EXPIRY_TIME_MS = 1000*60*60;
	public static final int MAX_NUMBER_OF_GENERATIONS = 1000;
    public static final String PROPERTY_REG_ID = "registrationId";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
	public static final String PROPERTY_PREF_NAME = "qzPrefName";
		
	public static void popAnim(final View view, Context context){
		if(view == null){
			
			return;
		}
		((Activity) context).runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Animation scaleUp = new ScaleAnimation(1, 2f, 1, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleUp.setDuration(300);
			    scaleUp.setFillAfter(true);
			    final Animation scaleDown = new ScaleAnimation(2f, 1, 2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleDown.setDuration(300);
			    scaleDown.setFillAfter(true);
			    view.startAnimation(scaleUp);
			    view.postDelayed(new Runnable(){

					@Override
					public void run() {
						if(view != null){
							view.startAnimation(scaleDown);
						}
					}
			    	
			    }, 300);
			}
			
		});

	}
	
	public static boolean hasParams(Context context){
		
		return context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).contains("qz_name") && 
				context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).contains("qz_id") &&
				context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).contains("qz_signature"); 
	}
	
	public static int getFinalScore(Context context, 
			int score, 
			boolean hasWon,
			boolean surrendered,
			boolean disconnected){
		
		int booster = getBoosterValue(context);
		if(!surrendered && !disconnected){
			return (score + ((hasWon) ? 100 : 0)) * booster; 
		}else{
			return score * booster;
		}
	}
	
	public static int getLevel(String score){
		long s = Long.parseLong(score);
		
		return 1 + (int)(s/100);
	}
	
	public static int getBoosterValue(Context context){
		if(context == null){
			return 1;
		}
		int value = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getInt("qz_booster_value", 0);
		boolean expired = System.currentTimeMillis() > getBooosterExpirationTime(context);
		
		return (expired) ? 1 : value;
	}
	
	public static long getBooosterExpirationTime(Context context){
		if(context != null){
			return context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).getLong("qz_booster_expiration", 0);
		}
		return 0;
	}
	
	public static void setBooster(Context context, int boost) {
		if(context != null){
	        long boosterExpirationTime = System.currentTimeMillis() + BOOSTER_EXPIRY_TIME_MS;
			context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
				.putInt("qz_booster_value", boost)
				.putLong("qz_booster_expiration", boosterExpirationTime)
				.commit();
		}
    }
	
	public static List<String> getImageIds(Map<String, Map<String, String>> questions){
		List<String> ids = new ArrayList<String>();
		for(Entry<String, Map<String, String>> entry: questions.entrySet()){
			if(entry.getValue().get("has_img").equals("1")){
				ids.add(entry.getKey());
			}
		}
		
		return ids;
	}
	
	public static Map<String, String> getImageUrls(Map<String, Map<String, String>> questions){
		Map<String, String> urls = new HashMap<String, String>();
		for(Entry<String, Map<String, String>> entry: questions.entrySet()){
			if(entry.getValue().get("has_img").equals("1")){
				urls.put(entry.getKey(), entry.getValue().get("img_url"));
			}
		}
		
		return urls;
	}
	
	public static List<String> getQIds(Map<String, Map<String, String>> questions){
		List<String> qIds = new ArrayList<String>();
		for(Entry<String, Map<String, String>> entry: questions.entrySet()){
			qIds.add(entry.getKey());
		}
		
		return qIds;
	}
	
	public static List<String> getQIdsFromSeq(String ansSeq){// id1#answer1;time1_id2#answer2;time2_...
		List<String> ids = new ArrayList<String>();
		
		if(ansSeq.length() == 0){
			return ids;
		}
		
		String[] a = ansSeq.split("_");
		for(String s : a){
			String id = s.split("#")[0];
			if(!ids.contains(id)){
				ids.add(id);
			}
		}
		
		return ids;
	}
	
	public static String getRandomQuestionIdsFromRange(String rangesString){
		if(rangesString == null || rangesString.length() == 0){
			return "";
		}
		
		String[] rangesArray = rangesString.split(";");
		
		List<String> ranges = new ArrayList<String>();
		for(String s : rangesArray){
			ranges.add(s);
		}
		Random r = new Random();
		List<String> qIds = new ArrayList<String>();
		for(int i = 0; i < MAX_NUMBER_OF_GENERATIONS; i++){
			int index = r.nextInt(ranges.size());
			String range = ranges.get(index);
			String[] s = range.split("_");
			int min = Integer.parseInt(s[0]);
			int max = Integer.parseInt(s[1]);
			int gen = min + r.nextInt(max - min + 1);
			if(qIds.size() == 6){
				break;
			}
			if(!qIds.contains(gen + "")){
				qIds.add(gen + "");
			}
		}
		String s = "";
		
		for(String i : qIds){
			s += i + ";";
		}
		return s.substring(0, s.length() - 1);
	}
	
	public static void startOfflineResponseGame(Context context,
			String rid, 
			String themeId, 
			String themeName,
			String ansSeq){
		if(context == null){
			return;
		}
		
		Intent intent = new Intent(context, GameActivity.class);
		
		intent.putExtra("isOfflineResponse", true);
		intent.putExtra("rid", rid);
		intent.putExtra("themeId", themeId);
		intent.putExtra("themeName", themeName);
		intent.putExtra("ansSeq", ansSeq);
		
		if(GCMIntentService.gameActivity != null){
			GCMIntentService.gameActivity.finish();
		}

		context.startActivity(intent);
	}
	
	public static void startGame(Context context, 
			String rid, 
			String themeId, 
			String themeName, 
			boolean isRandom, 
			boolean isRequesting, 
			boolean closePreviousActivity){
		if(context == null){
			return;
		}
		
		if(isRequesting && !isRandom){
			new Notify(rid, themeId, themeName).setContext(context).execute();
		}
		
		Intent intent = new Intent(context, GameActivity.class);
		intent.putExtra("isRandom", isRandom);
		intent.putExtra("isRequesting", isRequesting);
		if(!isRandom){
			intent.putExtra("rid", rid);
		}
		intent.putExtra("themeId", themeId);
		intent.putExtra("themeName", themeName);
		
		if(GCMIntentService.gameActivity != null){
			GCMIntentService.gameActivity.finish();
		}
		
		if(closePreviousActivity){
			((Activity) context).finish();
		}
		context.startActivity(intent);
	}
	
	public static void executeAsyncTask(AsyncTask task, String... params){
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	    }else{
	    	task.execute(params);
	    }
    }
	
	public static void crossfade(Context context, final View contentView, final View loadingView){
		
		if(contentView != null && loadingView != null && contentView.equals(loadingView)){
			return;
		}
		
		if(context == null){
			return;
		}

		((Activity) context).runOnUiThread(new Runnable(){

			@Override
			public void run() {

				if(contentView != null){
					contentView.animate().cancel();
		            contentView.setAlpha(0f);
		            contentView.setVisibility(View.VISIBLE);
		            contentView.animate()
		            	.alpha(1f)
			            .setDuration(CROSSFADE_TIME)
			            .setListener(null);
		        }
		        if(loadingView != null){
		        	loadingView.animate().cancel();
		        	loadingView.animate()
		        		.alpha(0f)
		        		.setDuration(CROSSFADE_TIME)
		        		.setListener(new AnimatorListenerAdapter() {
		        			@Override
		        			public void onAnimationEnd(Animator animation) {
		        				if(loadingView != null){
		        					loadingView.setVisibility(View.GONE);
		        				}
		        			}
		        		});
		        }
	
			}
		});		
	}
	
	public static void showProfileImage(final Bitmap bitmap, final Context context){
		if(bitmap == null || context == null){
			return;
		}
		((Activity)context).runOnUiThread(new Runnable(){

			@Override
			public void run() {
				Dialog dialog = new Dialog(context);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.dialog_profile_image);
				ImageView image = (ImageView) dialog.findViewById(R.id.dialog_profile_imageview);	
				image.setImageBitmap(bitmap);
				dialog.getWindow().setBackgroundDrawable(null);

				dialog.show();	
			}
		});
	}
	
	public static boolean isGCMIdInvalid(Context context){
		String regid = getRegistrationId(context);
		return regid.length() == 0;
	}
	
	public static String getRegistrationId(Context context){
		final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            return "";
        }
        
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isRegistrationExpired(context)) {
            return "";
        }
        return registrationId;
	}
	
	public static void setRegistrationId(Context context, String regId) {
		if(context != null && regId != null){
	        final SharedPreferences prefs = getGCMPreferences(context);
	        int appVersion = getAppVersion(context);
	        SharedPreferences.Editor editor = prefs.edit();
	        editor.putString(PROPERTY_REG_ID, regId);
	        editor.putInt(PROPERTY_APP_VERSION, appVersion);
	        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;
	
	        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
	        editor.commit();
		}
    }
	
	private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
	
	private static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(PROPERTY_PREF_NAME, Context.MODE_PRIVATE);
    }
	
	private static boolean isRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }
	
    public static void copyStream(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
    
    public static String getBase64EncodedBitmap(Bitmap bitmap, int quality){
		//quality 0 - 100;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bao);
        byte[] ba = bao.toByteArray();
        
        String res = new String(Base64.encodeBase64(ba));
        
        return res;
	}
}
