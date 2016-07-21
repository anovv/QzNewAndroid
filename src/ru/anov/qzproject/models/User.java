package ru.anov.qzproject.models;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

	private String id;
	private String name;
	private String profileImgUrl;
	private String thumbnailImgUrl;
	
	private String wins;
	private String total;
	private String status;
	private String bestIn;
	private String friendsCount;
	
	private boolean isFriend;
	private boolean isBlocked;
	private boolean isBlockedByMe;
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getStatus(){
		return status;
	}
	
	public String getWins(){
		return wins;
	}
	
	public String getTotal(){
		return total;
	}
	
	public String getBestIn(){
		return bestIn;
	}
	
	public int getFriendsCount(){
		try{
			return Integer.parseInt(friendsCount);	
		}catch(Exception e){
			return 0;
		}
	}
	
	
	public boolean isFriend(){
		return isFriend;
	}
	
	public boolean isBlocked(){
		return isBlocked;
	}
	
	public boolean isBlockedByMe(){
		return isBlockedByMe;
	}
	
	public String getProfileImgUrl(){
		return profileImgUrl;
	}
	
	public String getThumbnailImgUrl(){
		return thumbnailImgUrl;
	}
	
	public Map<String, String> toMap(){
		Map<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		res.put("fullname", name);
		res.put("wins", wins);
		res.put("total", total);
		res.put("status", status);
		res.put("best_in", bestIn);
		res.put("friends_count", friendsCount);
		res.put("profile_img_url", profileImgUrl);
		res.put("thumbnail_img_url", thumbnailImgUrl);
		res.put("is_friend", (isFriend) ? "1" : "0");
		res.put("is_blocked", (isBlocked) ? "1" : "0");
		res.put("is_blocked_by_me", (isBlockedByMe) ? "1" : "0");
		return res;
	}
	
	public User(Map<String, String> user){
		this.id = user.get("id");
		this.name = user.get("fullname");
		this.wins = user.get("wins");
		this.total = user.get("total");
		this.status = user.get("status");
		this.bestIn = user.get("best_in");
		this.friendsCount = user.get("friends_count");
		this.profileImgUrl = user.get("profile_img_url");
		this.thumbnailImgUrl = user.get("thumbnail_img_url");
		this.isFriend = (user.get("is_friend").equals("1"));
		this.isBlocked = (user.get("is_blocked").equals("1"));
		this.isBlockedByMe = (user.get("is_blocked_by_me").equals("1"));
		
	}
	
	public User(Parcel in){
		readFromParcel(in);
	}

	public Friend toFriend(){
		Map<String, String> m = new HashMap<String, String>();
		m.put("name", name);
		m.put("id", id);
		m.put("thumbnail_img_url", thumbnailImgUrl);
		
		return new Friend(m);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(status);
		dest.writeString(bestIn);
		dest.writeString(friendsCount);
		dest.writeString(profileImgUrl);
		dest.writeString(thumbnailImgUrl);
		dest.writeByte((byte) (isFriend ? 1 : 0));
		dest.writeByte((byte) (isBlocked ? 1 : 0));
		dest.writeByte((byte) (isBlockedByMe ? 1 : 0));
	}
	
	private void readFromParcel(Parcel in){
		id = in.readString();
		name = in.readString();
		status = in.readString();
		bestIn = in.readString();
		friendsCount = in.readString();
		profileImgUrl = in.readString();
		thumbnailImgUrl = in.readString();
		isFriend = in.readByte() != 0;
		isBlocked = in.readByte() != 0;
		isBlockedByMe = in.readByte() != 0;
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
		
	};
}
