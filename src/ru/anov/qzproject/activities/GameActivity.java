package ru.anov.qzproject.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ru.anov.qzproject.R;
import ru.anov.qzproject.asynctasks.CancelRequest;
import ru.anov.qzproject.asynctasks.DeclineRequest;
import ru.anov.qzproject.asynctasks.NotifyOffline;
import ru.anov.qzproject.client.Client;
import ru.anov.qzproject.fragments.UserFragment;
import ru.anov.qzproject.interfaces.OnCommandListener;
import ru.anov.qzproject.models.GameState;
import ru.anov.qzproject.models.Message;
import ru.anov.qzproject.models.MessageDao;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.services.GCMIntentService;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class GameActivity extends Activity implements OnCommandListener, OnClickListener{

	private static final long ROUNDVIEW_TIME_MILLIS = 3500;// must be more than POST_ANSWER_DELAY_MILLIS
	private static final long CONNECTION_CHECK_TIME_MILLIS = 6000;
	private static final long POST_ANSWER_DELAY_MILLIS = 1500;
	private static final long OFFLINE_BUTTON_SHOW_DELAY_MILLIS = 3000;
	private static final long PROFILE_TIME_MILLIS = 4000;
	
//	private static final long QUESTION_TO_ANSWERS_MILLIS = 1000; // between question and answer
	private static final long ANSWERS_TO_START_MILLIS = 1000; // between answers and countdown
	
	
	private long OFFLINE_BUTTON_RANDOM_SHOW_DELAY_MILLIS = 4000;
	
	private boolean notSaveResultInDB = false;
	private volatile boolean isCountdownRunning = false;
	private volatile boolean isRUserResponded = false;
	private volatile boolean isCancelled = false;
	private volatile boolean isNextRequested = false;
	private volatile boolean nullify = true;
	
	private volatile boolean isOfflineMode = false;
	private volatile boolean isOfflineResponding = false;
	private volatile boolean isOfflineAnswerHandled = false;
	private volatile boolean isOfflineGameButtonExpanded = false;
	private volatile boolean isBot = false;
	
	private String rAnsSeq = "";
	
//	private ExecutorService countdownExecutor;
//	private CountdownRunnable countdownRunnable;
	
	private volatile int countdownScore = 0;
	private volatile int countdownTimeMillis = 10*1000;
	private int STATE = 0; // 0 - loading, 1 - game, 2 - final
	private boolean isFinished = false;
	private boolean rematchNotify = true;
	private boolean isBest = false;
	
	private Client client;
	private String id;
	private String rid;
	private String themeId;
	private boolean isRequesting;
	private boolean isRandom;
	
	private String themeName;
	
	private Map<String, String> ruser;
	
	//UI
	private View curView;
	
	//loading
	private View loadingView;
	private TextView loadingTextView;
	private TextView loadingUserNameTextView;
	private TextView loadingLevelTextView;
	private ImageView loadingImageView;
	private TextView loadingNotificationTextView;
	
	//round
	private View roundView;
	private TextView roundTextView;
	
	//main
	private View mainView;
	private ProgressBar countdownProgressBar;
	private TextView countdownTextView;
	private ImageView userImageView;
	private TextView userNameTextView;
	private TextView userLevelTextView;
	private TextView userScoreTextView;
	private ImageView ruserImageView;
	private TextView ruserNameTextView;
	private TextView ruserLevelTextView;
	private TextView ruserScoreTextView;
	private ImageView questionImageView;
	private View questionImageViewFrame;
	private TextView questionTextView;
	
	private View userIndicator;
    private View ruserIndicator;

	private ImageView userRightImageView;
	private ImageView ruserRightImageView;
	private ImageView userWrongImageView;
	private ImageView ruserWrongImageView;
	
	private Button ans1Button;
	private Button ans2Button;
	private Button ans3Button;
	private Button ans4Button;
	
	private View buttonsView;
	
	//final
	private View finalView;
	private ImageView userFinalImageView;
	private ImageView ruserFinalImageView;
	private TextView userFinalNameTextView;
	private TextView ruserFinalNameTextView;
	private TextView userFinalScoreTextView;
	private TextView ruserFinalScoreTextView;
	private TextView finalResultTextView;
	
	private TextView finalScoreTextView;
	private TextView finalWinbonusTextView;
	private TextView finalMultTextView;
	private TextView finalTotalTextView;
	private TextView finalLevelTextView;
	private TextView finalStatusTextView;
	
	private TextView finalScoreValueTextView;
	private TextView finalWinbonusValueTextView;
	private TextView finalMultValueTextView;
	private TextView finalTotalValueTextView;
	private TextView finalLevelValueTextView;
	private TextView finalStatusValueTextView;
	
	private Button finalRematchButton;
	private Button finalQuitButton;
	private Button finalNextButton;
	private Button finalChatButton;
	private TextView finalOfflineTextView;
	
	private Button startOfflineGameButton;
	//error
	private View errorView;
	private TextView errorTextView;
	private Button errorButton;
	
	private GameState gameState;
	private Target target;
	
	private Integer curColor;
	private volatile boolean isBgAnimating = false;
	private volatile boolean isDotAnimating = true;
	private volatile boolean checkConnection = false;
	private volatile boolean isBadConnection = false;
	private Thread dotAnimationThread;
	private Thread connectionCheckThread;
	private int messageCounter = 0;
	private long startTime = 0;
	private boolean isRandomRecord = false;
	
	private TimerTask timerTask;
	private Timer timer;
	private int time;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		if(savedInstanceState != null){
			return;
		}
		animateDots();
		
		NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifManager.cancelAll();
		
		GCMIntentService.STATE = 2;
		GCMIntentService.gameActivity = this;
		id = APIHandler.user_id;
		
		if(getIntent().getExtras().containsKey("isOfflineResponse")){
			rid = getIntent().getExtras().getString("rid");
			rAnsSeq = getIntent().getExtras().getString("ansSeq");
			isOfflineMode = true;
			isOfflineResponding = true;
		}else{
			isRandom = getIntent().getExtras().getBoolean("isRandom");
			isRequesting = getIntent().getExtras().getBoolean("isRequesting");
			if(!isRandom){
				rid = getIntent().getExtras().getString("rid");
			}
		}
		themeId = getIntent().getExtras().getString("themeId");
		themeName = getIntent().getExtras().getString("themeName");
		String randomWaitMillis = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("random_wait_millis", "4000");
		try{
			OFFLINE_BUTTON_RANDOM_SHOW_DELAY_MILLIS = Long.parseLong(randomWaitMillis);
		}catch(Exception e){
			OFFLINE_BUTTON_RANDOM_SHOW_DELAY_MILLIS = 4000;
		}
//		countdownRunnable = new CountdownRunnable();
		gameState = new GameState();

		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		
		loadingView = findViewById(R.id.activity_game_loading_view);
		loadingTextView = (TextView) findViewById(R.id.activity_game_loading_textview);
		loadingTextView.setTypeface(tf);
		loadingUserNameTextView = (TextView) findViewById(R.id.activity_game_loading_user_name_textview);
		loadingUserNameTextView.setTypeface(tf);
		loadingLevelTextView = (TextView) findViewById(R.id.activity_game_loading_level_textview);
		loadingLevelTextView.setTypeface(tf);
		loadingImageView = (ImageView) findViewById(R.id.activity_game_loading_imageview);
		loadingNotificationTextView = (TextView) findViewById(R.id.activity_game_loading_notif_textview);
		loadingNotificationTextView.setTypeface(tf);
		
		roundView = findViewById(R.id.activity_game_round_view);
		roundTextView = (TextView) findViewById(R.id.activity_game_round_textview);
		roundTextView.setTypeface(tf);
		
		mainView = findViewById(R.id.activity_game_main_view);
		countdownProgressBar = (ProgressBar) findViewById(R.id.activity_game_main_countdown_progressbar);
		countdownTextView = (TextView) findViewById(R.id.activity_game_main_countdown_textview);
		userImageView = (ImageView) findViewById(R.id.activity_game_main_user_imageview);
		userNameTextView = (TextView) findViewById(R.id.activity_game_main_user_name_textview);
		userLevelTextView = (TextView) findViewById(R.id.activity_game_main_user_level_textview);
		userScoreTextView = (TextView) findViewById(R.id.activity_game_main_user_score_textview);
		ruserImageView = (ImageView) findViewById(R.id.activity_game_main_ruser_imageview);
		ruserLevelTextView = (TextView) findViewById(R.id.activity_game_main_ruser_level_textview);
		ruserNameTextView = (TextView) findViewById(R.id.activity_game_main_ruser_name_textview);
		ruserScoreTextView = (TextView) findViewById(R.id.activity_game_main_ruser_score_textview);
		questionImageView = (ImageView) findViewById(R.id.activity_game_main_question_imageview);
		questionTextView = (TextView) findViewById(R.id.activity_game_main_question_textview);
		questionImageViewFrame = findViewById(R.id.activity_game_main_question_imageview_frame);
		startOfflineGameButton = (Button) findViewById(R.id.activity_game_loading_offline_button);
		
		userIndicator = findViewById(R.id.activity_game_main_user_indicator);
		ruserIndicator = findViewById(R.id.activity_game_main_ruser_indicator);
		
		userRightImageView = (ImageView) findViewById(R.id.activity_game_main_user_right);
		userWrongImageView = (ImageView) findViewById(R.id.activity_game_main_user_wrong);
		ruserRightImageView = (ImageView) findViewById(R.id.activity_game_main_ruser_right);
		ruserWrongImageView = (ImageView) findViewById(R.id.activity_game_main_ruser_wrong);
		
		ans1Button = (Button) findViewById(R.id.activity_game_ans1_button);
		ans2Button = (Button) findViewById(R.id.activity_game_ans2_button);
		ans3Button = (Button) findViewById(R.id.activity_game_ans3_button);
		ans4Button = (Button) findViewById(R.id.activity_game_ans4_button);
		buttonsView = findViewById(R.id.activity_game_main_buttons_view);
		ans1Button.setOnClickListener(this);
		ans2Button.setOnClickListener(this);
		ans3Button.setOnClickListener(this);
		ans4Button.setOnClickListener(this);
		
		/*OnFocusChangeListener l = new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
		            v.performClick();
		        }
			}
		};
		
		ans1Button.setOnFocusChangeListener(l);
		ans2Button.setOnFocusChangeListener(l);
		ans3Button.setOnFocusChangeListener(l);
		ans4Button.setOnFocusChangeListener(l);*/
		
		countdownTextView.setTypeface(tf);
		userNameTextView.setTypeface(tf);
		userLevelTextView.setTypeface(tf);
		userScoreTextView.setTypeface(tf);
		ruserNameTextView.setTypeface(tf);
		ruserLevelTextView.setTypeface(tf);
		ruserScoreTextView.setTypeface(tf);
		questionTextView.setTypeface(tf);
		ans1Button.setTypeface(tf);
		ans2Button.setTypeface(tf);
		ans3Button.setTypeface(tf);
		ans4Button.setTypeface(tf);
		
		errorView = findViewById(R.id.activity_game_error_view);
		errorTextView = (TextView) findViewById(R.id.activity_game_error_textview);
		errorButton = (Button) findViewById(R.id.activity_game_error_button);
		errorButton.setOnClickListener(this);
		
		errorTextView.setTypeface(tf);
		errorButton.setTypeface(tf);
		
		String userName = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_name", "");
		userNameTextView.setText(userName);
		String userLevel = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_level_" + themeId, "1");
		userLevelTextView.setText(getResources().getString(R.string.level) + userLevel);
		
		String userThumbnailImgUrl = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_thumbnail_img_url", "");
		if(userThumbnailImgUrl.length() != 0){
			Picasso.with(this).load(userThumbnailImgUrl).into(userImageView);
		}
		
		finalView = findViewById(R.id.activity_game_final_view);

		userFinalImageView = (ImageView) findViewById(R.id.activity_game_final_user_imageview);
		ruserFinalImageView = (ImageView) findViewById(R.id.activity_game_final_ruser_imageview);
		userFinalNameTextView = (TextView) findViewById(R.id.activity_game_final_user_name_textview);
		ruserFinalNameTextView = (TextView) findViewById(R.id.activity_game_final_ruser_name_textview);
		userFinalScoreTextView = (TextView) findViewById(R.id.activity_game_final_user_score_textview);
		ruserFinalScoreTextView = (TextView) findViewById(R.id.activity_game_final_ruser_score_textview);
		finalResultTextView = (TextView) findViewById(R.id.activity_game_final_result_textview);
		finalScoreTextView = (TextView) findViewById(R.id.activity_game_final_score_textview);
		finalWinbonusTextView = (TextView) findViewById(R.id.activity_game_final_winbonus_textview);
		finalMultTextView = (TextView) findViewById(R.id.activity_game_final_mult_textview);
		finalTotalTextView = (TextView) findViewById(R.id.activity_game_final_total_textview);
		finalLevelTextView = (TextView) findViewById(R.id.activity_game_final_level_textview);
		finalStatusTextView = (TextView) findViewById(R.id.activity_game_final_status_textview);
		finalRematchButton = (Button) findViewById(R.id.activity_game_final_rematch_button);
		finalQuitButton = (Button) findViewById(R.id.activity_game_final_quit_button);
		finalNextButton = (Button) findViewById(R.id.floating_button);
		finalChatButton = (Button) findViewById(R.id.chat_button);
		finalOfflineTextView = (TextView) findViewById(R.id.activity_game_final_offline_textview);
		finalQuitButton.setOnClickListener(this);
		finalRematchButton.setOnClickListener(this);
		finalNextButton.setOnClickListener(this);
		
		finalScoreValueTextView = (TextView) findViewById(R.id.activity_game_final_score_value_textview);
		finalWinbonusValueTextView  = (TextView) findViewById(R.id.activity_game_final_winbonus_value_textview);
		finalMultValueTextView = (TextView) findViewById(R.id.activity_game_final_mult_value_textview);
		finalTotalValueTextView = (TextView) findViewById(R.id.activity_game_final_total_value_textview);
		finalLevelValueTextView = (TextView) findViewById(R.id.activity_game_final_level_value_textview);
		finalStatusValueTextView = (TextView) findViewById(R.id.activity_game_final_status_value_textview);
		String userProfileImgUrl = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_profile_img_url", "");
		if(userProfileImgUrl.length() != 0){
			Picasso.with(this).load(userProfileImgUrl).into(userFinalImageView);
		}
		
		userFinalNameTextView.setTypeface(tf);
		ruserFinalNameTextView.setTypeface(tf);
		userFinalScoreTextView.setTypeface(tf);
		ruserFinalScoreTextView.setTypeface(tf);
		finalResultTextView.setTypeface(tf);
		finalScoreTextView.setTypeface(tf);
		finalWinbonusTextView.setTypeface(tf);
		finalMultTextView.setTypeface(tf);
		finalStatusTextView.setTypeface(tf);
		finalRematchButton.setTypeface(tf);
		finalLevelTextView.setTypeface(tf);
		finalTotalTextView.setTypeface(tf);
		finalQuitButton.setTypeface(tf);
		finalNextButton.setTypeface(tf);
		finalChatButton.setTypeface(tf);
		startOfflineGameButton.setTypeface(tf);
		
		finalScoreValueTextView.setTypeface(tf);
		finalWinbonusValueTextView.setTypeface(tf);
		finalMultValueTextView.setTypeface(tf);
		finalTotalValueTextView.setTypeface(tf);
		finalLevelValueTextView.setTypeface(tf);
		finalStatusValueTextView.setTypeface(tf);
		finalOfflineTextView.setTypeface(tf);
		
		curView = loadingView;
		
		ruserFinalImageView.setOnClickListener(new OnClickListener(){

			@Override 
			public void onClick(View v) {
				Intent intent = new Intent(GameActivity.this, BaseActivity.class);
				intent.putExtra("id", rid);
				intent.putExtra("fragment", UserFragment.class.getSimpleName());
				startActivity(intent);
			}
		});
		
		client = new Client(APIHandler.dynamic_server_port, APIHandler.dynamic_server_ip, this, id, rid, themeId);
		
		setButtonsClickable(false);
		hideNextButton();
		startOfflineGameButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				expandOfflineGameButton();
				if(!isRandom){
					ruserScoreTextView.setText("?");
				}
				startOfflineGame();
			}
		});
		startOfflineGameButton.setVisibility(View.GONE);
	
		if(!isOfflineMode){
			if(isRandom){
				new StartGame().setContext(this).execute();
			}else{
				Utils.executeAsyncTask(new GetRUser().setContext(this), null);
				Utils.executeAsyncTask(new StartGame().setContext(this), null);
			}
		}else{
			new GetRUser().setContext(this).execute();
		}
		
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				if((isRequesting || isRandom) && !isRUserResponded){
					showOfflineGameButton();
				}
			}
			
		}, isRandom ? OFFLINE_BUTTON_RANDOM_SHOW_DELAY_MILLIS : OFFLINE_BUTTON_SHOW_DELAY_MILLIS);
	}
	
	@Override
	public void onClick(View v) {
		
		if(v.getId() == R.id.activity_game_error_button || v.getId() == R.id.activity_game_final_quit_button){
			finish();
			return;
		}
		
		if(v.getId() == R.id.activity_game_final_rematch_button){
			nullify = false;
			Utils.startGame(this, rid, themeId, themeName, false, true, true);
    		return;
		}
		
		if(v.getId() == R.id.floating_button){
			nullify = false;
			Utils.startGame(this, null, themeId, themeName, true, false, true);
    		return;
		}
		
		String answer;
		if(v.getId() == R.id.activity_game_ans1_button){
			answer = "1";
		}else if(v.getId() == R.id.activity_game_ans2_button){
			answer = "2";
		}else if(v.getId() == R.id.activity_game_ans3_button){
			answer = "3";
		}else{
			answer = "4";
		}
		if(!isOfflineMode){
			client.answer(answer, countdownTimeMillis, gameState.getRound());
		}
		setButtonsClickable(false);
		gameState.setUserAnswered();
		animateIndicator(userIndicator, gameState.isAnswerRight(answer, gameState.getRound()));
		animatePics(true, gameState.isAnswerRight(answer, gameState.getRound()));
		animateButton(answer, gameState.isAnswerRight(answer, gameState.getRound()), true, true);
		
		if(gameState.hasRUserAnswered()){
			stopCountdown();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					if(!isFinished && !isNextRequested){
						isNextRequested = true;
						if(isOfflineMode){
							setOfflineNext();
						}else{
							client.next();
						}
					}
				}
			}, POST_ANSWER_DELAY_MILLIS);
		}
		
		if(gameState.isAnswerRight(answer, gameState.getRound())){
			gameState.incrScore(countdownScore);
			userScoreTextView.setText(gameState.getScore() + "");
			Utils.popAnim(userScoreTextView, this);
			animateBg(mainView, false);
		}
		
		if(isOfflineMode && !isOfflineResponding && !isRandom){
			stopCountdown();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					if(!isFinished && !isNextRequested){
						isNextRequested = true;
						setOfflineNext();
					}
				}
			}, POST_ANSWER_DELAY_MILLIS);
		}
		
		gameState.setAnsSeqAnswer(answer, countdownTimeMillis);
		
		/*if(gameState.hasRUserAnswered() || gameState.isAnswerRight(answer, gameState.getRound())){
			stopCountdown();
			if(gameState.isAnswerRight(answer, gameState.getRound())){
				gameState.incrScore(countdownScore);
				userScoreTextView.setText(gameState.getScore() + "");
				Utils.popAnim(userScoreTextView, this);
				animateBg(mainView, false);
			}
			// delay
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					if(!isFinished && !isNextRequested){
						isNextRequested = true;
						if(isOfflineMode){
							setOfflineNext();
						}else{
							client.next();
						}
					}
				}
			}, POST_ANSWER_DELAY_MILLIS);
		}
		
		if(!gameState.isAnswerRight(answer, gameState.getRound()) && isOfflineMode && !isOfflineResponding && !isRandom){
			stopCountdown();
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
					if(!isFinished && !isNextRequested){
						isNextRequested = true;
						setOfflineNext();
					}
				}
			}, POST_ANSWER_DELAY_MILLIS);
		}
		
		gameState.setAnsSeqAnswer(answer, countdownTimeMillis);*/
	}	
	
	@Override
	public void onAnswer(final String answer, final int time, final int round) {
		if(round != gameState.getRound()){
			if(gameState.isAnswerRight(answer, round)){
				gameState.incrRScore((time/1000) + 1);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						ruserScoreTextView.setText(gameState.getRScore() + "");
					}
				});
			}
			return;
		}
		
		if(countdownTimeMillis > time && !gameState.hasUserAnswered()){
			//came too early
			new Thread(new Runnable(){

				@Override
				public void run() {
					while(countdownTimeMillis > time && countdownTimeMillis < 0 && !gameState.hasUserAnswered()){
						try{
							Thread.sleep(10);
						}catch(Exception e){}
					}
					handleRUserAnswer(answer, time, round);
				}
				
			}).start();
			return;
		}
		handleRUserAnswer(answer, time, round);
	}
	
	private void handleRUserAnswer(final String answer, final int time, final int round){
		if(round != gameState.getRound()){
			if(gameState.isAnswerRight(answer, round)){
				gameState.incrRScore((time/1000) + 1);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						ruserScoreTextView.setText(gameState.getRScore() + "");
					}
				});
			}
			return;
		}
		
		stopConnectionCheck();
		gameState.setRUserAnswered();
		animateIndicator(ruserIndicator, gameState.isAnswerRight(answer, round));
		animatePics(false, gameState.isAnswerRight(answer, gameState.getRound()));
		animateButton(answer, gameState.isAnswerRight(answer, gameState.getRound()), false, gameState.hasUserAnswered());
		
		if(gameState.isAnswerRight(answer, round)){
			gameState.incrRScore((time/1000) + 1);
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					ruserScoreTextView.setText(gameState.getRScore() + "");
					Utils.popAnim(ruserScoreTextView, GameActivity.this);
					animateBg(mainView, false);
				}
			});
		}
		
		if(gameState.hasUserAnswered()){
			stopCountdown();
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					new Handler().postDelayed(new Runnable(){
						@Override
						public void run() {
							if(!isFinished && !isNextRequested){
								isNextRequested = true;
								if(isOfflineMode){
									setOfflineNext();
								}else{
									client.next();
								}
							}
						}
					}, POST_ANSWER_DELAY_MILLIS);
				}
			});
		}
		
		/*if(gameState.hasUserAnswered() || gameState.isAnswerRight(answer, round)){
			stopCountdown();
			
			if(gameState.isAnswerRight(answer, round) && !gameState.hasUserAnswered()){
				gameState.generateRandomAns(countdownTimeMillis);
			}
			
			setButtonsClickable(false);
			if(gameState.isAnswerRight(answer, round)){
				gameState.incrRScore((time/1000) + 1);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						ruserScoreTextView.setText(gameState.getRScore() + "");
						Utils.popAnim(ruserScoreTextView, GameActivity.this);
						animateBg(mainView, false);
						// delay
						new Handler().postDelayed(new Runnable(){
							@Override
							public void run() {
								if(!isFinished && !isNextRequested){
									isNextRequested = true;
									if(isOfflineMode){
										setOfflineNext();
									}else{
										client.next();
									}
								}
							}
						}, POST_ANSWER_DELAY_MILLIS);
					}
				});
				return;
			}
			
			if(gameState.hasUserAnswered()){
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						new Handler().postDelayed(new Runnable(){
							@Override
							public void run() {
								if(!isFinished && !isNextRequested){
									isNextRequested = true;
									if(isOfflineMode){
										setOfflineNext();
									}else{
										client.next();
									}
								}
							}
						}, POST_ANSWER_DELAY_MILLIS);
					}
				});
			}
		}*/
	}

	@Override
	public void onQuestions(final List<String> qIds) {
		isRUserResponded = true;
		if(isOfflineMode){
			return;
		}else{
			hideOfflineGameButton();
		}
		if(qIds == null || qIds.isEmpty()){
			handleError(3);
			return;
		}
		gameState.setQIds(qIds);
		//isRUserFound = true;
		Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				new GetQuestions(qIds).setContext(GameActivity.this).execute();
			}
		});
	}

	@Override
	public void onRandom(String rid, final List<String> qIds) {
		if(APIHandler.user_id.equals(rid)){
			handleError(3);
			return;
		}
		isRUserResponded = true;
		gameState.setQIds(qIds);
		this.rid = rid;
		client.setRId(rid);
		Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
		hideOfflineGameButton();
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Utils.executeAsyncTask(new GetRUser().setContext(GameActivity.this), null);
				Utils.executeAsyncTask(new GetQuestions(qIds).setContext(GameActivity.this), null);
			}
		});
	}

	@Override
	public void onNext() {
		if(isOfflineMode){
			return;
		}
		
		stopDotsAnimation();
		stopConnectionCheck();
		stopCountdown();
		gameState.next();
		countdownTimeMillis = 10*1000;
		if(gameState.isFinished()){
			client.finalize();
			client.close();
			setFinalInfo(GameActivity.this, false, false);
			Utils.crossfade(GameActivity.this, finalView, curView);
			return;
		}
		new Thread(new Runnable(){

			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						STATE = 1;
						if(gameState.isLastRound()){
							roundTextView.setText(getResources().getString(R.string.last_round) + "\n" + getResources().getString(R.string.bonus_x2));
						}else{
							roundTextView.setText(getResources().getString(R.string.round) + gameState.getRound());
						}
						Utils.crossfade(GameActivity.this, roundView, curView);
						curView = roundView;
					}
				});
				try{
					Thread.sleep(ROUNDVIEW_TIME_MILLIS);
				}catch(Exception e){
					
				}
				isNextRequested = false;
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(!isFinished){
							setQuestion(gameState.getQuestion(), gameState.getImage());
							Utils.crossfade(GameActivity.this, mainView, curView);
							curView = mainView;
						}
					}
				});
				
				try{
					Thread.sleep(getWaitTime(gameState.getQuestion().get("question")));
				}catch(Exception e){
					
				}
				setAnswers(gameState.getQuestion());
				
				try{
					Thread.sleep(ANSWERS_TO_START_MILLIS);
				}catch(Exception e){}
				stopCountdown();
				startCountdown();
			}
		}).start();
	}
	
	@Override
	public void onFinalize() {
		if(STATE == 1){
			setFinalInfo(GameActivity.this, false, true);
			Utils.crossfade(GameActivity.this, finalView, curView);
		}
		stopConnectionCheck();
	}
	
	@Override
	public void onError(int errorCode) {
		handleError(errorCode);
	}

	@Override
	public void onDecline(String id) {
		if(id.equals(rid) && STATE == 0){
			if(!isRequesting){
				isFinished = true;
				stopDotsAnimation();
			}
			final String error = getResources().getString(R.string.user_declined_request);
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					errorTextView.setText(error);
					if(isRequesting){
						View v = findViewById(R.id.activity_game_error_offline_view);
						Button b = (Button) v.findViewById(R.id.activity_game_error_offline_button);
						TextView tv = (TextView) v.findViewById(R.id.activity_game_error_notif_textview);
						Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
						b.setTypeface(tf);
						tv.setTypeface(tf);
						Utils.crossfade(GameActivity.this, v, null);
						b.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								Utils.crossfade(GameActivity.this, loadingView, errorView);
								curView = loadingView;
								startOfflineGameButton.performClick();
							}
						});
					}
				}
			});
			Utils.crossfade(GameActivity.this, errorView, curView);
			client.close();
			if(!isRequesting){
				STATE = 2;
			}
		}
	}
	
	@Override
	public void onRematch(String id, String themeId) {
		if(isSameUser(id, themeId)){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					showRematchDialog();
				}
			});
		}
	}
	
	public void onMessage(String id){
		if(rid.equals(id)){
			messageCounter++;
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if(messageCounter != 0 && finalChatButton != null){
						finalChatButton.setText(getResources().getString(R.string.chat) + "\n+" + messageCounter);
					}
				}
			});
		}
	}
	
	private class GetQuestions extends AsyncTask<Void, Void, Void>{

		private List<String> qIds;
		private Context context;
		private Map<String, Map<String, String>> questions;
		
		private List<String> ids;
		private Map<String, String> urls;
		
		public GetQuestions(List<String> qIds){
			this.qIds = qIds;
		}
		
		public GetQuestions setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadingTextView.setText(getResources().getString(R.string.loading_questions));
		}
		
		@Override
		protected Void doInBackground(Void... params){
			if(isOfflineMode && !isOfflineResponding && !isRandom){
				questions = APIHandler.getRandomQuestions(themeId);
			}else{
				questions = APIHandler.getQuestionsByIds(themeId, qIds);
			}
			if(questions != null){
				ids = Utils.getImageIds(questions);
				urls = Utils.getImageUrls(questions);
			}else{
				return null;
			}
			if(urls.isEmpty()){
				long curTime = System.currentTimeMillis();
				if(curTime - startTime < PROFILE_TIME_MILLIS){
					long sleepTime = PROFILE_TIME_MILLIS - (curTime - startTime);
					try{
						Thread.sleep(sleepTime);
					}catch(Exception e){}
				}
			}
			return null;
		}
		
		protected void onPostExecute(Void unused){
			if(questions != null && (isOfflineMode || questions.size() == qIds.size())){
				gameState.setQuestions(questions); 
				if(urls.isEmpty()){
					if(isOfflineMode){	
						if(!isOfflineResponding && !isRandom){
							gameState.setQIds(Utils.getQIds(questions));
						}
						setOfflineNext();
					}else{
						loadingTextView.setText(getResources().getString(R.string.waiting_for_opponent));
						new Thread(new Runnable(){
	
							@Override
							public void run() {
								client.next();
							}
							
						}).start();
					}
				}else{
					if(isOfflineMode && !isOfflineResponding && !isRandom){
						gameState.setQIds(Utils.getQIds(questions));
					}
					new GetImages(ids, urls).setContext(context).execute();
				}
			}else{
				handleError(3);
			}
		}
	}
	
	private class GetImages{
		
		private int index = 0;
		private List<String> ids;
		private Map<String, String> urls;
		private Map<String, Bitmap> images;
		private Context context;
		
		public GetImages(final List<String> ids, Map<String, String> urls){
			this.ids = ids;
			this.urls = urls;
			images = new HashMap<String, Bitmap>();
			target = new Target(){
			      
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
					images.put(ids.get(index), bitmap);
					index++;
					load();
				}
		      
				@Override
				public void onBitmapFailed(Drawable arg0) {
//					handleError(3);
					images.put(ids.get(index), null);
					index++;
					load();
				}

				@Override
				public void onPrepareLoad(Drawable arg0) {
					
				}
			};
		}
		
		private void load(){
			if(index < ids.size()){
				loadingTextView.setText(getResources().getString(R.string.loading_images) + (index + 1) + "/" + ids.size());
				Picasso.with(context).load(urls.get(ids.get(index))).into(target);
			}else{
				if(images.size() == ids.size()){
					gameState.setImages(images);
					if(isOfflineMode){
						new Thread(new Runnable(){
							@Override
							public void run() {
								long curTime = System.currentTimeMillis();
								if(curTime - startTime < PROFILE_TIME_MILLIS){
									long sleepTime = PROFILE_TIME_MILLIS - (curTime - startTime);
									try{
										Thread.sleep(sleepTime);
									}catch(Exception e){}
								}
								setOfflineNext();
							}
						}).start();
					}else{
						loadingTextView.setText(getResources().getString(R.string.waiting_for_opponent));
						new Thread(new Runnable(){
	
							@Override
							public void run() {
								long curTime = System.currentTimeMillis();
								if(curTime - startTime < PROFILE_TIME_MILLIS){
									long sleepTime = PROFILE_TIME_MILLIS - (curTime - startTime);
									try{
										Thread.sleep(sleepTime);
									}catch(Exception e){}
								}
								client.next();
							}
							
						}).start();
					}
				}else{
					handleError(3);
				}
			}
		}
		
		public GetImages setContext(Context context){
			this.context = context;
			return this;
		}
		
		public void execute(){
			load();
		}
	}
	
	private class StartGame extends AsyncTask<Void, Void, Void>{
		private boolean isReady = false;
		private Context context;
		
		public StartGame setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(!isRandom){
				loadingTextView.setText(getResources().getString(R.string.notifying_opponent));
			}else{
				loadingTextView.setText(getResources().getString(R.string.looking_for_opponent));
			}
		}
		
		@Override
		protected Void doInBackground(Void... params){
			try{
				Theme theme = ThemeDao.getInstance(GameActivity.this).getTheme(themeId);
				String qIds;
				if(theme != null){
					String range = ThemeDao.getInstance(GameActivity.this).getTheme(themeId).getRange();
					qIds = Utils.getRandomQuestionIdsFromRange(range);
				}else{
					qIds = "";
				}
				if(isRandom && qIds.length() == 0){
					isReady = false;//tried to play unpaid topic, not possible though
					return null;
				}
				isReady = client.init();
				if(isReady){
					client.sendRequest(isRandom, qIds);
				}
			}catch(Exception e){
				isReady = false;
			}
			return null;
		}
		
		protected void onPostExecute(Void unused){
			/*if(!isReady){
				handleError(3);
			}*/
		}
	}
	
	private void handleError(final int errorCode){
		// 1 - op disconnected
		// 2 - connection timeout
		// 3 - internal error
		isFinished = true;
		stopDotsAnimation();
		if(isOfflineMode){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					errorTextView.setText(getResources().getString(R.string.cant_connect_to_the_server));
				}
				
			});
			Utils.crossfade(GameActivity.this, errorView, curView);
			return;
		}
		if(STATE == 0){
			final String error;
			if(errorCode == 1){
				error = getResources().getString(R.string.opponent_disconnected);
			}else if(errorCode == 2){
				error = getResources().getString(R.string.connection_timeout);
			}else{
				error = getResources().getString(R.string.cant_connect_to_the_server);
				client.error();
			}
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					if(isRequesting && !isCancelled){
						new CancelRequest(rid).setContext(GameActivity.this).execute();
						isCancelled = true;
					}
					errorTextView.setText(error);
				}
				
			});
			Utils.crossfade(GameActivity.this, errorView, curView);
			client.close();
			STATE = 2;
			stopConnectionCheck();
			return;
		}
		
		if(STATE == 1){
			stopCountdown();
			client.finalize();
			if(errorCode == 1){
				setFinalInfo(GameActivity.this, true, false);
				Utils.crossfade(GameActivity.this, finalView, curView);
			}else if(errorCode == 2){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						errorTextView.setText(getResources().getString(R.string.connection_timeout));
					}
					
				});
				Utils.crossfade(GameActivity.this, errorView, curView);
			}else{
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						errorTextView.setText(getResources().getString(R.string.cant_connect_to_the_server));
					}
					
				});
				Utils.crossfade(GameActivity.this, errorView, curView);
			}
			client.close();
			STATE = 2;
			stopConnectionCheck();
			return;
		}
		
		if(STATE == 2){
			stopConnectionCheck();
			return;
		}

		stopConnectionCheck();
	}
	
	private void setQuestion(Map<String, String> question, Bitmap image){
		curColor = null;
		isBgAnimating = false;
		mainView.setBackgroundColor(getResources().getColor(R.color.teal));
		buttonsView.setVisibility(View.GONE);
		userRightImageView.setVisibility(View.INVISIBLE);
		userWrongImageView.setVisibility(View.INVISIBLE);
		ruserRightImageView.setVisibility(View.INVISIBLE);
		ruserWrongImageView.setVisibility(View.INVISIBLE);
		
		countdownProgressBar.setMax(0);
		countdownProgressBar.setProgress(0);
		
		countdownProgressBar.setProgress(0);
		countdownTextView.setText("10");
		String q = question.get("question");
		if(q == null || q.length() == 0 || q.equals("null")){
			questionTextView.setVisibility(View.GONE);
		}else{
			questionTextView.setText(q);
			questionTextView.setVisibility(View.VISIBLE);
		}
		if(image != null){
			questionImageView.setImageBitmap(image);
			questionImageView.setVisibility(View.VISIBLE);
			questionImageViewFrame.setVisibility(View.VISIBLE);
		}else{
			questionImageView.setVisibility(View.GONE);
			questionImageViewFrame.setVisibility(View.GONE);
		}
	}
	
	private void setAnswers(final Map<String, String> question){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				ans1Button.setText(question.get("ans1"));
				ans2Button.setText(question.get("ans2"));
				ans3Button.setText(question.get("ans3"));
				ans4Button.setText(question.get("ans4"));
				
				ans1Button.setTextColor(getResources().getColor(R.color.Gray));
				ans2Button.setTextColor(getResources().getColor(R.color.Gray));
				ans3Button.setTextColor(getResources().getColor(R.color.Gray));
				ans4Button.setTextColor(getResources().getColor(R.color.Gray));
				
				ans1Button.setPressed(false);
				ans2Button.setPressed(false);
				ans3Button.setPressed(false);
				ans4Button.setPressed(false);
				
				ans1Button.setBackgroundDrawable(getResources().getDrawable(R.drawable.answer_bg));
				ans2Button.setBackgroundDrawable(getResources().getDrawable(R.drawable.answer_bg));
				ans3Button.setBackgroundDrawable(getResources().getDrawable(R.drawable.answer_bg));
				ans4Button.setBackgroundDrawable(getResources().getDrawable(R.drawable.answer_bg));
				
				
				Utils.crossfade(GameActivity.this, buttonsView, null);
			}
			
		});
	}
	
	private void startCountdown(){
		if(isCountdownRunning){
			return;
		}
//		countdownExecutor = Executors.newSingleThreadExecutor();
//		countdownExecutor.execute(countdownRunnable);
		
		isCountdownRunning = true;
		isOfflineAnswerHandled = false;
		setButtonsClickable(true);
		time = 10*1000;
		timer = new Timer();
		timerTask = new TimerTask(){

			@Override
			public void run() {
				if (time>= 0 && isCountdownRunning && !isFinished){
					final int temp = time;
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(temp != 10*1000){
								countdownTextView.setText(temp/1000 + 1 + "");
							}
							countdownTimeMillis = temp;
							countdownScore = temp/1000 + 1;
							
							countdownProgressBar.setMax(0);
							countdownProgressBar.setProgress(0);
							
							countdownProgressBar.setMax(1000);
							countdownProgressBar.setProgress(1000 - (temp/10));
							if(temp == 3000){
								animateBg(mainView, true);
							}
							
							if(temp == 0){
								countdownTextView.setText("0");
							}
							
							
							if(isOfflineResponding || (isOfflineMode && isRandom)){
								String qId = gameState.getCurQuestionId();
								int offlineAnswerTime = gameState.getOfflineAnswerTime(qId);
								String answer = gameState.getOfflineAnswer(qId);
								int round = gameState.getRound();
								if(offlineAnswerTime != 0 && offlineAnswerTime > countdownTimeMillis && !isOfflineAnswerHandled){
									handleRUserAnswer(answer, offlineAnswerTime, round);
									isOfflineAnswerHandled = true;
								}
							}
						}
					});
					time -= 10;
				}
				if(time < 0){
					if(isCountdownRunning && !isFinished){
						//delay
						setButtonsClickable(false);
						animateButton("0", false, true, false);
						gameState.setAnsSeqAnswer("0", 0);
						runOnUiThread(new Runnable(){
							@Override
							public void run() {
								new Handler().postDelayed(new Runnable(){
									@Override
									public void run() {
										if(!isFinished && !isNextRequested){
											if(isOfflineMode){
												setOfflineNext();
											}else{
												client.next();
											}
											isNextRequested = true;
										}
									}
								}, POST_ANSWER_DELAY_MILLIS);
							}
						});
					}
					stopCountdown();
				}
				if(!isCountdownRunning){
					if(!isOfflineMode && !isFinished){
						startConnectionCheck();
					}
				}
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 10);
	}
	
	private void stopCountdown(){
//		if(countdownExecutor != null){
//			countdownExecutor.shutdownNow();
//		}

		isCountdownRunning = false;
		if(timerTask != null){
			timerTask.cancel();
		}
	}

	
	/*private class CountdownRunnable implements Runnable{
		
		@Override
		public void run() {
			isCountdownRunning = true;
			isOfflineAnswerHandled = false;
			setButtonsClickable(true);
			int time = 10*1000;

			while(time>= 0 && isCountdownRunning && !isFinished){
				final int temp = time;
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						
						if(temp != 10*1000){
							countdownTextView.setText(temp/1000 + 1 + "");
						}
						countdownTimeMillis = temp;
						countdownScore = temp/1000 + 1;
						countdownProgressBar.setProgress(1000 - (temp/10));
						
						if(temp == 3000){
							animateBg(mainView, true);
						}
						
						if(temp == 0){
							countdownTextView.setText("0");
						}
						
						
						if(isOfflineResponding || (isOfflineMode && isRandom)){
							String qId = gameState.getCurQuestionId();
							int offlineAnswerTime = gameState.getOfflineAnswerTime(qId);
							String answer = gameState.getOfflineAnswer(qId);
							int round = gameState.getRound();
							if(offlineAnswerTime != 0 && offlineAnswerTime > countdownTimeMillis && !isOfflineAnswerHandled){
								handleRUserAnswer(answer, offlineAnswerTime, round);
								isOfflineAnswerHandled = true;
							}
						}
					}
				});
				time -= 10;
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					isCountdownRunning = false;
					Thread.currentThread().interrupt();
				}
			}
			if(isCountdownRunning && !isFinished){
				//delay
				setButtonsClickable(false);
				animateButton("0", false, true);
				gameState.setAnsSeqAnswer("0", 0);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						new Handler().postDelayed(new Runnable(){
							@Override
							public void run() {
								if(!isFinished && !isNextRequested){
									if(isOfflineMode){
										setOfflineNext();
									}else{
										client.next();
									}
									isNextRequested = true;
								}
							}
						}, POST_ANSWER_DELAY_MILLIS);
					}
				});
			}
			isCountdownRunning = false;
			if(!isOfflineMode && !isFinished){
				startConnectionCheck();
			}
		}
	}*/	
	
	private void setButtonsClickable(final boolean isClickable){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ans1Button.setClickable(isClickable);
				ans2Button.setClickable(isClickable);
				ans3Button.setClickable(isClickable);
				ans4Button.setClickable(isClickable);
				
				ans1Button.setPressed(false);
				ans2Button.setPressed(false);
				ans3Button.setPressed(false);
				ans4Button.setPressed(false);
	
			}
		});
	}
	
	private void animatePics(boolean isUser, boolean isRight){
		if(isUser){
			if(isRight){
				Utils.crossfade(GameActivity.this, userRightImageView, null);
			}else{
				Utils.crossfade(GameActivity.this, userWrongImageView, null);
			}
		}else{
			if(isRight){
				Utils.crossfade(GameActivity.this, ruserRightImageView, null);
			}else{
				Utils.crossfade(GameActivity.this, ruserWrongImageView, null);
			}
		}
	}
	
	private void animateIndicator(final View indicator, final boolean isRight){
		
		runOnUiThread(new Runnable(){

			@Override
			public void run() {

				int duration = 300;
				if(isRight){
					indicator.setBackgroundDrawable(getResources().getDrawable(R.drawable.teal_circle));
				}else{
					indicator.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_circle));
				}
				Utils.crossfade(GameActivity.this, indicator, null);
				Animation scaleUp = new ScaleAnimation(1, 500f, 1, 500f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleUp.setDuration(duration);
			    scaleUp.setFillAfter(true);
			    final Animation scaleDown = new ScaleAnimation(500f, 1, 500f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleDown.setDuration(duration);
			    scaleDown.setFillAfter(true);
			    indicator.startAnimation(scaleUp);
			    indicator.postDelayed(new Runnable(){

					@Override
					public void run() {
						if(indicator != null){
							indicator.startAnimation(scaleDown);
						}
					}
			    	
			    }, duration);
			    
			    new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						Utils.crossfade(GameActivity.this, loadingNotificationTextView, null);
					}
			    	
			    }, 2*duration);
			}
		});
	}
	
	private class GetRUser extends AsyncTask<Void, Void, Void>{
		
		private Context context;
		
		public GetRUser setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			ruser = APIHandler.getRUser(rid, themeId);
			return null;
		}
		
		protected void onPostExecute(Void unused){
			startTime = System.currentTimeMillis();
			if(ruser != null){
				ruserNameTextView.setText(ruser.get("fullname"));
				ruserLevelTextView.setText(getResources().getString(R.string.level) + Utils.getLevel(ruser.get("score")));
				loadingUserNameTextView.setText(ruser.get("fullname"));
				loadingLevelTextView.setText(getResources().getString(R.string.level) + Utils.getLevel(ruser.get("score")));
				
				finalChatButton.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {

						if(ruser == null || finalChatButton == null){
							return;
						}
						Intent intent = new Intent(GameActivity.this, ChatActivity.class);
						intent.putExtra("ruserId", rid);
						intent.putExtra("name", ruser.get("fullname"));
						intent.putExtra("thumbnail_img_url", ruser.get("thumbnail_img_url"));
						messageCounter = 0;
						finalChatButton.setText(getResources().getString(R.string.chat));
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					}
					
				});
				if(ruser.get("profile_img_url").length() != 0){
					Picasso.with(context).load(ruser.get("profile_img_url")).into(ruserFinalImageView);
					Picasso.with(context).load(ruser.get("profile_img_url")).into(ruserImageView);
					Picasso.with(context).load(ruser.get("profile_img_url")).into(loadingImageView);
				}
				
				Utils.crossfade(context, loadingUserNameTextView, null);
				Utils.crossfade(context, loadingLevelTextView, null);
				Utils.crossfade(context, loadingImageView, null);
				
				if(isOfflineResponding || (isRandom && isOfflineMode)){
					final List<String> qIds = Utils.getQIdsFromSeq(rAnsSeq);
					gameState.setQIds(qIds);
					gameState.setOfflineAnsSeq(rAnsSeq);
					//isRUserFound = true;
					Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
					if(!isRandom){
						hideOfflineGameButton();
					}
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							new GetQuestions(qIds).setContext(GameActivity.this).execute();
						}
					});
				}
			}else{
				handleError(3);
			}
		}
	}
	
	private class Finalize extends AsyncTask<Void, Void, Void>{

		private Context context;
		private Map<String, String> res;
		private boolean isOfflineMode;
		private boolean surrendered;
		private boolean disconnected;
		private boolean saveDuels;
		private String ansSeq;
		
		public Finalize(boolean isOfflineMode, boolean surrendered, boolean disconnected, boolean saveDuels, String ansSeq){
			this.isOfflineMode = isOfflineMode;
			this.surrendered = surrendered;
			this.disconnected = disconnected;
			this.saveDuels = saveDuels;
			this.ansSeq = ansSeq;
		}
		
		public Finalize setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			boolean hasWon = gameState.getScore() >= gameState.getRScore();
			if(gameState.getScore() <= gameState.getRScore() && !isRandomRecord){
				ansSeq = "";
			}
			res = APIHandler.finalize(
					rid, 
					themeId, 
					themeName, 
					Utils.getFinalScore(context, gameState.getScore(), hasWon, surrendered, disconnected) + "", 
					hasWon,
					isOfflineMode,
					gameState.getRScore() + "",
					saveDuels,
					ansSeq);
			ThemeDao.getInstance(context).updateFavorites(themeId);
			if(notSaveResultInDB || (isOfflineMode && !isOfflineResponding) || surrendered || disconnected){
				return null;
			}
			String message;
			if(gameState.getRScore() == gameState.getScore()){
				message = getResources().getString(R.string.draw_in_theme) + themeName;
			}else if(gameState.getScore() > gameState.getRScore()){
				message = getResources().getString(R.string.you_won_in_theme) + themeName;
			}else{
				message = getResources().getString(R.string.you_lost_in_theme) + themeName;
			}
			Map<String, String> messageMap = new HashMap<String, String>();
    		messageMap.put("ruser_id", rid);
			messageMap.put("name", ruser.get("fullname"));
    		messageMap.put("message", message);
    		messageMap.put("thumbnail_img_url", ruser.get("thumbnail_img_url"));
    		messageMap.put("type", "2");
    		messageMap.put("timestamp", System.currentTimeMillis() + "");
    		
    		final Message msg = new Message(messageMap);
    		
    		MessageDao.getInstance(context).insertMessage(msg);
			
			return null;
			
		}
		
		protected void onPostExecute(Void unused){
			if(res != null){
				finalTotalValueTextView.setText(res.get("score"));
				String level = Utils.getLevel(res.get("score")) + "";
				finalLevelValueTextView.setText(level);
				finalStatusValueTextView.setText(res.get("status"));
				Editor editor = context.getSharedPreferences("qz_pref", Context.MODE_PRIVATE).edit()
					.putString("qz_level_" + themeId, level)
					.putString("qz_status", res.get("status"));
				if(res.containsKey("best_in") && res.get("best_in").trim().length() != 0 && !res.get("best_in").equals("null")){
					editor.putString("qz_best_in", res.get("best_in"));
				}else{
					editor.putString("qz_best_in", "");
				}
				String best_in = res.get("best_in");
				isBest = themeName.equals(best_in);
				editor.commit();
			}else{
				Toast.makeText(context, getResources().getString(R.string.cant_save_results), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private class GetRandomRecord extends AsyncTask<Void, Void, Void>{
		private Context context;
		private Map<String, String> res;
		
		public GetRandomRecord(){
		}
		
		public GetRandomRecord setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			res = APIHandler.getRandomRecord(themeId);
			return null;
		}
		
		protected void onPostExecute(Void unused){
			if(isRUserResponded){
				return;
			}
			if(res != null){
				rid = res.get("id");
				rAnsSeq = res.get("ans_seq");
				isRandomRecord = "1".equals(res.get("is_random"));
				isBot = "1".equals(res.get("is_bot"));
				GCMIntentService.gameActivity = null;
				//close channel
				client.close();
				isOfflineMode = true;
				notSaveResultInDB = true;
				//isRUserFound = true;
				new GetRUser().setContext(context).execute();
			}else{
				loadingNotificationTextView.setText(context.getResources().getString(R.string.record_not_found));
				Utils.crossfade(context, loadingNotificationTextView, null);
			}
		}
	}
	
	private void setFinalInfo(final Context context, final boolean disconnected, final boolean surrendered){
		if(context == null){
			return;
		}
		stopCountdown();
		stopConnectionCheck();
		GCMIntentService.STATE = 1;
		STATE = 2;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				
				if(ruser != null){
					ruserFinalNameTextView.setText(ruser.get("fullname"));
				}
				String userName = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_name", "");
				userFinalNameTextView.setText(userName);
				userFinalScoreTextView.setText(gameState.getScore() + "");
				ruserFinalScoreTextView.setText(gameState.getRScore() + "");
				String finalResult;
				
				if(isOfflineMode && !isOfflineResponding && !isRandom){
					finalResult = context.getResources().getString(R.string.opp_turn);
					finalResultTextView.setText(finalResult);
					animateFinal();
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							String ansSeq = gameState.getAnsSeq();
							new NotifyOffline(rid, themeId, themeName, ansSeq).setContext(GameActivity.this).execute();
						}
					});
					return;
				}
				
				if(disconnected){
					finalResult = getResources().getString(R.string.opponent_disconnected);
				}else if(surrendered){
					finalResult = getResources().getString(R.string.opponent_surrendered);
				}else{
					if(gameState.getScore() > gameState.getRScore()){
						finalResult = getResources().getString(R.string.you_won);
					}else if(gameState.getScore() < gameState.getRScore()){
						finalResult = getResources().getString(R.string.you_lost);
					}else{
						finalResult = getResources().getString(R.string.draw);
					}
				}
				finalResultTextView.setText(finalResult);
				finalScoreValueTextView.setText(gameState.getScore() + "");
				String winBonus;
				if(disconnected || surrendered){
					winBonus = "0";
				}else{
					winBonus = ((gameState.getScore() >= gameState.getRScore()) ? "100" : "0") + "";
				}
				finalWinbonusValueTextView.setText(winBonus);
				finalMultValueTextView.setText("x" + Utils.getBoosterValue(GameActivity.this));
				String best_in = getSharedPreferences("qz_pref", MODE_PRIVATE).getString("qz_best_in", "");
				if(best_in.equals(themeName)){
					isBest = true;
				}
				animateFinal();
			}
		});
		
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(isBot){
					new Finalize(true, false, false, true, gameState.getAnsSeq()).setContext(GameActivity.this).execute();
					return;
				}
				
				if(isOfflineMode && !isOfflineResponding && !isRandom){
					return;
				}
				
				String ansSeq = gameState.getAnsSeq();
				if(isOfflineMode && !isRandomRecord){
					ansSeq = "";
				}
				new Finalize(isOfflineMode && !isRandom, disconnected, surrendered, !(isRandom && isOfflineMode), ansSeq).setContext(GameActivity.this).execute();
			}
		});
	}

	private void showSurrenderDialog(){
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_game_dialog);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		
		TextView title = (TextView) dialog.findViewById(R.id.game_dialog_textview);
		Button ok = (Button) dialog.findViewById(R.id.game_dialog_ok_button);
		Button cancel = (Button) dialog.findViewById(R.id.game_dialog_cancel_button);
		
		title.setTypeface(tf);
		ok.setTypeface(tf);
		cancel.setTypeface(tf);
		
		if(isBadConnection || isOfflineMode){
		    title.setText(getResources().getString(R.string.quit));
		}else{
		    title.setText(getResources().getString(R.string.surrender));
		}
		ok.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				stopCountdown();
				if(isOfflineMode){
					GCMIntentService.STATE = 0;
					GCMIntentService.gameActivity = null;
		    		finish();
		    		return;
				}
	    		if(!isBadConnection){
	    			client.finalize();
	    		}
	    		client.close();
				GCMIntentService.STATE = 0;
				GCMIntentService.gameActivity = null;
	    		finish();
				dialog.dismiss();
			}
			
		});
		
		cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
			
		});
		
		dialog.show();
	}
	
	private void showRematchDialog(){
		if(isOfflineMode){
			return;
		}
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_game_dialog);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		
		TextView title = (TextView) dialog.findViewById(R.id.game_dialog_textview);
		Button ok = (Button) dialog.findViewById(R.id.game_dialog_ok_button);
		Button cancel = (Button) dialog.findViewById(R.id.game_dialog_cancel_button);
		
		title.setTypeface(tf);
		ok.setTypeface(tf);
		cancel.setTypeface(tf);
		
		title.setText(ruser.get("fullname") + getResources().getString(R.string.wants_a_rematch));
		    
		ok.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				rematchNotify = false;
				nullify = false;
				Utils.startGame(GameActivity.this, rid, themeId, themeName, false, false, true);
				dialog.dismiss();
			}
			
		});
		
		cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				rematchNotify = true;
				dialog.dismiss();
			}
			
		});
		dialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface dialog) {
				new DeclineRequest(rematchNotify).setContext(GameActivity.this).execute();
			}
			
		});
		dialog.show();
	}
	
	private boolean isSameUser(String userRId, String userThemeId){
		return userRId.equals(rid) && userThemeId.equals(themeId);
	}
	
	@Override
	public void onBackPressed() {
		if(isOfflineMode){
			showSurrenderDialog();
			return;
		}
		
		if(STATE == 0){
			GCMIntentService.STATE = 0;
			GCMIntentService.gameActivity = null;
			if(isRequesting && !isCancelled){
				new CancelRequest(rid).setContext(this).execute();
				isCancelled = true;
			}
			if(client != null){
				client.close();
			}
			finish();
			return;
		}
		
		if(STATE == 1){
			showSurrenderDialog();
			return;
		}
		
		if(STATE == 2){
			GCMIntentService.STATE = 0;
			GCMIntentService.gameActivity = null;
			finish();
			return;
		}
	}
	
	@Override
	public void onDestroy(){
		isFinished = true;
		stopDotsAnimation();
		if(nullify){
			GCMIntentService.STATE = 0;
			GCMIntentService.gameActivity = null;
		}
		stopDotsAnimation();
		if(client != null){
			client.close();
		}

		if(isRequesting && STATE == 0 && !isCancelled){
			new CancelRequest(rid).setContext(this).execute();
			isCancelled = true;
		}
		super.onDestroy();
	}
	
	private void animateButton(final String answer, final boolean isRight, final boolean isUser, final boolean showRight){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {

				TextView button;
				if(answer.equals("1")){
					button = ans1Button;
				}else if(answer.equals("2")){
					button = ans2Button;
				}else if(answer.equals("3")){
					button = ans3Button;
				}else{
					button = ans4Button;
				}
				
				if(isRight && showRight){
					button.setBackgroundColor(getResources().getColor(R.color.light_teal));
					button.setTextColor(getResources().getColor(R.color.White));
				}else{
					if(isUser){
						buttonsView.animate().alpha(0.5f).setDuration(300);
					}
				}
			}
		});
	}
	
	private void animateBg(final View view,final boolean toWrong){
		Integer colorRight = getResources().getColor(R.color.teal);
		Integer colorWrong = getResources().getColor(R.color.dark_red);
		if(toWrong){
			ValueAnimator toWrongAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorRight, colorWrong);
			toWrongAnimation.addUpdateListener(new AnimatorUpdateListener() {

			    @Override
			    public void onAnimationUpdate(ValueAnimator animator) {
			    	curColor = (Integer)animator.getAnimatedValue();
			    	if(isBgAnimating){
			    		view.setBackgroundColor((Integer)animator.getAnimatedValue());
			    	}
			    }
			});
			toWrongAnimation.setDuration(3000);
			
			isBgAnimating = true;
			toWrongAnimation.start();
		}else{
			if(curColor == null){
				return;
			}
			ValueAnimator toRightAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curColor, colorRight);
			toRightAnimation.addUpdateListener(new AnimatorUpdateListener() {

			    @Override
			    public void onAnimationUpdate(ValueAnimator animator) {
			    	view.setBackgroundColor((Integer)animator.getAnimatedValue());
			    }

			});
			toRightAnimation.setDuration(500);
			curColor = colorRight;
			isBgAnimating = false;
			toRightAnimation.start();
		}
	}
	
	private void animateImgs(ImageView img1, ImageView img2){
		int amountToMoveRight = 170;
		int amountToMoveLeft = 170;
		final int amountToMoveDown = 220;
		int duration = 700;
		final int inititalHeight = img1.getHeight();
		
		img1.animate()
			.scaleXBy(1f)
			.scaleYBy(1f)
			.translationXBy(amountToMoveRight)
			.translationYBy(amountToMoveDown)
			.setDuration(duration);

		img2.animate()
			.scaleXBy(1f)
			.scaleYBy(1f)
			.translationXBy(-amountToMoveLeft)
			.translationYBy(amountToMoveDown)
			.setDuration(duration).setListener(
				new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						View imagesView = findViewById(R.id.activity_game_final_images_view);
						imagesView.getLayoutParams().height = 2*inititalHeight + amountToMoveDown;
						imagesView.requestLayout();
					}
				}
			);

	}
	
	private void animateFinal(){
		animateImgs(userFinalImageView, ruserFinalImageView);
		Handler handler = new Handler();
		long delay = 1000;
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.crossfade(GameActivity.this, finalResultTextView, null);
			}
			
		}, delay);
		delay += 500;
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.crossfade(GameActivity.this, userFinalNameTextView, null);
				Utils.crossfade(GameActivity.this, ruserFinalNameTextView, null);
			}
			
		}, delay);
		delay += 300;
		
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.crossfade(GameActivity.this, userFinalScoreTextView, null);
				Utils.crossfade(GameActivity.this, ruserFinalScoreTextView, null);
				Utils.popAnim(userFinalScoreTextView, GameActivity.this);
				Utils.popAnim(ruserFinalScoreTextView, GameActivity.this);
			}
			
		}, delay);
		delay += 200;
		
		if(isOfflineMode && !isOfflineResponding && !isRandom){
			handler.postDelayed(new Runnable(){
				
				@Override
				public void run() {
					finalOfflineTextView.setText(getResources().getString(R.string.offline_explain));
					Utils.crossfade(GameActivity.this, finalOfflineTextView, null);
				}
				
			}, delay);
			
			delay += 300;
			
			handler.postDelayed(new Runnable(){
				
				@Override
				public void run() {
					finalRematchButton.setVisibility(View.GONE);
					Utils.crossfade(GameActivity.this, finalQuitButton, null);
					finalChatButton.setVisibility(View.VISIBLE);
					showNextButton();
				}
				
			}, delay);
			
			
			return;
		}
		
		final List<View> list1 = new ArrayList<View>();
		final List<View> list2 = new ArrayList<View>();
		list1.add(finalScoreTextView);
		list1.add(finalWinbonusTextView);
		list1.add(finalMultTextView);
		list1.add(finalTotalTextView);
		list1.add(finalLevelTextView);
		list1.add(finalStatusTextView);

		list2.add(finalScoreValueTextView);
		list2.add(finalWinbonusValueTextView);
		list2.add(finalMultValueTextView);
		list2.add(finalTotalValueTextView);
		list2.add(finalLevelValueTextView);
		list2.add(finalStatusValueTextView);
		
		for(int i = 0; i < list1.size(); i++){
			delay += 300;
			final int pos = i;
			handler.postDelayed(new Runnable(){

				@Override
				public void run() {

					Utils.crossfade(GameActivity.this, list1.get(pos), null);
					Utils.crossfade(GameActivity.this, list2.get(pos), null);
					Utils.popAnim(list2.get(pos), GameActivity.this);
				}
				
			}, delay);

		}
		
		handler.postDelayed(new Runnable(){

			@Override
			public void run() {
				Utils.crossfade(GameActivity.this, finalRematchButton, null);
				Utils.crossfade(GameActivity.this, finalQuitButton, null);
				if(isBest){
					Toast.makeText(GameActivity.this, getResources().getString(R.string.you_are_best_in), Toast.LENGTH_SHORT).show();
				}
				if(isRandom){
					finalNextButton.setVisibility(View.VISIBLE);
				}
				finalChatButton.setVisibility(View.VISIBLE);
				showNextButton();
				
			}
			
		}, delay);
	}
	
	private void animateDots(){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				TextView dot1 = (TextView) findViewById(R.id.dot1);
				TextView dot2 = (TextView) findViewById(R.id.dot2);
				TextView dot3 = (TextView) findViewById(R.id.dot3);
				TextView dot4 = (TextView) findViewById(R.id.dot4);
				TextView dot5 = (TextView) findViewById(R.id.dot5);

				Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
				
				dot1.setTypeface(tf);
				dot2.setTypeface(tf);
				dot3.setTypeface(tf);
				dot4.setTypeface(tf);
				dot5.setTypeface(tf);
				
				final List<View> dots = new ArrayList<View>();
				dots.add(dot1);
				dots.add(dot2);
				dots.add(dot3);
				dots.add(dot4);
				dots.add(dot5);
				dotAnimationThread = new Thread(new Runnable(){

					@Override
					public void run() {
						int index = 0;
						while(isDotAnimating){
							Utils.popAnim(dots.get(index), GameActivity.this);
							index++;
							if(index == dots.size()){
								index = 0;
								try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									isDotAnimating = false;
									if(dotAnimationThread != null){
										dotAnimationThread.interrupt();
									}
								}
							}
							try {
								Thread.sleep(150);
							} catch (InterruptedException e) {
								isDotAnimating = false;
								if(dotAnimationThread != null){
									dotAnimationThread.interrupt();
								}
							}
						}
					}
					
				});
				dotAnimationThread.start();
			}
		});

	}
	
	private void stopDotsAnimation(){
		isDotAnimating = false;
		if(dotAnimationThread != null){
			dotAnimationThread.interrupt();
			dotAnimationThread = null;
		}
	}
	
	private void startConnectionCheck(){
		if(STATE == 2){
			return;
		}
		checkConnection = true;
		connectionCheckThread = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					Thread.sleep(CONNECTION_CHECK_TIME_MILLIS);
				} catch (InterruptedException e) {
					checkConnection = false;
					isBadConnection = false;
					if(connectionCheckThread != null){
						connectionCheckThread.interrupt();
					}
				}
				if(checkConnection){
					if(STATE == 2){
						return;
					}
					showBadConnectionWarning();
				}
			}
		});
		connectionCheckThread.start();
	}
	
	private void stopConnectionCheck(){
		checkConnection = false;
		isBadConnection = false;
		if(connectionCheckThread != null){
			connectionCheckThread.interrupt();
		}
		hideBadConnectionWarning();
	}
	
	private void showBadConnectionWarning(){
		isBadConnection = true;
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				TextView warning = (TextView) findViewById(R.id.activity_game_main_connection_warning_textview);
				View warningView = findViewById(R.id.activity_game_main_connection_warning_view);
				Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
				warning.setTypeface(tf);
				Utils.crossfade(GameActivity.this, warningView, null);
			}
		});
	}
	
	private void hideBadConnectionWarning(){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				View warningView = findViewById(R.id.activity_game_main_connection_warning_view);
				Utils.crossfade(GameActivity.this, null, warningView);
			}
		});
	}
	
	
		
	private void hideNextButton(){
		View nextButtonView = findViewById(R.id.floating_view);
		ObjectAnimator hideAnimator = ObjectAnimator.ofFloat(nextButtonView, "translationY", -300);
		hideAnimator.start();
	}
	
	private void showNextButton(){
		View nextButtonView = findViewById(R.id.floating_view);
		ObjectAnimator showAnimator = ObjectAnimator.ofFloat(nextButtonView, "translationY", 0);
		showAnimator.start();		
	}
	
	private void startOfflineGame(){
		
		if(isRandom){
			new GetRandomRecord().setContext(this).execute();
			return;
		}
		
		isOfflineMode = true;
		GCMIntentService.gameActivity = null;
		//cancel request
		if(isRequesting && !isCancelled){
			new CancelRequest(rid).setContext(this).execute();
			isCancelled = true;
		}
		//close channel
		client.close();
	    //isRUserFound = true;
		new GetQuestions(null).setContext(this).execute();
	}
	
	private void setOfflineNext(){
		stopDotsAnimation();
		stopConnectionCheck();
		stopCountdown();
		gameState.next();
		countdownTimeMillis = 10*1000;
		if(gameState.isFinished()){
			setFinalInfo(GameActivity.this, false, false);
			Utils.crossfade(GameActivity.this, finalView, curView);
			return;
		}
		new Thread(new Runnable(){

			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						STATE = 1;
						if(gameState.isLastRound()){
							roundTextView.setText(getResources().getString(R.string.last_round) + "\n" + getResources().getString(R.string.bonus_x2));
						}else{
							roundTextView.setText(getResources().getString(R.string.round) + gameState.getRound());
						}
						Utils.crossfade(GameActivity.this, roundView, curView);
						curView = roundView;
					}
				});
				try{
					Thread.sleep(ROUNDVIEW_TIME_MILLIS);
				}catch(Exception e){
					
				}
				isNextRequested = false;
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(!isFinished){
							setQuestion(gameState.getQuestion(), gameState.getImage());
							Utils.crossfade(GameActivity.this, mainView, curView);
							curView = mainView;
						}
					}
				});
				
				try{
					Thread.sleep(getWaitTime(gameState.getQuestion().get("question")));
				}catch(Exception e){
					
				}
				setAnswers(gameState.getQuestion());
				
				try{
					Thread.sleep(ANSWERS_TO_START_MILLIS);
				}catch(Exception e){}
				stopCountdown();
				startCountdown();
			}
		}).start();
	}
	
	private void showOfflineGameButton(){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(isRUserResponded || startOfflineGameButton == null){
					return;
				}
				startOfflineGameButton.setClickable(true);
				Utils.crossfade(GameActivity.this, startOfflineGameButton, null);
				Animation scaleUp = new ScaleAnimation(1, 2f, 1, 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleUp.setDuration(300);
			    scaleUp.setFillAfter(true);
			    final Animation scaleDown = new ScaleAnimation(2f, 1, 2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleDown.setDuration(300);
			    scaleDown.setFillAfter(true);
			    startOfflineGameButton.startAnimation(scaleUp);
			    startOfflineGameButton.postDelayed(new Runnable(){

					@Override
					public void run() {
						if(startOfflineGameButton != null && !isRUserResponded){
							startOfflineGameButton.startAnimation(scaleDown);
						}
					}
			    	
			    }, 300);
			    
			    new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						if(isRUserResponded || isOfflineMode){
							return;
						}
						String text = isRandom ? getResources().getString(R.string.offline_loading_random_explain) : getResources().getString(R.string.offline_loading_explain);
						if(loadingNotificationTextView != null){
							loadingNotificationTextView.setText(text);
						}
						Utils.crossfade(GameActivity.this, loadingNotificationTextView, null);
					}
			    	
			    }, 700);
			}
		});
	}
	
	private void expandOfflineGameButton(){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(startOfflineGameButton == null || loadingNotificationTextView == null){
					return;
				}
				isOfflineGameButtonExpanded = true;
				startOfflineGameButton.setClickable(false);
				Animation scaleUp = new ScaleAnimation(1, 100f, 1, 100f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleUp.setDuration(2000);
			    scaleUp.setFillAfter(true);
			    startOfflineGameButton.startAnimation(scaleUp);
			    startOfflineGameButton.setText("");
			    loadingNotificationTextView.setText("");
			    Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
			}
		});
	}
	
	private void hideOfflineGameButton(){
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				
				if(startOfflineGameButton == null || loadingNotificationTextView == null){
					return;
				}
				startOfflineGameButton.setClickable(false);
				if(isOfflineGameButtonExpanded){
					Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
					Animation scaleDown = new ScaleAnimation(100f, 0.1f, 100f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				    scaleDown.setDuration(2000);
				    scaleDown.setFillAfter(true);
				    startOfflineGameButton.startAnimation(scaleDown);
				    new Handler().postDelayed(new Runnable(){

						@Override
						public void run() {
							Utils.crossfade(GameActivity.this, null, startOfflineGameButton);
						}
				    	
				    }, 2000);
				    return;
				}
				
				Animation scaleUp = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleUp.setDuration(300);
			    scaleUp.setFillAfter(true);
			    final Animation scaleDown = new ScaleAnimation(1.5f, 0.1f, 1.5f, 0.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			    scaleDown.setDuration(300);
			    scaleDown.setFillAfter(true);
			    startOfflineGameButton.startAnimation(scaleUp);
			    startOfflineGameButton.postDelayed(new Runnable(){

					@Override
					public void run() {
						if(startOfflineGameButton != null){
							startOfflineGameButton.startAnimation(scaleDown);
						}
					}
			    	
			    }, 300);
			    
			    new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						Utils.crossfade(GameActivity.this, null, loadingNotificationTextView);
						Utils.crossfade(GameActivity.this, null, startOfflineGameButton);
					}
			    	
			    }, 600);
			}
		});
	}
	
	private int getWaitTime(String s){
		if(s == null || s.length() <= 50){
			return 1000;
		}else if(s.length() <= 100){
			return 1500;
		}else{
			return 2000;
		}
	}
}
