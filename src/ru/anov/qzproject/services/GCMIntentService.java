package ru.anov.qzproject.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.anov.qzproject.R;
import ru.anov.qzproject.activities.ChatActivity;
import ru.anov.qzproject.activities.GameActivity;
import ru.anov.qzproject.activities.MainActivity;
import ru.anov.qzproject.models.Message;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.receivers.GCMBroadcastReceiver;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService{
	
	public static String ACTION_CHAT_NEW_MESSAGE = "new_message_chat";
	public static String ACTION_CHAT_CLOSE = "close_chat";
	public static String ACTION_CONVERSATION_NEW_MESSAGE = "new_message_conversation";
	
	public static int STATE = 0;
	public static GameActivity gameActivity;
	public static Set<String> friendIds = new HashSet<String>();
	
	public static boolean isChatActivityAvailable = false;
	public static boolean isConversationsFragmentAvailable = false;
	
	public GCMIntentService() {
		super("GCMIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
		
		if(extras != null && !extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
			try{
    			handleMessage(getApplicationContext(), extras);
    	        GCMBroadcastReceiver.completeWakefulIntent(intent);
			}catch(Exception e){}
		}
	}
	
	private static void handleMessage(final Context context, Bundle bundle) {
		String type = bundle.getString("type");
		if(type == null){
			return;
		}
		
		if(type.equals("message_notification")){
			final String ruserId = bundle.getString("id");
			final String name = bundle.getString("name");
			final String thumbnailImgUrl = bundle.getString("thumbnail_img_url");
			String message = bundle.getString("message");
			
			Map<String, String> messageMap = new HashMap<String, String>();
    		messageMap.put("ruser_id", ruserId);
			messageMap.put("name", name);
    		messageMap.put("message", message);
    		messageMap.put("thumbnail_img_url", thumbnailImgUrl);
    		messageMap.put("type", "1");
    		messageMap.put("timestamp", System.currentTimeMillis() + "");
    		
    		final Message msg = new Message(messageMap);
			
    		new Thread(new Runnable(){

				@Override
				public void run() {
					MessageDao.getInstance(context).insertMessage(msg);
					MessageDao.getInstance(context).updateCredentials(ruserId, name, thumbnailImgUrl);

//					if(cFragment != null{
					if(isConversationsFragmentAvailable){
		    			Intent intent = new Intent();
		    			intent.setAction(ACTION_CONVERSATION_NEW_MESSAGE);
		    			context.sendBroadcast(intent);
//						cFragment.onMessage();
					}
				}
    			
    		}).start();

    		//if(chatActivity != null && chatActivity.isSameUser(ruserId)){
    		if(isChatActivityAvailable && ChatActivity.isSameUser(ruserId)){
//    			chatActivity.onMessage(msg);
    			// send broadcast
    			Intent intent = new Intent();
    			intent.putExtra("ruser_id", ruserId);
    			intent.putExtra("name", name);
    			intent.putExtra("message", message);
    			intent.putExtra("thumbnail_img_url", thumbnailImgUrl);
    			intent.putExtra("type", "1");
    			intent.putExtra	("timestamp", System.currentTimeMillis() + "");
    			intent.setAction(ACTION_CHAT_NEW_MESSAGE);
    			context.sendBroadcast(intent);
    			
    		}else{
    			if(STATE == 1){
    				if(gameActivity != null){
    					gameActivity.onMessage(ruserId);
    				}
					return;
				}
    			SharedPreferences sp = context.getSharedPreferences("qz_pref", MODE_PRIVATE);
    			
    			int	messageCount = sp.getInt("message_count_" + ruserId, 0);
    			messageCount++;
    			sp.edit().putInt("message_count_" + ruserId, messageCount).commit();
    			
				messageCount = sp.getInt("message_count", 0);
    			messageCount++;
    			sp.edit().putInt("message_count", messageCount).commit();
    			
    			String isAvailable = sp.getString("qz_is_available", "1");
        		if(isAvailable.equals("1")){
    				if(STATE != 0 && STATE != 1){
    					return;
    				}
    				Intent intent = new Intent(context, ChatActivity.class);
    				intent.putExtra("ruserId", ruserId);
    				intent.putExtra("name", name);
    				intent.putExtra("thumbnail_img_url", thumbnailImgUrl);
    				
    				PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
    							Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|PendingIntent.FLAG_CANCEL_CURRENT);
    				
    				String ticker = name;
    				Notification notification = new NotificationCompat.Builder(context)
    			       .setContentTitle(ticker)
    			       .setContentText(context.getResources().getString(R.string.new_message))
    			       .setTicker(ticker)
    			       .setWhen(System.currentTimeMillis())
    			       .setContentIntent(pendingIntent)
    			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
    			       .setAutoCancel(true)
    			       .setSmallIcon(R.drawable.ic_infinity_notif)
    			       .build();

    				NotificationManager notificationManager = 
    			       (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    				notificationManager.notify(0, notification);
    			}
    		}
    		
    		return;
		}
		
		if(type.equals("game_notification") 
				|| type.equals("offline_notification") 
				|| type.equals("info_notification") 
				|| type.equals("friend_notification")
				|| type.equals("offline_result_notification")){
			if(STATE != 0 && STATE != 1){
				return;
			}
			
			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = null;
			if(type.equals("game_notification")){
			
				String rid = bundle.getString("id");
				String name = bundle.getString("name");
				String themeId = bundle.getString("theme_id");
				String themeName = bundle.getString("theme_name");
				
				if(STATE == 1){
					if(gameActivity != null && rid != null && themeId != null){
						gameActivity.onRematch(rid, themeId);
		    			Intent i = new Intent();
		    			i.setAction(ACTION_CHAT_CLOSE);
		    			i.putExtra("rid", rid);
		    			context.sendBroadcast(i);
					}
					return;
				}
				
				String ticker = name + context.getResources().getString(R.string.wants_to_play);
				notification = new NotificationCompat.Builder(context)
			       .setContentTitle(ticker)
			       .setContentText(themeName)
			       .setTicker(ticker)
			       .setWhen(System.currentTimeMillis())
			       .setContentIntent(pendingIntent)
			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			       .setAutoCancel(true)
			       .setSmallIcon(R.drawable.ic_infinity_notif)
			       .build();
			}else if(type.equals("info_notification")){
				String title = bundle.getString("title");
				String text = bundle.getString("text");
				String longText = bundle.getString("long_text");
				String checkPurchase = bundle.getString("check_purchase");
				String showLong = bundle.getString("show_long");
				String showShare = bundle.getString("show_share");
				String showRate = bundle.getString("show_rate");
				notification = new NotificationCompat.Builder(context)
			       .setTicker(title)
			       .setContentTitle(title)
			       .setContentText(text)
			       .setWhen(System.currentTimeMillis())
			       .setContentIntent(pendingIntent)
			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
			       .setAutoCancel(true)
			       .setSmallIcon(R.drawable.ic_infinity_notif)
			       .build();
				if("1".equals(showLong)){
					context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_info_long_text", longText)
						.putString("qz_info_check_purchase", checkPurchase)
						.commit();
				}
				
				if("1".equals(showShare)){
					context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_info_show_share", "1")
						.commit();
				}
				
				if("1".equals(showRate)){
					context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
						.putString("qz_info_show_rate", "1")
						.commit();
				}
			}else if(type.equals("friend_notification")){
				String id = bundle.getString("id");
				
				if(friendIds.contains(id)){
					return;
				}
				String name = bundle.getString("name");
				String ticker = context.getResources().getString(R.string.you_have_new_friend);
				notification = new NotificationCompat.Builder(context)
			       .setContentTitle(context.getResources().getString(R.string.new_friend))
			       .setContentText(context.getResources().getString(R.string.you_added_by) + name)
			       .setTicker(ticker)
			       .setWhen(System.currentTimeMillis())
			       .setContentIntent(pendingIntent)
			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			       .setAutoCancel(true)
			       .setSmallIcon(R.drawable.ic_infinity_notif)
			       .build();
				
				friendIds.add(id);
				String newCount = context.getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_new_friends_count", "0");
				newCount = "" + (Integer.parseInt(newCount) + 1);
				context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
					.putString("qz_new_friends_count", newCount)
					.commit();
			}else if(type.equals("offline_notification")){
//				String rid = bundle.getString("id");
				String name = bundle.getString("name");
//				String themeId = bundle.getString("theme_id");
				String themeName = bundle.getString("theme_name");
				
				String ticker = name + context.getResources().getString(R.string.left_you_challenge);
				notification = new NotificationCompat.Builder(context)
			       .setContentTitle(ticker)
			       .setContentText(themeName)
			       .setTicker(ticker)
			       .setWhen(System.currentTimeMillis())
			       .setContentIntent(pendingIntent)
			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			       .setAutoCancel(true)
			       .setSmallIcon(R.drawable.ic_infinity_notif)
			       .build();
			}else if(type.equals("offline_result_notification")){
				String rid = bundle.getString("id");
				String name = bundle.getString("name");
				String thumbnailImgUrl = bundle.getString("thumbnail_img_url");
				String themeId = bundle.getString("theme_id");
				String themeName = bundle.getString("theme_name");
				String score = bundle.getString("score");
				String rscore = bundle.getString("rscore");
				String newScore = bundle.getString("new_score");
				
				String ticker = name + context.getResources().getString(R.string.completed_your_challenge);
				notification = new NotificationCompat.Builder(context)
			       .setContentTitle(ticker)
			       .setContentText(themeName)
			       .setTicker(ticker)
			       .setWhen(System.currentTimeMillis())
			       .setContentIntent(pendingIntent)
			       .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
			       .setAutoCancel(true)
			       .setSmallIcon(R.drawable.ic_infinity_notif)
			       .build();
				context.getSharedPreferences("qz_pref", MODE_PRIVATE).edit()
					.putString("qz_offline_result_id", rid)
					.putString("qz_offline_result_name", name)
					.putString("qz_offline_result_theme_id", themeId)
					.putString("qz_offline_result_theme_name", themeName)
					.putString("qz_offline_result_score", score)
					.putString("qz_offline_result_rscore", rscore)
					.putString("qz_offline_result_new_score", newScore)
					.commit();
				
				int scr = Integer.parseInt(score);
				int rscr = Integer.parseInt(rscore);
				
				String message;
				if(scr == rscr){
					message = context.getResources().getString(R.string.draw_in_theme) + themeName;
				}else if(scr > rscr){
					message = context.getResources().getString(R.string.you_won_in_theme) + themeName;
				}else{
					message = context.getResources().getString(R.string.you_lost_in_theme) + themeName;
				}
				Map<String, String> messageMap = new HashMap<String, String>();
	    		messageMap.put("ruser_id", rid);
				messageMap.put("name", name);
	    		messageMap.put("message", message);
	    		messageMap.put("thumbnail_img_url", thumbnailImgUrl);
	    		messageMap.put("type", "2");
	    		messageMap.put("timestamp", System.currentTimeMillis() + "");
	    		
	    		final Message msg = new Message(messageMap);
	    		
	    		MessageDao.getInstance(context).insertMessage(msg);
			}
			
			if(notification == null){
				return;
			}
			 
			NotificationManager notificationManager = 
		       (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(0, notification);
			return;
		}
		
		if(type.equals("online_decline_notification")){
			String id = bundle.getString("id");
			if(gameActivity != null && id != null){
				gameActivity.onDecline(id);
			}
			return;
		}
		
		//TODO add offline_decline_notification
	}
	
	public static void dropFriendIds(){
		friendIds = new HashSet<String>();
	}
}
