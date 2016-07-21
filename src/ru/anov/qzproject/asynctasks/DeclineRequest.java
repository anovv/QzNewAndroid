package ru.anov.qzproject.asynctasks;

import java.util.Map;

import ru.anov.qzproject.utils.APIHandler;



import android.content.Context;
import android.os.AsyncTask;


public class DeclineRequest extends AsyncTask<Void, Void, Void>{
	
	private Map<String, Map<String, String>> res;
	private Context context;
	private boolean notify;
	
	public DeclineRequest(boolean notify){
		this.notify = notify;
	}
	
	public DeclineRequest setContext(Context context){
		this.context = context;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		res = APIHandler.checkRequests(true, notify, false);
		return null;
	}
	
	/*public void onPostExecute(Void unused){
		if(res != null){
			Toast.makeText(context, "Declined", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(context, "Not Declined" + APIHandler.error, Toast.LENGTH_LONG).show();
		}
	}*/
}
