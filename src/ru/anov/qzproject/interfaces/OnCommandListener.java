package ru.anov.qzproject.interfaces;

import java.util.List;

public interface OnCommandListener {
	
	public void onError(int errorCode);
	
	public void onQuestions(List<String> qIds);
	
	public void onAnswer(String answer, int time, int round);
	
	public void onNext();
	
	public void onFinalize();
	
	public void onRandom(String rid, List<String> qIds);

	public void onDecline(String id);

	public void onRematch(String id, String themeId);
	
}
