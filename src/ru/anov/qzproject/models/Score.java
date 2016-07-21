package ru.anov.qzproject.models;

import java.util.Map;

public class Score {

	private String userId;
	private String themeId;
	private String name;
	private String score;
	private String thumbnailImgUrl;
	
	public Score(Map<String, String> score){
		this.userId = score.get("user_id");
		this.themeId = score.get("theme_id");
		this.name = score.get("name");
		this.score = score.get("score");
		this.thumbnailImgUrl = score.get("thumbnail_img_url");
	}
	
	public String getUserId(){
		return userId;
	}
	
	public String getThemeId(){
		return themeId;
	}
	
	public String getUserName(){
		return name;
	}
	
	public String getScore(){
		return score;
	}
	
	public String getThumbnailImgUrl(){
		return thumbnailImgUrl;
	}
	
}
