package ru.anov.qzproject.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;

public class APIHandler {
	
	public static String api_url = "http://ec2-54-171-253-235.eu-west-1.compute.amazonaws.com/" +
		"index.php/api";

	public static final String RSA_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg" +
			"KCAQEArDlIo9oa/kBpyeaa/QzYxNTk/kcGKg+cVLbaP9Zanhljy2KlQ8S32MLBTfy3V" +
			"dqoSAXeGKjipHkKwMR1sJMRCmfZWy0Tl5vGD1mzzTfOTseiT8YhfpT2tBWx75WbIMr/" +
			"0/cuQTv8tc+uWwOzlAW8b9oPtXzLSmov8bwR75IDNgUhWN8H3BxaOpfM/Rzk2VMwKDLF" +
			"PLWeRMOuR0rWlJGRkS4oykLvfopQK76FWcR3G4kP/Gatk3fsxHQWlNBHaFjRg+RoU4++" +
			"jsy53fuZbFy7fSqxucGBwcTrgpotIqtcn19hZ9FOMvMRZtaM+g+IVCP6GdwrA11MlKF/" +
			"2eGe9+VHxwIDAQAB";

	/*public static String api_url = "http://ec2-54-68-144-47.us-west-2.compute.amazonaws.com/" +
			"index.php/api";

	public static final String RSA_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC" +
			"AQEAvpGt7E4PF3pUCcVAF160z4Gi0Cvt656Y093S6gLcAlTz0s1xDyT2Feu3TeKHqLAHK" +
			"dMU/gJbGT03kAtzyY+4IvmK3R1MNqE/oA047zWpmRl9+RC6+xB5jsCVdyJ0ztfW/fUl/L" +
			"TRsr+H+gRsmMItpBmWILw2w1akeZdbAjlhGRpqR5D3vlQTf98jGSpPjBL0YtZMU3NjgDZ" +
			"Y5asDAOxn0rzXDBk4OdnRO/A83jvqZ2NNQXUYuqeFU9hLaK7oqV/wbdbYskMYSvLn/QG4" +
			"G72Z9/SL6NPkHdtYi8AjFzOPWNJRHTsmZgHztfi4NvZIbE9mk5n+q149q49fwFG6BwxzhwIDAQAB";*/

	public static final String SENDER_ID = "97918" +
		"1055616"; 
	
	
	public static String dynamic_server_ip;
	public static int dynamic_server_port;
	
	public static String error;
	public static String user_id;
	public static String signature;
	public static String name;
	
	public static int SELECTION_STATE = 0;
	
	public static AsyncTask curTask;
	
