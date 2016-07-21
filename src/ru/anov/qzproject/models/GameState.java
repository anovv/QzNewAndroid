package ru.anov.qzproject.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import android.graphics.Bitmap;

public class GameState {
	private int index = -1;
	private int round = 0;
	
	private Map<String, Bitmap> images;
	private List<String> qIds;
	private Map<String, Map<String, String>> questions;
	private Map<String, Map<String, String>> offlineAnswers;
	
	private Set<String> ansSeqAnsweredIds;
	private String ansSeq = "";
	
	
	private int score = 0;
	private int rscore = 0;
	
	private boolean hasRUserAnswered = false;
	private boolean hasUserAnswered = false;
	
	public GameState(){
		ansSeqAnsweredIds = new HashSet<String>();
	}
	
	public void setImages(Map<String, Bitmap> images){
		this.images = images;
	}
	
	public void setQuestions(Map<String, Map<String, String>> questions){
		this.questions = questions;
	}
	
	public void setQIds(List<String> qIds){
		this.qIds = qIds;
	}
	
	public int getScore(){
		return score;
	}
	
	public int getRScore(){
		return rscore;
	}
	
	public int getRound(){
		return round;
	}
	
	public void next(){
		index++;
		round++;
		hasRUserAnswered = false;
		hasUserAnswered = false;
	}
	
	/*private Map<String, Map<String, String>> getShuffledQuestions(Map<String, Map<String, String>> questions){
		Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
		for(Entry<String, Map<String, String>> entry : questions.entrySet()){
			Map<String, String> q = new HashMap<String, String>();
			Map<String, String> question = entry.getValue();
			for(Entry<String, String> en : question.entrySet()){
				q.put(en.getKey(), en.getValue());
			}
			
			Map<String, String> connector = shuffledConnectors.get(entry.getKey());
			String realAns1 = question.get("ans1");
			String realAns2 = question.get("ans2");
			String realAns3 = question.get("ans3");
			String realAns4 = question.get("ans4");
			String rightAns = question.get("right_ans");
			
			for(int i = 1; i <= 4; i++){
				String ind = i + "";
				String s = connector.get(ind);
				String a = "ans" + i;
				if(s.equals("1")){
					q.put(a, realAns1);
				}else if(s.equals("2")){
					q.put(a, realAns2);
				}else if(s.equals("3")){
					q.put(a, realAns3);
				}else{
					q.put(a, realAns4);
				}
			}
			
			String fakeRightAns = connector.get(rightAns);
			q.put("right_ans", fakeRightAns);
			res.put(entry.getKey(), q);
		}
		
		return res;
	}*/
	
	public Map<String, String> getQuestion(){
		try{
			return questions.get(qIds.get(index));
		}catch(Exception e){
			return questions.get(qIds.get(0));
		}
	}
	
	public Bitmap getImage(){
		try{
			return images.get(qIds.get(index));
		}catch(Exception e){
			return null;
		}
	}
	
	public boolean isFinished(){
		return index == qIds.size();
	}
	
	public boolean hasRUserAnswered(){
		return hasRUserAnswered;
	}
	
	public void setRUserAnswered(){
		hasRUserAnswered = true;
	}
	
	public boolean hasUserAnswered(){
		return hasUserAnswered;
	}
	
	public void setUserAnswered(){
		hasUserAnswered = true;
	}
	
	public boolean isAnswerRight(String answer, int round){
		return questions.get(qIds.get(round - 1)).get("right_ans").equals(answer);
	}
	
	public void incrScore(int score){
		if(isLastRound()){
			this.score += (4*score);
		}else{
			this.score += (2*score);
		}
	}
	
	public void incrRScore(int score){
		if(isLastRound()){
			rscore += (4*score);
		}else{
			rscore += (2*score);
		}
	}
	
	public boolean isLastRound(){
		return index == qIds.size() - 1;
	}
	
	public String getCurQuestionId(){
		return qIds.get(index);
	}
	
	public void setOfflineAnsSeq(String ansSeq){
		offlineAnswers = new HashMap<String, Map<String, String>>();
		if(ansSeq.length() == 0){
			return;
		}
		
		String[] a = ansSeq.split("_");// id1#answer1;time1_id2#answer2;time2_...
		
		for(String s : a){
			String[] b = s.split("#");
			String qId = b[0];
			String[] c = b[1].split(";");
			String answer = c[0];
			String time = c[1];
			Map<String, String> m = new HashMap<String, String>();
			m.put("answer", answer);
			m.put("time", time);
			offlineAnswers.put(qId, m);
		}
	}
	
	public int getOfflineAnswerTime(String qId){
		Map<String, String> ans = offlineAnswers.get(qId);
		if(ans == null){
			return 0;
		}
		
		return Integer.parseInt(ans.get("time"));
	}
	
	public String getOfflineAnswer(String qId){
		Map<String, String> ans = offlineAnswers.get(qId);
		if(ans == null){
			return "0";
		}
		
		return ans.get("answer");
	}
	
	public int getQuestionsCount(){
		if(questions == null){
			return 0 ;
		}
		return questions.size();
	}
	
	public void setAnsSeqAnswer(String answer, int time){
		String qId = getCurQuestionId();
		if(!ansSeqAnsweredIds.contains(qId)){
			ansSeqAnsweredIds.add(qId);
			ansSeq += (qId + "#" + answer + ";" + time + "_");
		}
	}
	
	public void generateRandomAns(int time){
		String qId = getCurQuestionId();
		if(!ansSeqAnsweredIds.contains(qId)){
			ansSeqAnsweredIds.add(qId);
			Random r = new Random();
			int randomTime = r.nextInt(time + 1);
			String randomAnswer = (r.nextInt(4) + 1) + "";
			ansSeq += (qId + "#" + randomAnswer + ";" + randomTime + "_");
		}
	}
	
	public String getAnsSeq(){
		if(ansSeq.length() == 0){
			return "";
		}
		
		if(!isValidAnsSeq(ansSeq.substring(0, ansSeq.length() - 1))){
			return "";
		}
		
		return ansSeq.substring(0, ansSeq.length() - 1);
	}
	
	private boolean isValidAnsSeq(String ansSeq){
		int counter = 0;
		for(char c : ansSeq.toCharArray()){
			if(c == '_'){
				counter++;
			}
		}
		
		return (counter == 5);
	}
	
	private Map<String, Map<String, String>> shuffle(Map<String, Map<String, String>> questions){
		Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
		List<String> l = new ArrayList<String>();
		l.add("1");l.add("2");l.add("3");l.add("4");
		for(Entry<String, Map<String, String>> entry : questions.entrySet()){
			Map<String, String> connector = new HashMap<String, String>();
			Collections.shuffle(l);
			connector.put("1", l.get(0));
			connector.put("2", l.get(1));
			connector.put("3", l.get(2));
			connector.put("4", l.get(3));
			res.put(entry.getKey(), connector);
		}
		
		return res;
	}
	
	/*public String getRealAnswer(String answer, int round){
		Map<String, String> connector = shuffledConnectors.get(qIds.get(round - 1));
		String ans = "1";
		for(Entry<String, String> entry : connector.entrySet()){
			if(entry.getValue().equals(answer)){
				ans = entry.getKey();
				break;
			}
		}
		return ans;
	}
	
	public String getFakeAnswer(String answer, int round){
		return shuffledConnectors.get(qIds.get(round - 1)).get(answer); 
	}*/
}
