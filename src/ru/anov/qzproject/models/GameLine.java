package ru.anov.qzproject.models;

public class GameLine {

	private Friend friend;
	private Theme theme;
	public static int STATE = 0;
	
	private static GameLine instance;
	
	private GameLine(){
		
	}
	
	public static GameLine getInstance(){
		if(instance == null){
			instance = new GameLine();
		}
		return instance;
	}
	
	public Friend getFriend(){
		return friend;
	}
																												
	public void setFriend(Friend friend){
		this.friend = friend;
	}
	
	public Theme getTheme(){
		return theme;
	}
	
	public void setTheme(Theme theme){
		this.theme = theme;
	}
	
	
}
