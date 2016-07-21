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
import android.database.sqlite.SQLiteDatabase;


public class ThemeDao{
	
	private DbOpenHelper dbOpenHelper;
	private static ThemeDao instance;
	private Context context;
	
	private ThemeDao(Context context){
		if(context != null){
			if(this.context == null){
				this.context = context.getApplicationContext();
			}
			dbOpenHelper = new DbOpenHelper(this.context);
		}
	}
	
	public static synchronized ThemeDao getInstance(Context context){
		if(instance == null){
			instance = new ThemeDao(context);
		}		
		return instance;
	}
	
	public synchronized void dropAndCreateFavorite(){
		SQLiteDatabase db = null;
		try {
		    
			db = dbOpenHelper.getReadableDatabase();
			db.execSQL("DROP TABLE IF EXISTS '" + DbOpenHelper.FAVORITES_TABLE_NAME + "'");
			db.execSQL("create table " + DbOpenHelper.FAVORITES_TABLE_NAME + " (" + DbOpenHelper.ID_FIELD + " TEXT UNIQUE, " + DbOpenHelper.NAME_FIELD
				+ " TEXT, " + DbOpenHelper.DESCRIPTION_FIELD + " TEXT, " + DbOpenHelper.PARENT_FIELD + " TEXT, " + DbOpenHelper.POPULARITY_FIELD + " TEXT, " + DbOpenHelper.RANGE_FIELD + " TEXT, " + DbOpenHelper.COUNTER_FIELD + " INTEGER)");
		}catch(Exception e){
		}finally {
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized ArrayList<Theme> getChildList(){
		Cursor c = null;
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getReadableDatabase();
	        String query = "SELECT * FROM " + DbOpenHelper.THEMES_TABLE_NAME;
	        c = db.rawQuery(query, null);
	        
	        ArrayList<Theme> themes = new ArrayList<Theme>();
	        if(c.moveToFirst()){
	        	while(!c.isAfterLast()){
	        		String id = c.getString(c.getColumnIndex(DbOpenHelper.ID_FIELD));
	        		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        		String description = c.getString(c.getColumnIndex(DbOpenHelper.DESCRIPTION_FIELD));
	        		String parent = c.getString(c.getColumnIndex(DbOpenHelper.PARENT_FIELD));
	        		String popularity = c.getString(c.getColumnIndex(DbOpenHelper.POPULARITY_FIELD));
	        		String range = c.getString(c.getColumnIndex(DbOpenHelper.RANGE_FIELD));
	        		String locked = c.getString(c.getColumnIndex(DbOpenHelper.LOCKED_FIELD));
	        		
	        		Map<String, String> theme = new HashMap<String, String>();
	        		theme.put("id", id);
	        		theme.put("name", name);
	        		theme.put("description", description);
	        		theme.put("parent", parent);
	        		theme.put("popularity", popularity);
	        		theme.put("range", range);
	        		theme.put("locked", locked);
	        		
	        		Theme t = new Theme(theme);
	        		if("0".equals(t.getLocked()) && !t.isParent()){
	        			themes.add(t);
	        		}
	                c.moveToNext();
	        	}
	        }
			
			return themes;
	    }catch(Exception e){
	    	return new ArrayList<Theme>();
	    }finally {
	        if (c != null) {
	            c.close();
	        }
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized ArrayList<Theme> getFavoriteThemes(int limit){
		Cursor c = null;
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getReadableDatabase();
	    	//String query = "SELECT * FROM "+ DbOpenHelper.FAVORITES_TABLE_NAME + " WHERE " + DbOpenHelper.ID_FIELD + " IN (SELECT " + DbOpenHelper.ID_FIELD + " FROM " + DbOpenHelper.THEMES_TABLE_NAME + ")";
	    	//String query = "SELECT * FROM " + DbOpenHelper.FAVORITES_TABLE_NAME + " a INNER JOIN " + DbOpenHelper.THEMES_TABLE_NAME + " b ON a.id = b.id";
	    	String query = "SELECT" + 
	    	" a." + DbOpenHelper.ID_FIELD + " AS " + DbOpenHelper.ID_FIELD + 
	    	", b." + DbOpenHelper.NAME_FIELD + " AS " + DbOpenHelper.NAME_FIELD + 
	    	", b." + DbOpenHelper.DESCRIPTION_FIELD + " AS " + DbOpenHelper.DESCRIPTION_FIELD + 
	    	", b." + DbOpenHelper.PARENT_FIELD + " AS " + DbOpenHelper.PARENT_FIELD + 
	    	", b." + DbOpenHelper.POPULARITY_FIELD + " AS " + DbOpenHelper.POPULARITY_FIELD + 
	    	", b." + DbOpenHelper.RANGE_FIELD + " AS " + DbOpenHelper.RANGE_FIELD + 
	    	", b." + DbOpenHelper.LOCKED_FIELD + " AS " + DbOpenHelper.LOCKED_FIELD + 
	    	", a." + DbOpenHelper.COUNTER_FIELD + " AS " + DbOpenHelper.COUNTER_FIELD + 
	    	
	    	" FROM " + DbOpenHelper.FAVORITES_TABLE_NAME + " a INNER JOIN " + DbOpenHelper.THEMES_TABLE_NAME + " b ON a.id = b.id";
	    	
	    	c = db.rawQuery(query, null);
	        
	        ArrayList<Map<String, String>> themes = new ArrayList<Map<String, String>>();
	        if(c.moveToFirst()){
	        	while(!c.isAfterLast()){
	        		String id = c.getString(c.getColumnIndex(DbOpenHelper.ID_FIELD));
	        		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        		String description = c.getString(c.getColumnIndex(DbOpenHelper.DESCRIPTION_FIELD));
	        		String parent = c.getString(c.getColumnIndex(DbOpenHelper.PARENT_FIELD));
	        		String popularity = c.getString(c.getColumnIndex(DbOpenHelper.POPULARITY_FIELD));
	        		String range = c.getString(c.getColumnIndex(DbOpenHelper.RANGE_FIELD));
	        		String locked = c.getString(c.getColumnIndex(DbOpenHelper.LOCKED_FIELD));
	        		String counter = c.getString(c.getColumnIndex(DbOpenHelper.COUNTER_FIELD));
	        		
	        		Map<String, String> theme = new HashMap<String, String>();
	        		theme.put("id", id);
	        		theme.put("name", name);
	        		theme.put("description", description);
	        		theme.put("parent", parent);
	        		theme.put("popularity", popularity);
	        		theme.put("range", range);
	        		theme.put("locked", locked);
	        		theme.put("counter", counter);
	        		
	        		themes.add(theme);
	                c.moveToNext();
	        	}
	        	
	        	Collections.sort(themes, new FavoriteComparator());
	        }
	        
	        ArrayList<Theme> result = new ArrayList<Theme>();
        	for(int i = 0; i < themes.size() && i < limit; i++){
        		//do not add parent themes
        		if(!themes.get(i).get("parent").equals("0")){
        			result.add(new Theme(themes.get(i)));
        		}
        	}
			
			return result;
	    }catch(Exception e){
	    	return new ArrayList<Theme>();
	    }finally {
	        if (c != null) {
	            c.close();
	        }
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized ArrayList<Map<String, String>> getAllThemes(String tableName){
		Cursor c = null;
		SQLiteDatabase db = null;
	    try {
	    	db = dbOpenHelper.getReadableDatabase();
	        String query = "SELECT * FROM " + tableName;
	        c = db.rawQuery(query, null);
	        
	        ArrayList<Map<String, String>> themes = new ArrayList<Map<String, String>>();
	        if(c.moveToFirst()){
	        	while(!c.isAfterLast()){
	        		String id = c.getString(c.getColumnIndex(DbOpenHelper.ID_FIELD));
	        		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	        		String description = c.getString(c.getColumnIndex(DbOpenHelper.DESCRIPTION_FIELD));
	        		String parent = c.getString(c.getColumnIndex(DbOpenHelper.PARENT_FIELD));
	        		String popularity = c.getString(c.getColumnIndex(DbOpenHelper.POPULARITY_FIELD));
	        		String range = c.getString(c.getColumnIndex(DbOpenHelper.RANGE_FIELD));
	        		String locked = c.getString(c.getColumnIndex(DbOpenHelper.LOCKED_FIELD));
	        		
	        		Map<String, String> theme = new HashMap<String, String>();
	        		theme.put("id", id);
	        		theme.put("name", name);
	        		theme.put("description", description);
	        		theme.put("parent", parent);
	        		theme.put("popularity", popularity);
	        		theme.put("range", range);
	        		theme.put("locked", locked);
	        		
	        		themes.add(theme);

	                c.moveToNext();
	        	}
	        }
			
			return themes;
	    }catch(Exception e){
	    	return new ArrayList<Map<String, String>>();
	    }finally {
	        if (c != null) {
	            c.close();
	        }
	        if (db != null) {
	            db.close();
	        }
	    }
	}
	
	public synchronized Theme getTheme(String themeId){
		SQLiteDatabase db = null;
		Cursor c = null;
		try{
			db = dbOpenHelper.getReadableDatabase();
			String query = "SELECT * FROM " + DbOpenHelper.THEMES_TABLE_NAME + " WHERE " + DbOpenHelper.ID_FIELD + " = ?";
	        c = db.rawQuery(query, new String[] {themeId});
	        if(c.moveToFirst()){
	        	String id = c.getString(c.getColumnIndex(DbOpenHelper.ID_FIELD));
	    		String name = c.getString(c.getColumnIndex(DbOpenHelper.NAME_FIELD));
	    		String description = c.getString(c.getColumnIndex(DbOpenHelper.DESCRIPTION_FIELD));
	    		String parent = c.getString(c.getColumnIndex(DbOpenHelper.PARENT_FIELD));
	    		String popularity = c.getString(c.getColumnIndex(DbOpenHelper.POPULARITY_FIELD));
	    		String range = c.getString(c.getColumnIndex(DbOpenHelper.RANGE_FIELD));
	    		String locked = c.getString(c.getColumnIndex(DbOpenHelper.LOCKED_FIELD));
	    		
	    		Map<String, String> theme = new HashMap<String, String>();
	    		theme.put("id", id);
	    		theme.put("name", name);
	    		theme.put("description", description);
	    		theme.put("parent", parent);
	    		theme.put("popularity", popularity);
	    		theme.put("range", range);
	    		theme.put("locked", locked);
	    		
	    		return new Theme(theme);
	        }
	        return null;
		}catch(Exception e){
			return null;
		}finally{
			if(db != null){
				db.close();
			}
			if(c != null){
				c.close();
			}
		}
	}
	
	public synchronized void deleteTable(String tableName){
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			db.delete(tableName, null, null);
		}catch(Exception e){
		}finally{
			if(db != null){
				db.close();
			}
		}
		
	}
	
	public synchronized void updateThemes(List<Map<String, String>> themes, String tableName){
		if(themes == null){
			return;
		}
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			db.delete(tableName, null, null);
			
			for(Map<String, String> theme : themes){
				ContentValues cv = new ContentValues();
		        cv.put(DbOpenHelper.ID_FIELD, theme.get("id"));
		        cv.put(DbOpenHelper.NAME_FIELD, theme.get("name"));
		        cv.put(DbOpenHelper.DESCRIPTION_FIELD, theme.get("description"));
		        cv.put(DbOpenHelper.PARENT_FIELD, theme.get("parent"));
		        cv.put(DbOpenHelper.POPULARITY_FIELD, theme.get("popularity"));
		        cv.put(DbOpenHelper.RANGE_FIELD, theme.get("range"));
		        cv.put(DbOpenHelper.LOCKED_FIELD, theme.get("locked"));
		        
		        db.insert(tableName, null, cv);
			}
		}catch(Exception e){
		}finally{
			if(db != null){
		        db.close();
			}
		}
	}
	
	/*public synchronized void addTheme(Theme theme){
		
		if(theme == null){
			return;
		}
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
	        cv.put(DbOpenHelper.ID_FIELD, theme.getId());
	        cv.put(DbOpenHelper.NAME_FIELD, theme.getName());
	        cv.put(DbOpenHelper.DESCRIPTION_FIELD, theme.getDescription());
	        cv.put(DbOpenHelper.PARENT_FIELD, theme.getParentId());
	        cv.put(DbOpenHelper.POPULARITY_FIELD, theme.getPopularity());
	        cv.put(DbOpenHelper.RANGE_FIELD, theme.getRange());
	        cv.put(DbOpenHelper.LOCKED_FIELD, theme.getLocked());
	        db.insert(DbOpenHelper.THEMES_TABLE_NAME, null, cv);
		}catch(Exception e){
		}finally{
			if(db != null){
				db.close();
			}
		}
	}*/
	
	public synchronized void unlockAll(){
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
	        cv.put(DbOpenHelper.LOCKED_FIELD, "0");
	        db.update(DbOpenHelper.NEW_TABLE_NAME, cv, null, null);
	        db.update(DbOpenHelper.FAVORITES_TABLE_NAME, cv, null, null);
	        db.update(DbOpenHelper.THEMES_TABLE_NAME, cv, null, null);
		}catch(Exception e){
		}finally{
			if(db != null){
				db.close();
			}
		}
	}
	
	public synchronized void unlockTheme(String themeId){
		SQLiteDatabase db = null;
		try{
			db = dbOpenHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
	        cv.put(DbOpenHelper.LOCKED_FIELD, "0");
	        db.update(DbOpenHelper.NEW_TABLE_NAME, cv, DbOpenHelper.ID_FIELD + " = ?", new String[] {themeId});
	        db.update(DbOpenHelper.FAVORITES_TABLE_NAME, cv, DbOpenHelper.ID_FIELD + " = ?", new String[] {themeId});
	        db.update(DbOpenHelper.THEMES_TABLE_NAME, cv, DbOpenHelper.ID_FIELD + " = ?", new String[] {themeId});
    	
		}catch(Exception e){
		}finally{
			if(db != null){
				db.close();
			}
		}
	}
	
	public synchronized void updateFavorites(String themeId){
		int counter = 0;
		SQLiteDatabase db = null;
		Cursor c = null;
		try{
			db = dbOpenHelper.getReadableDatabase();
			String query = "SELECT * FROM " + DbOpenHelper.FAVORITES_TABLE_NAME + " WHERE " + DbOpenHelper.ID_FIELD + " = ?";
	        c = db.rawQuery(query, new String[] {themeId});
	        
	        if(c != null && c.getCount() > 0){
	        	c.moveToFirst();
	        	counter = c.getInt(c.getColumnIndex(DbOpenHelper.COUNTER_FIELD));
	        }
	        counter++;
			
			Theme theme = getTheme(themeId);
			
			if(theme == null){
				return;
			}
			
			if("1".equals(theme.getLocked())){
				return;
			}
			
			db = dbOpenHelper.getWritableDatabase();
			ContentValues cv = new ContentValues();
	        cv.put(DbOpenHelper.ID_FIELD, theme.getId());
	        cv.put(DbOpenHelper.NAME_FIELD, theme.getName());
	        cv.put(DbOpenHelper.DESCRIPTION_FIELD, theme.getDescription());
	        cv.put(DbOpenHelper.PARENT_FIELD, theme.getParentId());
	        cv.put(DbOpenHelper.POPULARITY_FIELD, theme.getPopularity());
	        cv.put(DbOpenHelper.RANGE_FIELD, theme.getRange());
	        cv.put(DbOpenHelper.LOCKED_FIELD, theme.getLocked());
	        cv.put(DbOpenHelper.COUNTER_FIELD, counter);
		       
	        if(counter > 1){
	        	db.update(DbOpenHelper.FAVORITES_TABLE_NAME, cv, DbOpenHelper.ID_FIELD + " = ?", new String[] {themeId});
	    	}else{
		        db.insert(DbOpenHelper.FAVORITES_TABLE_NAME, null, cv);
			}
		}catch(Exception e){
		}finally{
			if(db != null){
				db.close();
			}
			if(c != null){
				c.close();
			}
		}
	}
	
	/*public synchronized boolean contains(String themeId, String tableName){
		Cursor c = null;
		SQLiteDatabase db = null;
	    try {
	        db = dbOpenHelper.getReadableDatabase();
	        String query = "SELECT * FROM " + tableName + " WHERE " + DbOpenHelper.ID_FIELD + " = ?";
	        c = db.rawQuery(query, new String[] {themeId});
	        
	        return c.moveToFirst();
	    }catch(Exception e){
	    	return false;
	    }finally {
	        if (c != null) {
	            c.close();
	        }
	        if (db != null) {
	            db.close();
	        }
	    }
	}*/
	
	private static class FavoriteComparator implements Comparator<Map<String, String>>{
		//desc
		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			long l1 = Long.parseLong(lhs.get("counter"));
			long l2 = Long.parseLong(rhs.get("counter"));
			return (int)(l2 - l1);
		}
		
	}
}
