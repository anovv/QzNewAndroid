package ru.anov.qzproject.models;

import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class Friend  implements Parcelable{

	private String id;
	private String name;
	private String thumbnail_img_url;
	
	public Friend(String id, String name, String thumbnail_img_url){
		this.id = id;
		this.name = name;
		this.thumbnail_img_url = thumbnail_img_url;
	}
	
	public Friend(Map<String, String> friend){
		this.id = friend.get("id");
		this.name = friend.get("name");
		this.thumbnail_img_url = friend.get("thumbnail_img_url");
	}
	
	public Friend(Parcel in){
		readFromParcel(in);
	}
	
	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getThumbnailImgUrl(){
		return thumbnail_img_url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(thumbnail_img_url);
	}
	
	private void readFromParcel(Parcel in){
		id = in.readString();
		name = in.readString();
		thumbnail_img_url = in.readString();
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Friend createFromParcel(Parcel source) {
			return new Friend(source);
		}

		@Override
		public Friend[] newArray(int size) {
			return new Friend[size];
		}
		
	}; 
}
