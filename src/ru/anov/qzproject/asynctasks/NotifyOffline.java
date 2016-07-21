package ru.anov.qzproject.asynctasks;

import ru.anov.qzproject.utils.APIHandler;

import android.content.Context;
import android.os.AsyncTask;


public class NotifyOffline extends AsyncTask<Void, Void, Void>{
	//private Friend user;
	//private Theme theme;
	private String rid;
	private String themeId;
	private String themeName;
	private String ansSeq;
	private boolean res;
	private Context context;
	
	public NotifyOffline(String rid, String themeId, String themeName, String ansSeq){
		this.rid = rid;
		this.themeId = themeId;
		this.themeName = themeName;
		this.ansSeq = ansSeq;
	}
	
	public NotifyOffline setContext(Context context){
		this.context = context;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		res = APIHandler.notifyOffline(rid, themeId, themeName, ansSeq);
		return null;
	}
	
	/*public void onPostExecute(Void unused){
		if(res){
			Toast.makeText(context, "Notified Offline", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(context, "Not Notified Offline" + APIHandler.error, Toast.LENGTH_LONG).show();
		}
	}*/
}
