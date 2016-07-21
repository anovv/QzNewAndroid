package ru.anov.qzproject.asynctasks;

import ru.anov.qzproject.utils.APIHandler;

import android.content.Context;
import android.os.AsyncTask;


public class Notify extends AsyncTask<Void, Void, Void>{
	//private Friend user;
	//private Theme theme;
	private String rid;
	private String themeId;
	private String themeName;
	private boolean res;
	private Context context;
	
	public Notify(String rid, String themeId, String themeName){
		this.rid = rid;
		this.themeId = themeId;
		this.themeName = themeName;
	}
	
	public Notify setContext(Context context){
		this.context = context;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		res = APIHandler.notify(rid, themeId, themeName);
		return null;
	}
	
	/*public void onPostExecute(Void unused){
		if(res){
			Toast.makeText(context, "Notified", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(context, "Not Notified " + APIHandler.error, Toast.LENGTH_LONG).show();
		}
	}*/
}
