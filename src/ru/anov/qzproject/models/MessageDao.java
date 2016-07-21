package ru.anov.qzproject.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.db.DbOpenHelper;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


public class MessageDao {
	private static final int MAX_NUMBER_OF_CONVERSATIONS = 50;
	private static final int MAX_NUMBER_OF_MESSAGES = 500;
	private static final int NUMBER_OF_MESSAGES_TO_DELETE = 100;
	private DbOpenHelper dbOpenHelper;
	private static MessageDao instance;
	private Context context;
	
	private MessageDao(Context context){
		if(this.context == null){
			this.context = context.getApplicationContext();
		}
		
		dbOpenHelper = new DbOpenHelper(this.context);
	}
	
	public static synchronized MessageDao getInstance(Context context){
		if(instance == null){
			instance = new MessageDao(context);
		}		
		return instance;
	}
	
	
	
	public synchronized ArrayList<Message> getMessagesForUser(String ruserId){

		Cursor c = null;
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getReadableDatabase();
			String query = "SELECT * FROM " + DbOpenHelper.MESSAGES_TABLE_NAME + " WHERE " + DbOpenHelper.RUSER_ID_FIELD + " = ?";
			c = db.rawQuery(query, new String[] {ruserId});
	            
	        ArrayList<Message> messages = new ArrayList<Message>();
	        if(c.moveToFirst()){
	        	while(!c.isAfterLast()){
	        		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        		String message = c.getString(c.getColumnIndex(DbOpenHelper.MESSAGE_FIELD));
	        		String thumbnailImgUrl = c.getString(c.getColumnIndex(DbOpenHelper.THUMBNAIL_IMG_URL_FIELD));
	        		String type = c.getString(c.getColumnIndex(DbOpenHelper.TYPE_FIELD));
	        		String timestamp = c.getString(c.getColumnIndex(DbOpenHelper.TIMESTAMP_FIELD));
	        		
	        		Map<String, String> messageMap = new HashMap<String, String>();
	        		messageMap.put("name", name);
	        		messageMap.put("message", message);
	        		messageMap.put("thumbnail_img_url", thumbnailImgUrl);
	        		messageMap.put("type", type);
	        		messageMap.put("ruser_id", ruserId);
	        		messageMap.put("timestamp", timestamp);
	        		
	        		messages.add(new Message(messageMap));

	                c.moveToNext();
	        	}
	        }
	        Collections.sort(messages, new Comparator<Message>(){

				@Override
				public int compare(Message lhs,	Message rhs) {
					try{
						long lt  = Long.parseLong(lhs.getTimestamp());
						long rt  = Long.parseLong(rhs.getTimestamp());
						if(lt == rt){
							return 0;
						}
						if(lt > rt){
							return 1;
						}else{
							return -1;
						}
					}catch(Exception e){
						return 0;
					}
				}
			});
			return messages;
	    }catch(Exception e){
	    	return new ArrayList<Message>();
	    }finally {
	        if (c != null) {
	            c.close();
	        }
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized boolean insertMessage(Message message){
		if(message == null){
			return false;
		}
		String ruserId = message.getRUserId();
		if(ruserId == null){
			return false;
		}
		SQLiteDatabase db = null;
		Cursor c = null;
		Cursor cursor = null;
	    try {
	    	db = dbOpenHelper.getReadableDatabase();
			long count = DatabaseUtils.queryNumEntries(db, DbOpenHelper.MESSAGES_TABLE_NAME,
			                DbOpenHelper.RUSER_ID_FIELD + " = ?", new String[] {ruserId});
			
			if(count > 0){
				if(count >= MAX_NUMBER_OF_MESSAGES){
					c = db.query(DbOpenHelper.MESSAGES_TABLE_NAME, null, DbOpenHelper.RUSER_ID_FIELD + " = ?", new String[]{ruserId}, null, null, DbOpenHelper.TIMESTAMP_FIELD + " ASC", NUMBER_OF_MESSAGES_TO_DELETE + "");
					List<String> idsToRemove = new ArrayList<String>();
					if(c.moveToFirst()){
						while(!c.isAfterLast()){
							String id = c.getString(c.getColumnIndex(DbOpenHelper.ID_FIELD));
			        		if(id != null){
			        			idsToRemove.add(id);
			        		}
							c.moveToNext();
			            }
					}
					if(!removeMessagesByIds(idsToRemove)){
						return false;
					}
				}
			}else{
				List<String> ruserIds = new ArrayList<String>();
				c = db.query(true, DbOpenHelper.MESSAGES_TABLE_NAME, new String[] {DbOpenHelper.RUSER_ID_FIELD}, null, null, DbOpenHelper.RUSER_ID_FIELD, null, null, null);
				if(c.moveToFirst()){
					while(!c.isAfterLast()){
						String ruId = c.getString(c.getColumnIndex(DbOpenHelper.RUSER_ID_FIELD));
		        		if(ruId != null){
		        			ruserIds.add(ruId);
		        		}
						c.moveToNext();
		            }
				}
				if(ruserIds.size() >= MAX_NUMBER_OF_CONVERSATIONS){
					//get latest messages for each user
					cursor = db.rawQuery(
						"SELECT *, MAX(" + DbOpenHelper.TIMESTAMP_FIELD + ")" +
						" FROM " + DbOpenHelper.MESSAGES_TABLE_NAME +
						" GROUP BY " + DbOpenHelper.RUSER_ID_FIELD,
					    null
						);
					List<Map<String, String>> l = new ArrayList<Map<String, String>>();
					if(cursor.moveToFirst()){
						while(!cursor.isAfterLast()){
							String ruId = cursor.getString(cursor.getColumnIndex(DbOpenHelper.RUSER_ID_FIELD));
							String timestamp = cursor.getString(cursor.getColumnIndex(DbOpenHelper.TIMESTAMP_FIELD));
			        		if(ruId != null){
			        			Map<String, String> map = new HashMap<String, String>();
			        			map.put("ruser_id", ruId);
			        			map.put("timestamp", timestamp);
			        			l.add(map);
			        		}
			        		cursor.moveToNext();
			            }
					}
					//sort in asc
					Collections.sort(l, new Comparator<Map<String, String>>(){

						@Override
						public int compare(Map<String, String> lhs,	Map<String, String> rhs) {
							try{
								long lt  = Long.parseLong(lhs.get("timestamp"));
								long rt  = Long.parseLong(rhs.get("timestamp"));
								if(lt == rt){
									return 0;
								}
								if(lt > rt){
									return 1;
								}else{
									return -1;
								}
							}catch(Exception e){
								return 0;
							}
						}
						
					});
					//get the first one and remove
					String userIdToRemove = l.get(0).get("ruser_id");
					removeMessagesByRUserId(userIdToRemove);
				}
			}
			//insert
			db = dbOpenHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
	        cv.put(DbOpenHelper.NAME_FIELD, message.getName());
	        cv.put(DbOpenHelper.MESSAGE_FIELD, message.getMessage());
	        cv.put(DbOpenHelper.TYPE_FIELD, message.getType());
	        cv.put(DbOpenHelper.RUSER_ID_FIELD, message.getRUserId());
	        cv.put(DbOpenHelper.TIMESTAMP_FIELD, message.getTimestamp());
	        cv.put(DbOpenHelper.THUMBNAIL_IMG_URL_FIELD, message.getThumbnailImgUrl());
	        db.insert(DbOpenHelper.MESSAGES_TABLE_NAME, null, cv);

	        return true;
	    }catch(Exception e){
	    	return false;
	    }finally{
	    	if(c != null){
	    		c.close();
	    	}
	    	if(cursor != null){
	    		cursor.close();
	    	}
	        if (db != null) {
	            db.close();
	        }
	    }
	}

	public synchronized boolean removeMessagesByIds(List<String> ids){
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getWritableDatabase();
	    	
	    	String[] idsToRemove = ids.toArray(new String[]{});
	    	String args = TextUtils.join(", ", idsToRemove);
	    	db.execSQL(String.format("DELETE FROM " + DbOpenHelper.MESSAGES_TABLE_NAME + " WHERE " + DbOpenHelper.ID_FIELD + " IN (%s);", args));
	        return true;
	    }catch(Exception e){
	    	return false;
	    }finally {
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized boolean removeMessagesByRUserId(String ruserId){
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getWritableDatabase();
	    	db.delete(DbOpenHelper.MESSAGES_TABLE_NAME, DbOpenHelper.RUSER_ID_FIELD +  " = ?", new String[]{ruserId});

	        return true;
	    }catch(Exception e){
	    	return false;
	    }finally {
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized boolean updateCredentials(String ruserId, String name, String thumbnailImgUrl){
		SQLiteDatabase db = null;
		Cursor c = null;
	    try {
	    	
	    	//check if credentials are different;
	    	db = dbOpenHelper.getReadableDatabase();
	    	String query = "SELECT * FROM " + DbOpenHelper.MESSAGES_TABLE_NAME + " WHERE " + DbOpenHelper.RUSER_ID_FIELD + " = ? LIMIT 1";
			c = db.rawQuery(query, new String[] {ruserId});
	        boolean update = false;
	        
	        if(c.moveToFirst()){
	        	String oldName = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        	String oldThumb = c.getString(c.getColumnIndex(DbOpenHelper.THUMBNAIL_IMG_URL_FIELD));
	        	update = !oldName.equals(name) || !oldThumb.equals(thumbnailImgUrl);
	        }
	    	
	        if(update){
		    	db = dbOpenHelper.getWritableDatabase();
				ContentValues cv = new ContentValues();
		        cv.put(DbOpenHelper.NAME_FIELD, name);
		        cv.put(DbOpenHelper.THUMBNAIL_IMG_URL_FIELD, thumbnailImgUrl);
		        db.update(DbOpenHelper.MESSAGES_TABLE_NAME, cv, DbOpenHelper.RUSER_ID_FIELD + " = ?", new String[] {ruserId});
	    	}
	        return true;
	    }catch(Exception e){
	    	return false;
	    }finally {
	    	if(c != null){
	    		c.close();
	    	}
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized ArrayList<Message> getLastMessages(){
		SQLiteDatabase db = null;
		Cursor c = null;
	    try {
	    	db = dbOpenHelper.getWritableDatabase();
	    	c = db.rawQuery(
				"SELECT *, MAX(" + DbOpenHelper.TIMESTAMP_FIELD + ")" +
				" FROM " + DbOpenHelper.MESSAGES_TABLE_NAME +
				" GROUP BY " + DbOpenHelper.RUSER_ID_FIELD,
			    null
			);
	    	
	    	ArrayList<Message> messages = new ArrayList<Message>();
	    	if(c.moveToFirst()){
		    	while(!c.isAfterLast()){
		    		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        		String message = c.getString(c.getColumnIndex(DbOpenHelper.MESSAGE_FIELD));
	        		String thumbnailImgUrl = c.getString(c.getColumnIndex(DbOpenHelper.THUMBNAIL_IMG_URL_FIELD));
	        		String type = c.getString(c.getColumnIndex(DbOpenHelper.TYPE_FIELD));
		    		String ruserId = c.getString(c.getColumnIndex(DbOpenHelper.RUSER_ID_FIELD));
	        		String timestamp = c.getString(c.getColumnIndex(DbOpenHelper.TIMESTAMP_FIELD));
	        		
	        		Map<String, String> messageMap = new HashMap<String, String>();
	        		messageMap.put("name", name);
	        		messageMap.put("message", message);
	        		messageMap.put("thumbnail_img_url", thumbnailImgUrl);
	        		messageMap.put("type", type);
	        		messageMap.put("ruser_id", ruserId);
	        		messageMap.put("timestamp", timestamp);
	        		
	        		messages.add(new Message(messageMap));
	        		
					c.moveToNext();
	            }
	    	}
	    	
	        Collections.sort(messages, new Comparator<Message>(){

				@Override
				public int compare(Message lhs,	Message rhs) {
					try{
						long lt  = Long.parseLong(lhs.getTimestamp());
						long rt  = Long.parseLong(rhs.getTimestamp());
						if(lt == rt){
							return 0;
						}
						if(lt > rt){
							return -1;
						}else{
							return 1;
						}
					}catch(Exception e){
						return 0;
					}
				}
			});

	        return messages;
		
	    }catch(Exception e){
	    	return new ArrayList<Message>();
	    }finally {
	    	if(c != null){
	    		c.close();
	    	}
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized boolean deleteAllMessages(){
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getWritableDatabase();
	    	db.delete(DbOpenHelper.MESSAGES_TABLE_NAME, null, null);
	    	return true;
	    }catch(Exception e){
	    	return false;
	    }finally {
	        if (db != null) {
	            db.close();
	        }
	    }
	}
}
