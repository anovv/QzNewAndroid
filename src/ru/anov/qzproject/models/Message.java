package ru.anov.qzproject.models;

import java.util.Map;

public class Message {
	
	private String name;
	private String message;
	private String thumbnailImgUrl;
	private String timestamp;
	private String type; //0 - user, 1 - ruser, 2 - game result
	private String ruserId;
	
	public Message(Map<String, String> map){
		name = map.get("name");
		message = map.get("message");
		thumbnailImgUrl = map.get("thumbnail_img_url");
		timestamp = map.get("timestamp");
		type = map.get("type");
		ruserId = map.get("ruser_id");
	}
	

	public String getName(){
		return name;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getThumbnailImgUrl(){
		return thumbnailImgUrl;
	}
	
	public String getTimestamp(){
		return timestamp;
	}
	
	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}
	
	public String getType(){
		return type;
	}
	
	public String getRUserId(){
		return ruserId;
	}
}
