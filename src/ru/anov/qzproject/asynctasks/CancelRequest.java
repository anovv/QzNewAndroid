package ru.anov.qzproject.asynctasks;

import ru.anov.qzproject.utils.APIHandler;

import android.content.Context;
import android.os.AsyncTask;


public class CancelRequest extends AsyncTask<Void, Void, Void>{
	
	private String rid;
	private boolean res;
	private Context context;
	
	public CancelRequest(String rid){
		this.rid = rid;
	}
	
	public CancelRequest setContext(Context context){
		this.context = context;
		return this;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		res = APIHandler.cancelRequest(rid);
		return null;
	}
	
	/*public void onPostExecute(Void unused){
		if(res){
			Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(context, "Not Cancelled " + APIHandler.error, Toast.LENGTH_LONG).show();
		}
	}*/
}
