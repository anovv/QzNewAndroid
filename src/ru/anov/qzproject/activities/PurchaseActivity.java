package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.anov.qzproject.R;
import ru.anov.qzproject.adapters.PurchaseAdapter;
import ru.anov.qzproject.billing.IabHelper;
import ru.anov.qzproject.billing.IabHelper.OnConsumeFinishedListener;
import ru.anov.qzproject.billing.IabHelper.OnConsumeMultiFinishedListener;
import ru.anov.qzproject.billing.IabHelper.OnIabPurchaseFinishedListener;
import ru.anov.qzproject.billing.IabHelper.QueryInventoryFinishedListener;
import ru.anov.qzproject.billing.IabResult;
import ru.anov.qzproject.billing.Inventory;
import ru.anov.qzproject.billing.Purchase;
import ru.anov.qzproject.billing.SkuDetails;
import ru.anov.qzproject.fragments.MainFragment;
import ru.anov.qzproject.fragments.ThemelistFragment;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PurchaseActivity extends ActionBarActivity {
	
	private static final int PURCHASE_REQUEST_CODE = 228;
	private static final String PAYLOAD_TOKEN = "token";
	
	private IabHelper mHelper;
	private Map<String, Map<String, String>> skusMap;
	private List<Map<String, String>> skus;
	private List<Purchase> purchases;
	private List<SkuDetails> skuDetails;
	private List<SkuDetails> skusToShow;
	
	//UI
	private View curView;
	private View purchaseView;
	private ListView listView;
	private View errorView;
	private TextView errorTextView;
	private Button errorButton;
	private ProgressBar progressBar;
	
	private Button booster1Button;
	private Button booster2Button;
	private Button booster3Button;
	private TextView boosterTextView;
	
	private View unlockAllView;
	private View unlockAllButtonView;
	private TextView unlockAllTextView;
	
	private TextView titleTextView;
	
	private QueryInventoryFinishedListener mQueryFinishedListener;
	private OnIabPurchaseFinishedListener mOnIabPurchaseFinishedListener;
	private OnConsumeMultiFinishedListener mOnConsumeMultiFinishedListener;
	private OnConsumeFinishedListener mOnConsumeFinishedListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		if(savedInstanceState != null){
			return;
			
		}
		
		purchaseView = findViewById(R.id.activity_purchase_view);
		errorView = findViewById(R.id.activity_purchase_error_view);
		errorTextView = (TextView) findViewById(R.id.activity_purchase_error_textview);
		listView = (ListView) findViewById(R.id.activity_purchase_listview);
		errorButton = (Button) findViewById(R.id.activity_purchase_error_button);
		progressBar = (ProgressBar) findViewById(R.id.activity_purchase_progressbar);
		curView = progressBar;
		
		View header = getLayoutInflater().inflate(R.layout.layout_purchase_header, listView, false);
        listView.addHeaderView(header, "header", false);
        booster1Button = (Button) header.findViewById(R.id.layout_purchase_header_button_1);
        booster2Button = (Button) header.findViewById(R.id.layout_purchase_header_button_2);
        booster3Button = (Button) header.findViewById(R.id.layout_purchase_header_button_3);
        boosterTextView = (TextView) header.findViewById(R.id.layout_purchase_booster_textview);
        
        
		View headerAll = getLayoutInflater().inflate(R.layout.layout_purchase_all_header, listView, false);
        listView.addHeaderView(headerAll, "header", false);

        unlockAllView = headerAll.findViewById(R.id.layout_purchase_all_view);
        titleTextView = (TextView) headerAll.findViewById(R.id.layout_purchase_all_title_textview);
        unlockAllTextView = (TextView) headerAll.findViewById(R.id.layout_purchase_all_textview);
        unlockAllButtonView = headerAll.findViewById(R.id.layout_purchase_all_button_view);
        
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
		booster1Button.setTypeface(tf);
		booster2Button.setTypeface(tf);
		booster3Button.setTypeface(tf);
		boosterTextView.setTypeface(tf);
        
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");
		unlockAllTextView.setTypeface(tf);
		titleTextView.setTypeface(typeface);
		
		View footer = new View(this);
        footer.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 20));
        listView.addFooterView(footer, "footer", false);

		errorButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new GetSkus().setContext(PurchaseActivity.this).execute();
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, String> item = (Map<String, String>) listView.getAdapter().getItem(position);
				mHelper.launchPurchaseFlow(PurchaseActivity.this, item.get("sku"), PURCHASE_REQUEST_CODE, mOnIabPurchaseFinishedListener, PAYLOAD_TOKEN);

			}
		});
		
		mOnConsumeFinishedListener = new OnConsumeFinishedListener(){
			@Override
			public void onConsumeFinished(Purchase purchase, IabResult result) {}
		};
		
		mOnConsumeMultiFinishedListener = new OnConsumeMultiFinishedListener(){
			
			@Override
			public void onConsumeMultiFinished(List<Purchase> purchasesList, List<IabResult> results) {
				skus = new ArrayList<Map<String, String>>();
				
				for(String sku : skusMap.keySet()){
					skus.add(skusMap.get(sku));
				}
				
				for(int i = 0; i < purchases.size(); i++){
					boolean check = false;
					Purchase p = purchases.get(i);
					for(Purchase pur : purchasesList){
						if(p.getSku().equals(pur.getSku())){
							check = true;
							break;
						}
					}
					if(check){
						purchases.remove(i);
					}
				}
				
				for(int i = 0; i < skus.size(); i++){
					boolean check = false;
					Map<String, String> sku = skus.get(i);
					for(Purchase p : purchases){
						if(sku.get("sku").equals(p.getSku())){
							check = true;
							break;
						}
					}
					if(check){
						skus.remove(i);
					}
				}
				
				List<String> tempSkus = new ArrayList<String>();
				for(Map<String, String> sku : skus){
					tempSkus.add(sku.get("sku"));
				}
				
				skusToShow = new ArrayList<SkuDetails>();
				
				for(SkuDetails s : skuDetails){
					if(tempSkus.contains(s.getSku())){
						skusToShow.add(s);
					}
				}
				
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				List<Map<String, String>> boosterList = new ArrayList<Map<String, String>>();
				Map<String, String> unlockerSku = null;
				for(SkuDetails s : skusToShow){
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", s.getTitle());
					map.put("description", s.getDescription());
					map.put("price", s.getPrice());
					map.put("type", s.getType());
					map.put("sku", s.getSku());
					
					if(skusMap.get(s.getSku()).containsKey("parent")){//theme
						map.put("name", skusMap.get(s.getSku()).get("name"));
						map.put("description", skusMap.get(s.getSku()).get("description"));
						list.add(map);
					}else if(skusMap.get(s.getSku()).containsKey("value")){//booster
						map.put("value", skusMap.get(s.getSku()).get("value"));
						boosterList.add(map);
					}else if(skusMap.get(s.getSku()).containsKey("perm")){//unlocker
						unlockerSku = map;
					}
				}

				if(unlockerSku == null){// already unlocked
					listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, new ArrayList<Map<String, String>>()));
					unlockAllView.setVisibility(View.GONE);
				}else{
					Collections.sort(list, new PurchaseComparator());
					listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, list));
					initUnlocker(unlockerSku);
				}
				Utils.crossfade(PurchaseActivity.this, purchaseView, curView);
				initBoosters(boosterList);
				curView = purchaseView;
			}
		};
		
		mOnIabPurchaseFinishedListener = new OnIabPurchaseFinishedListener(){

			@Override
			public void onIabPurchaseFinished(IabResult result, Purchase info) {
				if(result.isFailure()){
					return;
				}
				Map<String, String> sku = skusMap.get(info.getSku());
				if(sku.containsKey("parent")){
					// theme
					skus.remove(sku);
					for(int i = 0; i < skusToShow.size(); i++){
						SkuDetails s = skusToShow.get(i);
						if(s.getSku().equals(sku.get("sku"))){
							skusToShow.remove(i);
						}
					}
					
					List<Map<String, String>> list = new ArrayList<Map<String, String>>();
					List<Map<String, String>> boosterList = new ArrayList<Map<String, String>>();
					Map<String, String> unlockerSku = null;
					for(SkuDetails s : skusToShow){
						Map<String, String> map = new HashMap<String, String>();
						map.put("title", s.getTitle());
						map.put("description", s.getDescription());
						map.put("price", s.getPrice());
						map.put("type", s.getType());
						map.put("sku", s.getSku());
						if(skusMap.get(s.getSku()).containsKey("parent")){//theme
							map.put("name", skusMap.get(s.getSku()).get("name"));
							map.put("description", skusMap.get(s.getSku()).get("description"));
							list.add(map);
						}else if(skusMap.get(s.getSku()).containsKey("value")){//booster
							map.put("value", skusMap.get(s.getSku()).get("value"));
							boosterList.add(map);
						}else if(skusMap.get(s.getSku()).containsKey("perm")){//unlocker
							unlockerSku = map;
						}
					}
					
					if(unlockerSku == null){// already unlocked
						listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, new ArrayList<Map<String, String>>()));
						unlockAllView.setVisibility(View.GONE);
					}else{
						Collections.sort(list, new PurchaseComparator());
						listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, list));
						initUnlocker(unlockerSku);
					}
					
					initBoosters(boosterList);
					
					ThemeDao.getInstance(PurchaseActivity.this).unlockTheme(sku.get("id"));
					ThemelistFragment.unlockedId = sku.get("id");
					MainFragment.unlockedId = sku.get("id");
					return;
				}
				
				if(sku.containsKey("value")){
					// booster
					mHelper.consumeAsync(info, mOnConsumeFinishedListener);
					Utils.setBooster(PurchaseActivity.this, Integer.parseInt(sku.get("value")));
					return;
				}
				
				if(sku.containsKey("perm")){
					//unlocker
					ThemeDao.getInstance(PurchaseActivity.this).unlockAll();
					listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, new ArrayList<Map<String, String>>()));
					//hide button
					unlockAllView.setVisibility(View.GONE);
					ThemelistFragment.unlockAll = true;
					MainFragment.unlockAll = true;
				}
			}
		};
		
		mQueryFinishedListener = new QueryInventoryFinishedListener() {
		
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
				if (result.isFailure()) { 
					errorTextView.setText(getResources().getString(R.string.no_connection));	
					Utils.crossfade(PurchaseActivity.this, errorView, curView);
					curView = errorView;
					return;  
				}else{
					purchases = inventory.getAllPurchases();
					skuDetails = inventory.getAllSkuDetails();
					
					if(purchases == null){
						errorTextView.setText(getResources().getString(R.string.no_connection));	
						Utils.crossfade(PurchaseActivity.this, errorView, curView);
						curView = errorView;
						return;
					}
					List<Purchase> purchasesToConsume = new ArrayList<Purchase>();
					for(Purchase p : purchases){
						if(skusMap != null && skusMap.get(p.getSku()) != null && !skusMap.get(p.getSku()).containsKey("parent") && !skusMap.get(p.getSku()).containsKey("perm")){
							purchasesToConsume.add(p);
						}
					}
					if(!purchasesToConsume.isEmpty()){
						//not consuming themes
						mHelper.consumeAsync(purchasesToConsume, mOnConsumeMultiFinishedListener);
					}else{
						skus = new ArrayList<Map<String, String>>();
						
						for(String sku : skusMap.keySet()){
							skus.add(skusMap.get(sku));
						}
						
						for(int i = 0; i < skus.size(); i++){
							boolean check = false;
							Map<String, String> sku = skus.get(i);
							for(Purchase p : purchases){
								if(sku.get("sku").equals(p.getSku())){
									check = true;
									break;
								}
							}
							if(check){
								skus.remove(i);
							}
						}
						
						
						List<String> tempSkus = new ArrayList<String>();
						for(Map<String, String> sku : skus){
							tempSkus.add(sku.get("sku"));
						}
						
						skusToShow = new ArrayList<SkuDetails>();
						
						for(SkuDetails s : skuDetails){
							if(tempSkus.contains(s.getSku())){
								skusToShow.add(s);
							}
						}
						
						List<Map<String, String>> list = new ArrayList<Map<String, String>>();
						List<Map<String, String>> boosterList = new ArrayList<Map<String, String>>();
						Map<String, String> unlockerSku = null;
						for(SkuDetails s : skusToShow){
							Map<String, String> map = new HashMap<String, String>();
							map.put("title", s.getTitle());
							map.put("description", s.getDescription());
							
							map.put("price", s.getPrice());
							map.put("type", s.getType());
							map.put("sku", s.getSku());
							
							if(skusMap.get(s.getSku()).containsKey("parent")){//theme
								map.put("name", skusMap.get(s.getSku()).get("name"));
								map.put("description", skusMap.get(s.getSku()).get("description"));
								list.add(map);
							}else if(skusMap.get(s.getSku()).containsKey("value")){//booster
								map.put("value", skusMap.get(s.getSku()).get("value"));
								boosterList.add(map);
							}else if(skusMap.get(s.getSku()).containsKey("perm")){//unlocker
								unlockerSku = map;
							}
						}
						
						if(unlockerSku == null){// already unlocked
							listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, new ArrayList<Map<String, String>>()));
							unlockAllView.setVisibility(View.GONE);
						}else{
							Collections.sort(list, new PurchaseComparator());
							listView.setAdapter(new PurchaseAdapter(PurchaseActivity.this, R.layout.item_purchase, list));
							initUnlocker(unlockerSku);
						}
						Utils.crossfade(PurchaseActivity.this, purchaseView, curView);
						initBoosters(boosterList);
						curView = purchaseView;
					}
				}
			}
		};

		mHelper = new IabHelper(this, APIHandler.RSA_KEY);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			   
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					new GetSkus().setContext(PurchaseActivity.this).execute();
				}else{
					errorTextView.setText(getResources().getString(R.string.no_connection));	
					Utils.crossfade(PurchaseActivity.this, errorView, curView);
					curView = errorView;
				}            
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {  
			super.onActivityResult(requestCode, resultCode, data);
		}
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
	
	@Override
	public void onDestroy() {
	   super.onDestroy();
	   if(mHelper != null){
		   mHelper.dispose();
	   }
	   mHelper = null;
	}
	
	private class GetSkus extends AsyncTask<Void, Void, Void>{

		private Context context;
		
		public GetSkus setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override 
		protected void onPreExecute(){
			super.onPreExecute();
			Utils.crossfade(context, progressBar, curView);
			curView = progressBar;
		} 
		
		@Override
		protected Void doInBackground(Void... params) {
			skusMap = APIHandler.getSkus();
			return null;
		}
		
		protected void onPostExecute(Void unused){
			if(skusMap != null){
				List<String> skuList = new ArrayList<String>(skusMap.keySet());
				if(mHelper != null){
					mHelper.queryInventoryAsync(true, skuList, mQueryFinishedListener);
				}
			}else{
				errorTextView.setText(getResources().getString(R.string.no_connection));	
				Utils.crossfade(context, errorView, curView);
				curView = errorView;
			}
		}
	}
	
	private void initBoosters(List<Map<String, String>> boosterList){
		if(boosterList == null){
			return;
		}
		Collections.sort(boosterList, new Comparator<Map<String, String>>(){

			@Override
			public int compare(Map<String, String> lhs, Map<String, String> rhs) {
				try{
					int l = Integer.parseInt(lhs.get("value"));
					int r = Integer.parseInt(rhs.get("value"));
					return l - r;
				}catch(Exception e){
					return 0;
				}
			}
			
		});
		
		if(boosterList.size() > 0){
			Map<String, String> m = boosterList.get(0);
			final String sku = m.get("sku");
			String value = m.get("value");
			if(sku != null && value != null){
				booster1Button.setText("x" + value);
				booster1Button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						mHelper.launchPurchaseFlow(PurchaseActivity.this, sku, PURCHASE_REQUEST_CODE, mOnIabPurchaseFinishedListener, PAYLOAD_TOKEN);
					}
				});
			}
		}
		
		if(boosterList.size() > 1){
			Map<String, String> m = boosterList.get(1);
			final String sku = m.get("sku");
			String value = m.get("value");
			if(sku != null && value != null){
				booster2Button.setText("x" + value);
				booster2Button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						mHelper.launchPurchaseFlow(PurchaseActivity.this, sku, PURCHASE_REQUEST_CODE, mOnIabPurchaseFinishedListener, PAYLOAD_TOKEN);
					}
				});
			}
		}
		
		if(boosterList.size() > 2){
			Map<String, String> m = boosterList.get(2);
			final String sku = m.get("sku");
			String value = m.get("value");
			if(sku != null && value != null){
				booster3Button.setText("x" + value);
				booster3Button.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						mHelper.launchPurchaseFlow(PurchaseActivity.this, sku, PURCHASE_REQUEST_CODE, mOnIabPurchaseFinishedListener, PAYLOAD_TOKEN);
					}
				});
			}
		}
		
		int delay = 100;
		booster1Button.postDelayed(new Runnable(){
 
			@Override
			public void run() {
				Utils.popAnim(booster1Button, PurchaseActivity.this);
			}
			
		}, delay);
		
		delay += 100;
		
		booster2Button.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.popAnim(booster2Button, PurchaseActivity.this);
			}
			
		}, delay);
		
		delay += 100;
		
		booster3Button.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.popAnim(booster3Button, PurchaseActivity.this);
			}
			
		}, delay);
		
	}
	
	private void initUnlocker(Map<String, String> skuMap){
		if(skuMap == null){
			return;
		}

		final String sku = skuMap.get("sku");
		unlockAllButtonView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mHelper.launchPurchaseFlow(PurchaseActivity.this, sku, PURCHASE_REQUEST_CODE, mOnIabPurchaseFinishedListener, PAYLOAD_TOKEN);
			}
		});
	}
	
	private class PurchaseComparator implements Comparator<Map<String, String>>{

		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			
			String s1 = lhs.get("name");
			String s2 = rhs.get("name");
			
			if(s1 == null || s2 == null || s1.equals(s2)){
				return 0;
			}
			
			return s1.compareTo(s2); 
		}
	}
}
