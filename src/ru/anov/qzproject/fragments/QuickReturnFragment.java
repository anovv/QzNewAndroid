package ru.anov.qzproject.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class QuickReturnFragment extends ListFragment{

	public static enum QuickReturnState {
        ON_SCREEN,
        OFF_SCREEN,
        RETURNING,
        HIDING
    }
	
    public static QuickReturnState quickReturnState = QuickReturnState.ON_SCREEN;
    private ObjectAnimator hideAnimator;
    private ObjectAnimator returnAnimator;
    private View quickReturnView;
    private View detectView;
    private boolean isTouched = false;
    private boolean isEnabled = true;
    
	private class GestureListener extends SimpleOnGestureListener {
		
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	    	if(e1 == null || e2 == null){
	    		return false;
	    	}
	    	isTouched = true;
	    	float positionY = e2.getY();
	    	float lastY = e1.getY();
	
	        switch (quickReturnState) {
            case OFF_SCREEN:
                if (!quickReturnBarIsReturning()
                    && (positionY > lastY))
                        returnAnimator.start();
                break;
 
            case ON_SCREEN:
                if (!quickReturnBarIsGoingAway()
                    && (positionY < lastY))
                    hideAnimator.start();
                break;
 
            case RETURNING:
            	if (positionY < lastY) {
                    returnAnimator.cancel();
                    hideAnimator.start();
                }
                break;
 
            case HIDING:
            	if (positionY > lastY) {
                    hideAnimator.cancel();
                    returnAnimator.start();
                }
                break;
	        }
	        
	        return false;
	    }
	}
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setAnimators();
	}
	
	private void setAnimators(){
		if(quickReturnView == null){
			return;
		}
		
		returnAnimator = ObjectAnimator.ofFloat(quickReturnView, "translationY", 0);
        returnAnimator.addListener(new Animator.AnimatorListener() {
                   
        	@Override
        	public void onAnimationStart(Animator animator) {
        		quickReturnState = QuickReturnState.RETURNING;
        	}
            
        	@Override
        	public void onAnimationEnd(Animator animator) {
        		quickReturnState = QuickReturnState.ON_SCREEN;
        	}
            
        	@Override
        	public void onAnimationCancel(Animator animator) {
        		quickReturnState = QuickReturnState.OFF_SCREEN;
        	}
            
        	@Override
        	public void onAnimationRepeat(Animator animator) {}
            
        });
       
        hideAnimator = ObjectAnimator.ofFloat(quickReturnView, "translationY", 400);
        hideAnimator.addListener(new Animator.AnimatorListener() {
                   
        	@Override
            public void onAnimationStart(Animator animator) {
        		quickReturnState = QuickReturnState.HIDING;
        	}
            
        	@Override
        	public void onAnimationEnd(Animator animator) {
        		quickReturnState = QuickReturnState.OFF_SCREEN;
        	}
            
        	@Override
        	public void onAnimationCancel(Animator animator) {
        		quickReturnState = QuickReturnState.ON_SCREEN;
        	}
            
        	@Override
        	public void onAnimationRepeat(Animator animator) {}
            
        });
       
        
        final GestureDetector gd = new GestureDetector(getActivity(), new GestureListener());
        OnTouchListener ol = new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!isEnabled){
					return false;
				}
				gd.onTouchEvent(event);
				return false;
			}
    	};

    	if(detectView == null){
    		detectView = getListView();
    	}
    	detectView.setOnTouchListener(ol);
	}
	
	private boolean quickReturnBarIsReturning() {
		return returnAnimator.isRunning() || returnAnimator.isStarted();
	}
	
	private boolean quickReturnBarIsGoingAway() {
		return hideAnimator.isRunning() || hideAnimator.isStarted();
	}
	
	public void setQuickReturnView(View quickReturnView){
		this.quickReturnView = quickReturnView;
	}
	
	public void hideQuickReturnView(){
		if(hideAnimator == null){
			return;
		}
		if(!isTouched){
			hideAnimator.start();
		}
	}
	
	public void showQuickReturnView(){
		if(returnAnimator == null){
			return;
		}
		returnAnimator.start();
	}
	
	public void setDetectView(View detectView){
		this.detectView = detectView;
	}
	
	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
	}
}