	public static Map<String, String> sendMessage(String rid, String message){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("rid", rid);
		args.put("message", message);
		
		JSONObject res = getResponseForPost("user/sendmessage", args, headers);	
		
		if(res != null){
			try{
				Map<String, String> map = new HashMap<String, String>();
				Iterator iter = res.keys();
				while(iter.hasNext()){
					String key = (String) iter.next();
					String val = res.getString(key);
					map.put(key, val);
				}
				return map;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static boolean submitQuestion(String themeId, 
			String themeName,
			String question, 
			String ans1, 
			String ans2, 
			String ans3, 
			String ans4,
			String right_ans,
			String encoded){
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("theme_id", themeId);
		args.put("theme_name", themeName);
		args.put("question", question);
		args.put("ans1", ans1);
		args.put("ans2", ans2);
		args.put("ans3", ans3);
		args.put("ans4", ans4);
		args.put("right_ans", right_ans);
		args.put("img", encoded);
		
		JSONObject res = getResponseForPost("question/submitquestion", args, headers);	
		
		if(res != null){
			return true;
		}else{
			return false;
		}
	}
	
	public static Map<String, String> postComment(String themeId, String comment){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("theme_id", themeId);
		args.put("comment", comment);
		
		JSONObject res = getResponseForPost("question/postcomment", args, headers);	
		
		if(res != null){
			try{
				Map<String, String> map = new HashMap<String, String>();
				Iterator iter = res.keys();
				while(iter.hasNext()){
					String key = (String) iter.next();
					String val = res.getString(key);
					map.put(key, val);
				}
				return map;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static List<Map<String, String>> getComments(String theme_id, int index){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("question/getcomments?index=" + index + "&theme_id=" + theme_id, headers);
		
		if(res != null){
			try{
				List<Map<String, String>> result = new ArrayList<Map<String, String>>();
				String is_last = res.getString("is_last");
				Map<String, String> isLast = new HashMap<String, String>();
				isLast.put("is_last", is_last);
				result.add(isLast);
				JSONArray comments = res.getJSONArray("comments");
				for(int i = 0; i < comments.length(); i++){
					JSONObject obj = comments.getJSONObject(i);
					Map<String, String> comment = new HashMap<String, String>();
					Iterator iter = obj.keys();
					while(iter.hasNext()){
						String key = (String) iter.next();
						String val = obj.getString(key);
						comment.put(key, val);
					}
					result.add(comment);
				}
				return result;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static Map<String, Map<String, String>> getSkus(){
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("question/getskus", headers);
		
		if(res != null){
			try{
				JSONObject skus = res.getJSONObject("skus");
				Map<String, Map<String, String>> m = new HashMap<String, Map<String, String>>();
				Iterator it = skus.keys();
				while(it.hasNext()){
					String k = (String) it.next();
					JSONObject v = skus.getJSONObject(k);
					Iterator iter = v.keys();
					Map<String, String> map = new HashMap<String, String>();
					while(iter.hasNext()){
						String key = (String) iter.next();
						String val = v.getString(key);
						map.put(key, val);
					}
					map.put("sku", k);
					m.put(k, map);
				}
				return m;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static Map<String, String> getRandomRecord(String theme_id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("question/getrandomrecord/" + theme_id, headers);
		
		if(res == null){
			return null;
		}
		
		try{
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			APIHandler.error = e.toString();
			return null;
		}
	}
	
	public static Map<String, String> getRUser(String rid, String theme_id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("user/getruser?rid=" + rid + "&theme_id=" + theme_id, headers);
		
		if(res == null){
			return null;
		}
		
		try{
			JSONObject ruser = res.getJSONObject("ruser");
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = ruser.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = ruser.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			error = e.toString();
			return null;
		}
		
	}
	
	public static boolean cancelRequest(String rid){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("user/cancelrequest/" + rid, headers);
		
		return res != null;
	}
	
	public static Map<String, Map<String, String>> checkRequests(boolean isDeclined, boolean notify, boolean isOfflineMode){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("user/checkrequests?is_declined=" + (isDeclined ? 1 : 0) + "&notify=" + (notify ? 1 : 0) + "&is_offline_mode=" + (isOfflineMode ? 1 : 0), headers);
		
		if(res != null){
			try{
				JSONArray reqs = res.getJSONArray("requests");
				Map<String, String> online = new HashMap<String, String>();
				Map<String, String> offline = new HashMap<String, String>();
				Map<String, Map<String, String>> m = new HashMap<String, Map<String, String>>();
				
				String upThemeId = "";
				String upThemeName = "";
				String upThemeCounter = "";
				String upThemeMessage = "";
				if(res.has("up_theme_id") && res.has("up_theme_name") && res.has("up_theme_counter") && res.has("up_theme_message")){
					upThemeId = res.getString("up_theme_id");
					upThemeName = res.getString("up_theme_name");
					upThemeCounter = res.getString("up_theme_counter");
					upThemeMessage = res.getString("up_theme_message");
				}
				
				Map<String, String> m1 = new HashMap<String, String>();
				Map<String, String> m2 = new HashMap<String, String>();
				Map<String, String> m3 = new HashMap<String, String>();
				Map<String, String> m4 = new HashMap<String, String>();

				m1.put("up_theme_id", upThemeId);
				m2.put("up_theme_name", upThemeName);
				m3.put("up_theme_counter", upThemeCounter);
				m4.put("up_theme_message", upThemeMessage);
				
				m.put("up_theme_id", m1);
				m.put("up_theme_name", m2);
				m.put("up_theme_counter", m3);
				m.put("up_theme_message", m4);
				
				if(reqs.length() == 0){
					m.put("offline", offline);
					m.put("online", online);
					return m;
				}
				
				for(int i = 0; i < reqs.length(); i++){
					JSONObject req = reqs.getJSONObject(i);
					String type = req.getString("type");
					if(type != null){
						if(type.equals("offline")){
							Iterator it = req.keys();
							while(it.hasNext()){
								String k = (String) it.next();
								String v = req.getString(k);
								offline.put(k, v);
							}
						}else{
							Iterator it = req.keys();
							while(it.hasNext()){
								String k = (String) it.next();
								String v = req.getString(k);
								online.put(k, v);
							}
						}
					}
				}

				m.put("offline", offline);
				m.put("online", online);

				return m;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static boolean unblock(String id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("user/unblock/" + id, headers);		
		
		return !(res == null);
	}
	
	public static boolean blockUser(String rid){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);

		JSONObject res = getResponseForGet("user/blockuser/" + rid, headers);	
		return !(res == null);
	}
	
	public static boolean addFriend(String rid){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);

		JSONObject res = getResponseForGet("user/addfriend/" + rid,  headers);	
		return !(res == null);
	}
	
	public static List<Map<String, String>> fetchServerInfo(List<String> skusList, String id, String gcm_id){

		Map<String, String> args = new HashMap<String, String>();
		String skus = "";
		
		for(String s : skusList){
			skus += s + ";";
		}
		
		if(skus.length() > 0){
			skus = skus.substring(0, skus.length() - 1);
		}
		
		args.put("skus", skus);
		if(id != null){
			args.put("id", id);
		}
		if(gcm_id != null){
			args.put("gcm_id", gcm_id);
		}
		
		JSONObject res = getResponseForPost("question/fetchserverinfo", args, null);
		
		if(res != null){
			List<Map<String, String>> result = new ArrayList<Map<String, String>>();
			
			try{
				JSONObject info = res.getJSONObject("info");
				Map<String, String> m = new HashMap<String, String>();
				Iterator it = info.keys();
				while(it.hasNext()){
					String k = (String) it.next();
					String v = info.getString(k);
					m.put(k, v);
				}
				result.add(m);
				
				JSONArray ts = res.getJSONArray("themes");
				
				for(int i = 0; i < ts.length(); i++){
					JSONObject t = (JSONObject) ts.get(i);
					Iterator iter = t.keys();
					Map<String, String> theme = new HashMap<String, String>();					
					while(iter.hasNext()){
						String k = (String) iter.next();
						theme.put(k, t.getString(k));
					}
					result.add(theme);
				}

				JSONArray ns = res.getJSONArray("new");
				
				String new_ids = "";
				
				for(int i = 0; i < ns.length(); i++){
					String new_id = ns.getString(i);
					new_ids += new_id + "_";
				}
				
				if(new_ids.length() > 0){
					new_ids = new_ids.substring(0, new_ids.length() - 1);
				}
				
				Map<String, String> nids = new HashMap<String, String>();
				nids.put("new", new_ids);
				
				String friendsCount = res.getString("friends_count"); 
				Map<String, String> f = new HashMap<String, String>();
				f.put("friends_count", friendsCount);
				result.add(nids);
				result.add(f);
				
				return result;
			}catch(Exception e){
				APIHandler.error = e.toString();
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static boolean setSettings(String name, boolean isAvailable){
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);

		Map<String, String> args = new HashMap<String, String>();
		args.put("name", name);
		args.put("is_available", (isAvailable ? "1" : "0"));
		
		JSONObject res = getResponseForPost("user/setsettings", args, headers);	
		
		return !(res == null);
	}
	
	public static Map<String, String> registerVk(String vk_id, String access_token, String gcm_id){
		Map<String, String> args = new HashMap<String, String>();
		args.put("vk_id", vk_id);
		args.put("access_token", access_token);
		args.put("gcm_id", gcm_id);

		JSONObject res = getResponseForPost("user/registervk", args, null);
		
		if(res == null){
			return null;
		}
		
		try{
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			error = e.toString();
			return null;
		}
	}
	
	public static Map<String, String> finalize(
			String rid, 
			String theme_id, 
			String theme_name, 
			String score, 
			boolean hasWon,
			boolean isOfflineMode,
			String rScore,
			boolean saveDuels,
			String ansSeq){
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);

		Map<String, String> args = new HashMap<String, String>();
		args.put("rid", rid);
		args.put("theme_id", theme_id);
		args.put("theme_name", theme_name);
		args.put("score", score);
		args.put("has_won", (hasWon) ? "1" : "0");
		args.put("is_offline_mode", (isOfflineMode) ? "1" : "0");
		if(isOfflineMode){
			args.put("rscore", rScore);
		}
		args.put("save_duels", (saveDuels) ? "1" : "0");
		args.put("ans_seq", ansSeq);
		
		JSONObject res = getResponseForPost("question/finalize", args, headers);	
		
		if(res == null){
			return null;
		}
		
		try{
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			error = e.toString();
			return null;
		}
	}
	
	public static List<Map<String, String>> getScore(String theme_id, boolean isTopScore){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = null;
		
		if(isTopScore){
			res = getResponseForGet("question/gettopscore/" + theme_id, headers);
		}else{
			res = getResponseForGet("question/getfriendsscore/" + theme_id, headers);
		}
		if(res != null){
			List<Map<String, String>> ar = new ArrayList<Map<String, String>>();
			try {
				JSONArray score = res.getJSONArray("score");
				for(int i = 0; i < score.length(); i++){
					Map<String, String> m = new HashMap<String, String>();
					JSONObject j = score.getJSONObject(i);
					Iterator it = j.keys();
					while(it.hasNext()){
						String k = (String) it.next();
						String v = j.getString(k);
						m.put(k, v);
					}
					ar.add(m);
				}
				
				return ar;
			} catch (Exception e) {
				APIHandler.error = e.toString();
				return null;
			}
			
		}else{
			return null;
		}
	}

	public static boolean notify(String rid, String theme_id, String theme_name){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("theme_id", theme_id);
		args.put("theme_name", theme_name);
		args.put("rid", rid);

		JSONObject res = getResponseForPost("user/notify", args, headers);	
		
		return res != null;
	}
	
	public static boolean notifyOffline(String rid, String theme_id, String theme_name, String ans_seq){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("theme_id", theme_id);
		args.put("theme_name", theme_name);
		args.put("rid", rid);
		args.put("ans_seq", ans_seq);

		JSONObject res = getResponseForPost("user/notifyoffline", args, headers);	
		
		return res != null;
	}
	
	public static Map<String, Map<String, String>> getQuestionsByIds(String theme_id, List<String> ids){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		String qids_string = "";
		
		for(String id : ids){
			qids_string += (id + "_");
		}
		
		if(qids_string.length() > 0){
			qids_string = qids_string.substring(0, qids_string.length() - 1);
		}
		
		
		JSONObject res = getResponseForGet("question/getquestionsbyids?theme_id=" + theme_id + "&qids=" + qids_string, headers);	
		Map<String, Map<String, String>> qs = new HashMap<String, Map<String, String>>();
		if(res != null){
			try {
				JSONArray questions = res.getJSONArray("questions");
				for(int i = 0; i < questions.length(); i++){
					JSONObject question = questions.getJSONObject(i);
					String id = question.getString("id");
					Map<String, String> q = new HashMap<String, String>();
					Iterator it = question.keys();
					while(it.hasNext()){
						String k = (String) it.next();
						q.put(k, question.getString(k));
					}
					qs.put(id, q);
				}
				
				return qs;
				
			}catch(Exception e){
				error = e.toString();
				return null;
			}
		}
		
		return null;
	}
	
	public static Map<String, Map<String, String>> getRandomQuestions(String theme_id){

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("question/getrandomquestions/" + theme_id, headers);	
		Map<String, Map<String, String>> qs = new HashMap<String, Map<String, String>>();
		if(res != null){
			try {
				JSONArray questions = res.getJSONArray("questions");
				for(int i = 0; i < questions.length(); i++){
					JSONObject question = questions.getJSONObject(i);
					String id = question.getString("id");
					Map<String, String> q = new HashMap<String, String>();
					Iterator it = question.keys();
					while(it.hasNext()){
						String k = (String) it.next();
						q.put(k, question.getString(k));
					}
					qs.put(id, q);
				}
				
				return qs;
				
			}catch(Exception e){
				error = e.toString();
				return null;
			}
		}
		
		return null;
	}
	
	public static Map<String, String> register(String email, String password, String name, String regId){
		Map<String, String> args = new HashMap<String, String>();
		args.put("email", email);
		args.put("password", password);
		args.put("fullname", name);
		args.put("gcm_id", regId);
		JSONObject res = getResponseForPost("user/register", args, null);
		if(res == null){
			return null;
		}
		try{
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			return null;
		}
	}
	
	public static Map<String, String> login(String email, String password, String gcm_id){
		Map<String, String> args = new HashMap<String, String>();
		args.put("email", email);
		args.put("password", password);
		args.put("gcm_id", gcm_id);
		
		JSONObject res = getResponseForPost("user/login", args, null);
		
		if(res == null){
			return null;
		}	
		
		try {
			Map<String, String> result = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				result.put(k, v);
			}
			return result;
		} catch (Exception e) {
			error = e.toString();
			return null;
		}
	} 
	
	public static boolean setAvailability(boolean isAvailable){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("user/setavailability/" + ((isAvailable) ? "1" : "0"), headers);
		return !(res == null);
	}
	
	public static Map<String, Map<String, String>> getSinglePlayer(String theme_id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("question/single/" + theme_id, headers);
		
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		
		if(res != null){
			try {
				JSONArray array = res.getJSONArray("questions");
				for(int i = 0; i < array.length(); i++){
					JSONObject obj = array.getJSONObject(i);
					Map<String, String> temp = new HashMap<String, String>();
					Iterator it = obj.keys();
					String id = null;
					while(it.hasNext()){
						String k = (String) it.next();
						if(k.equals("id")){
							id = obj.getString(k);
						}
						String v = obj.getString(k);
						temp.put(k, v);
					}
					if(id != null){
						result.put(id, temp);
					}
				}
			} catch (Exception e) {
				error = e.toString();
				return null;
			}
			return result;
		}else{
			return null;
		}
	}
	
	public static JSONObject getResponseForGet(String method, Map<String, String> headers){
		
		HttpClient httpclient = new DefaultHttpClient();
		String getData = api_url + "/" + method;
				
		HttpGet httpget = new HttpGet(getData); 			

		if(headers != null){
			Iterator iter = headers.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				httpget.addHeader(entry.getKey(), entry.getValue());
			}
		}
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
			
		} catch (Exception e) {	
			error = "No connection 2 " + e.toString();
			return null;			
		}		
		
		String requestResult = null;
		try {
			requestResult = EntityUtils.toString(response.getEntity());

		} catch (Exception e) {	
			error = "3" + e.toString();
			return null;
		}	
		
		if(requestResult != null) {      
			JSONObject jsonResult = null;			
			try {
				jsonResult = new JSONObject(requestResult);
				if(!jsonResult.getBoolean("success")){
										
					error = jsonResult.getJSONObject("message").getString("text");
					return null;
					
				}else{
					error = null;				
					return new JSONObject(jsonResult.getString("message"));					
				}
			} catch (Exception e) {				
				error = "4" + e.toString();
				return null;		
			}
		}			
		return null;	
	}
	
	public static JSONObject getResponseForPost(String method, Map<String, String> arguments, Map<String, String> headers){
		
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		
		if(arguments == null){    
			arguments = new HashMap<String, String>();	        
		}
		
		Iterator i = arguments.entrySet().iterator();
		
		while(i.hasNext()){
			Map.Entry<String, String> entry = (Map.Entry<String, String>)i.next();
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
				
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(api_url + "/" + method); 
		
		if(headers != null){
			Iterator iter = headers.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
				httppost.addHeader(entry.getKey(), entry.getValue());
			}
		}
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
		} catch (Exception e) {	
			error = "No connection 1 " + e.toString();
			return null;
		}
				
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
			
		} catch (Exception e) {	
			error = "No connection 2 " + e.toString();
			return null;			
		}		
		
		String requestResult = null;
		try {
			requestResult = EntityUtils.toString(response.getEntity());

		} catch (Exception e) {	
			error = "3" + e.toString();
			return null;
		}	
		
		if(requestResult != null) {      
			JSONObject jsonResult = null;			
			try {
				jsonResult = new JSONObject(requestResult);
				if(!jsonResult.getBoolean("success")){
										
					error = jsonResult.getJSONObject("message").getString("text");
					return null;
					
				}else{
					error = null;				
					return new JSONObject(jsonResult.getString("message"));					
				}
			} catch (Exception e) {				
				error = "4" + e.toString();
				return null;		
			}
		}			
		return null;
	}
	
	public static List<Map<String, String>> getFriends(String id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("user/getfriends/" + id, headers);
		if(res == null){
			return null;
		}
		ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
		
		try{
			JSONArray ar = res.getJSONArray("friends");
			for(int i = 0; i < ar.length(); i++){
				JSONObject user = ar.getJSONObject(i);
				Map<String, String> u = new HashMap<String, String>();
				Iterator it = user.keys();
				while(it.hasNext()){
					String k = (String) it.next();
					String v = user.getString(k);
					u.put(k, v);
				}
				result.add(u);
			}			
			return result;
			
		}catch(Exception e){
			error = e.toString();
			return null;
		}	
	}
	
	public static Map<String, String> setVKFriends(String vk_id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		JSONObject res = getResponseForGet("user/setvkfriends", headers);
		
		try{
			Map<String, String> m = new HashMap<String, String>();
			Iterator i = res.keys();
			while(i.hasNext()){
				String k = (String) i.next();
				String v = res.getString(k);
				m.put(k, v);
			}
			return m;
		}catch(Exception e){
			APIHandler.error = e.toString();
			return null;
		}
	}
		
	public static ArrayList<Map<String, String>> getUserById(String id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("user/getuserbyid/" + id, headers);		
		
		if(res == null){
			return null;
		}
		
		try{

			ArrayList<Map<String, String>> arr = new ArrayList<Map<String, String>>();
			
			Map<String, String> map = new HashMap<String, String>();
			JSONObject user = res.getJSONObject("user");
			if(user == null){
				return null;
			}
			Iterator it = user.keys();
			while(it.hasNext()){
				String k = (String) it.next();
				String v = user.getString(k);
				map.put(k, v);
			}
			arr.add(map);
			JSONArray scores = res.getJSONArray("score");
			for(int i = 0; i < scores.length(); i++){
				JSONObject score = scores.getJSONObject(i);
				Map<String, String> m = new HashMap<String, String>();
				Iterator iter = score.keys();
				while(iter.hasNext()){
					String k = (String) iter.next();
					String v = score.getString(k);
					m.put(k, v);
				}
				arr.add(m);
			}
			String userDuelScore = res.getString("user_duel_score");
			String opDuelScore = res.getString("op_duel_score");
			
			Map<String, String> duel = new HashMap<String, String>();
			
			duel.put("user_duel_score", userDuelScore);
			duel.put("op_duel_score", opDuelScore);
			
			arr.add(duel);
			
			return arr;
		}catch(Exception e){
			error = e.toString();
			return null;
		}
	}
	
	public static List<Map<String, String>> getScoreById(String id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("question/getscorebyid/" + id, headers);		
		
		
		try{
			JSONArray scores = res.getJSONArray("score");
			
			ArrayList<Map<String, String>> arr = new ArrayList<Map<String, String>>();
			
			for(int i = 0; i < scores.length(); i++){
				JSONObject score = scores.getJSONObject(i);
				Map<String, String> m = new HashMap<String, String>();
				Iterator iter = score.keys();
				while(iter.hasNext()){
					String k = (String) iter.next();
					String v = score.getString(k);
					m.put(k, v);
				}
				arr.add(m);
			}
			String userDuelScore = res.getString("user_duel_score");
			String opDuelScore = res.getString("op_duel_score");
			
			Map<String, String> duel = new HashMap<String, String>();
			
			duel.put("user_duel_score", userDuelScore);
			duel.put("op_duel_score", opDuelScore);
			
			arr.add(duel);
			
			return arr;
			
		}catch(Exception e){
			APIHandler.error = e.toString();
			return null;
		}
	}
	
	public static List<Map<String, String>> findUser(String name){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		Map<String, String> args = new HashMap<String, String>();
		args.put("name", name);
		
		JSONObject res = getResponseForPost("user/finduser", args, headers);
		
		if(res == null){
			return null;
		}
		
		try{
			ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();			
			
			JSONArray ar = res.getJSONArray("users");
			
			for(int i = 0; i < ar.length(); i++){
				JSONObject obj = ar.getJSONObject(i);
				Map<String, String> u = new HashMap<String, String>();
				Iterator it = obj.keys();
				while(it.hasNext()){
					String k = (String) it.next();
					String v = obj.getString(k);
					u.put(k, v);
				}
				result.add(u);
			}
			
			return result;
			
		}catch(Exception e){
			error = e.toString();
			return null;			
		}
		
	}
	
	public static boolean unfriend(String id){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		JSONObject res = getResponseForGet("user/unfriend/" + id, headers);		
		
		return !(res == null);
	}
	
	public static Map<String, String> uploadImage(String base64EncodedBitmap){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("HTTP_USERID", user_id);
		headers.put("HTTP_SIGNATURE", signature);
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("image", base64EncodedBitmap);
		
		JSONObject res = getResponseForPost("user/uploadimage", args, headers);
		Map<String, String> result = new HashMap<String, String>();
		
		try{
			Iterator iter = res.keys();
			while(iter.hasNext()){
				String k = (String) iter.next();
				String v = res.getString(k);
				result.put(k, v);
			}
			return result;
		}catch(Exception e){
			APIHandler.error = e.toString();
			return null;
		}
	}
	
	public static String getHash(String input, String key){

		Mac mac;
        String _sign = "";
        try {
            byte[] bytesKey = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(bytesKey, "HmacSHA512" );
            mac = Mac.getInstance( "HmacSHA512" );
            mac.init( secretKey );
            final byte[] macData = mac.doFinal(input.getBytes());
            byte[] hex = new Hex().encode(macData);
            _sign = new String( hex, "ISO-8859-1" );
        } catch(Exception e){
        	error = e.toString();
        	return null;
        }
        return _sign;       
	}	
}
