package ru.anov.qzproject.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class Theme implements Parcelable{
	
	/*
	 * tree depth is never more than 2
	 * */
	
	private String id;
	private String name;
	private String description;
	private String popularity;
	private String parentId;
	private String range;
	private String locked;
	private boolean isParent;
	private ArrayList<Theme> children;
	
	public Theme(String name){
		id = "";
		this.name = name;
	}
	
	public Theme(Map<String, String> theme){
		this.id = theme.get("id");
		this.name = theme.get("name");
		this.description = theme.get("description");
		this.popularity = theme.get("popularity");
		this.parentId = theme.get("parent");
		this.range = theme.get("range");
		this.locked = theme.get("locked");
		if(theme.get("parent") == null){
			this.isParent = true;
		}else{
			this.isParent = theme.get("parent").equals("0");
		}
	}
	
	public static ArrayList<Theme> toPopularList(ArrayList<Map<String, String>> themes, int limit){
		ArrayList<Theme> res = new ArrayList<Theme>();
		Collections.sort(themes, new PopularComparator());
		for(int i = 0; i < themes.size() && i < limit; i++){
			if(!themes.get(i).get("parent").equals("0")){
				res.add(new Theme(themes.get(i)));
			}
		}
		
		return res;
	}
	
	public static ArrayList<Theme> toNewList(ArrayList<Map<String, String>> themes, String newIds){
		ArrayList<Theme> res = new ArrayList<Theme>();
		String[] ids = newIds.split("_");
		
		for(Map<String, String> theme: themes){
			for(String id : ids){
				if(theme.get("id").equals(id)){
					res.add(new Theme(theme));
					break;
				}
			}
		}
		
		return res;
	}
	
	public static ArrayList<Theme> toList(List<Map<String, String>> themes){
		ArrayList<Theme> result = new ArrayList<Theme>();
		
		if(themes == null){
			return result;
		}
		
		for(Map<String, String> map : themes){
			Theme theme = new Theme(map);
			if(theme.isParent()){
				String id = theme.getId(); 
				ArrayList<Theme> children = new ArrayList<Theme>();
				for(Map<String, String> m : themes){
					Theme child = new Theme(m);
					if(id.equals(child.getParentId())){
						children.add(child);
					}
				}
				theme.setChildren(children);
				result.add(theme);
			}
		}
		
		return result;
	}
	
	public String getId(){
		return id;
	}
	
	public String getParentId(){
		return parentId;
	}
	
	public String getName(){
		return name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public String getPopularity(){
		return popularity;
	}
	
	public boolean isParent(){
		return isParent;
	}
	
	public String getRange(){
		return range;
	}
	
	public ArrayList<Theme> getChildren(){
		return children;
	}
	
	public void setChildren(ArrayList<Theme> children){
		this.children = children;
	}
	
	public String getLocked(){
		return locked;
	}

	public void setLocked(String locked){
		this.locked = locked;
	}
	
	public Theme(Parcel in){
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(popularity);
		dest.writeString(parentId);
		dest.writeString(range);
		dest.writeString(locked);
		dest.writeByte((byte) ((isParent) ? 1 : 0));
		if(isParent){
			dest.writeInt(children.size());//NPE
			for(int i = 0; i < children.size(); i++){
				Theme child = children.get(i);
				dest.writeString(child.getId());
				dest.writeString(child.getName());
				dest.writeString(child.getDescription());
				dest.writeString(child.getPopularity());
				dest.writeString(child.getParentId());
				dest.writeString(child.getRange());
				dest.writeString(child.getLocked());
				dest.writeByte((byte) (child.isParent() ? 1 : 0));
			}
		}
	}
	
	private void readFromParcel(Parcel in){
		id = in.readString();
		name = in.readString();
		description = in.readString();
		popularity = in.readString();
		parentId = in.readString();
		range = in.readString();
		locked = in.readString();
		isParent = in.readByte() != 0;
		if(isParent){
			int size = in.readInt();
			children = new ArrayList<Theme>();
			for(int i = 0; i < size; i++){
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", in.readString());
				map.put("name", in.readString());
				map.put("description", in.readString());
				map.put("popularity", in.readString());
				map.put("parent", in.readString());
				map.put("range", in.readString());
				map.put("locked", in.readString());
				Theme theme = new Theme(map);
				children.add(theme);
			}
		}
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Theme createFromParcel(Parcel source) {
			return new Theme(source);
		}

		@Override
		public Theme[] newArray(int size) {
			return new Theme[size];
		}
		
	};
	
	private static class PopularComparator implements Comparator<Map<String, String>>{

		@Override
		public int compare(Map<String, String> lhs, Map<String, String> rhs) {
			long l1 = Long.parseLong(lhs.get("popularity"));
			long l2 = Long.parseLong(rhs.get("popularity"));
			return (int)(l2 - l1);
		}
		
	}
}
