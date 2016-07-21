package ru.anov.qzproject.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.anov.qzproject.R;
import ru.anov.qzproject.models.Theme;
import ru.anov.qzproject.models.ThemeDao;
import ru.anov.qzproject.utils.APIHandler;
import ru.anov.qzproject.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import eu.janmuller.android.simplecropimage.CropImage;

public class SubmitQuestionActivity extends ActionBarActivity implements OnClickListener{

	private List<Theme> themelist;
	private List<String> titles;
	private Typeface tf;
	private Typeface typeface;
	
	private View mainView;
	private ProgressBar progressBar;
	
	private TextView title1TextView;
	private TextView title2TextView;
	private TextView title3TextView;
	private TextView title4TextView;
	
	private TextView explain1TextView;
	private TextView explain2TextView;
	private TextView explain3TextView;
	private TextView explain4TextView;
	
	private AutoCompleteTextView acTextView;
	
	private View addImage;
	private ImageView imageView;
	
	private EditText question;
	private EditText ans1;
	private EditText ans2;
	private EditText ans3;
	private EditText ans4;
	private CheckBox cb1;
	private CheckBox cb2;
	private CheckBox cb3;
	private CheckBox cb4;
	private Button submit;
	
	private CheckBox checked;
	
    private File tempFile;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_CROP_IMAGE = 2;
	private static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
	
	private Bitmap bitmap;
	
	private String upThemeId = null;
	private String upThemeName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_submit_question);

		if(savedInstanceState != null){
			return;
		}
		
		Intent i = getIntent();
		if(i.hasExtra("up_theme_id") && i.hasExtra("up_theme_name")){
			upThemeId = i.getExtras().getString("up_theme_id");
			upThemeName = i.getExtras().getString("up_theme_name");
		}
		getActionBar().setDisplayHomeAsUpEnabled(true);
		tf = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		typeface = Typeface.createFromAsset(getAssets(), "Roboto-Italic.ttf");
		mainView = findViewById(R.id.activity_submit_main_view);
		progressBar = (ProgressBar) findViewById(R.id.activity_submit_progressbar);
        acTextView = (AutoCompleteTextView) findViewById(R.id.activity_submit_ac_textview);
        acTextView.setThreshold(0);
        acTextView.setTypeface(tf);
        
        title1TextView = (TextView) findViewById(R.id.activity_submit_title1_textview);
        title2TextView = (TextView) findViewById(R.id.activity_submit_title2_textview);
        title3TextView = (TextView) findViewById(R.id.activity_submit_title3_textview);
        title3TextView = (TextView) findViewById(R.id.activity_submit_title3_textview);
        title4TextView = (TextView) findViewById(R.id.activity_submit_title4_textview);
        title1TextView.setTypeface(typeface);
        title2TextView.setTypeface(typeface);
        title3TextView.setTypeface(typeface);
        title4TextView.setTypeface(typeface);
        
        explain1TextView = (TextView) findViewById(R.id.activity_submit_explain1_textview);
        explain2TextView = (TextView) findViewById(R.id.activity_submit_explain2_textview);
        explain3TextView = (TextView) findViewById(R.id.activity_submit_explain3_textview);
        explain4TextView = (TextView) findViewById(R.id.activity_submit_explain4_textview);
        explain1TextView.setTypeface(tf);
        explain2TextView.setTypeface(tf);
        explain3TextView.setTypeface(tf);
        explain4TextView.setTypeface(tf);
        
        question = (EditText) findViewById(R.id.activity_submit_question_editext);
        ans1 = (EditText) findViewById(R.id.activity_submit_ans1_editext);
        ans2 = (EditText) findViewById(R.id.activity_submit_ans2_editext);
        ans3 = (EditText) findViewById(R.id.activity_submit_ans3_editext);
        ans4 = (EditText) findViewById(R.id.activity_submit_ans4_editext);
        
        question.setTypeface(tf);
        ans1.setTypeface(tf);
        ans2.setTypeface(tf);
        ans3.setTypeface(tf);
        ans4.setTypeface(tf);
        
        submit = (Button) findViewById(R.id.activity_submit_button);
        submit.setTypeface(tf);
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String s = isValid();
				if(s == null){
					showConfirmDialog(SubmitQuestionActivity.this);
				}else{
					Toast.makeText(SubmitQuestionActivity.this, s, Toast.LENGTH_LONG).show();
				}
			}
        	
        });
        
        cb1 = (CheckBox) findViewById(R.id.activity_submit_cb1);
        cb2 = (CheckBox) findViewById(R.id.activity_submit_cb2);
        cb3 = (CheckBox) findViewById(R.id.activity_submit_cb3);
        cb4 = (CheckBox) findViewById(R.id.activity_submit_cb4);
        
        cb1.setOnClickListener(this);
        cb2.setOnClickListener(this);
        cb3.setOnClickListener(this);
        cb4.setOnClickListener(this);
        
        addImage = findViewById(R.id.activity_submit_imageview_button);
        addImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String state = Environment.getExternalStorageState();
		    	if (Environment.MEDIA_MOUNTED.equals(state)) {
		    		tempFile = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
		    	}else{
		    		tempFile = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
		    	}
				
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		        photoPickerIntent.setType("image/*");
		        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
			}
        });
        imageView = (ImageView) findViewById(R.id.activity_submit_imageview);
        if(upThemeName != null){
        	acTextView.setText(upThemeName);
        	question.requestFocus();
        }
        new GetThemes().setContext(this).execute();
	}
	

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.activity_submit_cb1 || 
				id == R.id.activity_submit_cb2 || 
				id == R.id.activity_submit_cb3 || 
				id == R.id.activity_submit_cb4){
			if(checked != null){
				checked.setChecked(false);
			}
			checked = (CheckBox)v;
		}
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
	    case android.R.id.home:
			finish();
	    	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private class SubmitQuestion extends AsyncTask<Void, Void, Void>{
		private Context context;
		private String themeId = "-1";
		private String themeName;
		private String questionStr;
		private String bitmapEncoded;
		private String ans1Str;
		private String ans2Str;
		private String ans3Str;
		private String ans4Str;
		private String rightAns;
		
		private boolean res;
		
		public SubmitQuestion(){}
		
		public SubmitQuestion setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Utils.crossfade(context, progressBar, mainView);
			themeName = acTextView.getText().toString().trim();
			for(Theme t : themelist){
				String name = t.getName().trim();
				if(name.equals(themeName)){
					themeId = t.getId();
					break;
				}
			}
			if(upThemeId != null && upThemeName != null){
				themeId = upThemeId;
				themeName = upThemeName;
			}
			questionStr = question.getText().toString().trim();
			ans1Str = ans1.getText().toString().trim();
			ans2Str = ans2.getText().toString().trim();
			ans3Str = ans3.getText().toString().trim();
			ans4Str = ans4.getText().toString().trim();
			if(checked != null){
				if(checked.getId() == R.id.activity_submit_cb1){
					rightAns = "1";
				}else if(checked.getId() == R.id.activity_submit_cb1){
					rightAns = "2";
				}else if(checked.getId() == R.id.activity_submit_cb1){
					rightAns = "3";
				}else{
					rightAns = "4";
				}
			}
			if(bitmap != null){
				bitmapEncoded = Utils.getBase64EncodedBitmap(bitmap, 100);
			}else{
				bitmapEncoded = "";
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if(context == null){
				return null;
			}
			res = APIHandler.submitQuestion(themeId, themeName, questionStr, ans1Str, ans2Str, ans3Str, ans4Str, rightAns, bitmapEncoded);
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(context == null){
				return;
			}
			Utils.crossfade(context, mainView, progressBar);
			if(upThemeName == null){
				acTextView.setText("");
			}
			question.setText("");
			ans1.setText("");
			ans2.setText("");
			ans3.setText("");
			ans4.setText("");
			if(checked != null){
				checked.setChecked(false);
				checked = null;
			}
			bitmap = null;
			imageView.setImageBitmap(null);
			Toast.makeText(context, getResources().getString(R.string.thank_you), Toast.LENGTH_LONG).show();
		}	
	}
	
	private class GetThemes extends AsyncTask<Void, Void, Void>{
		
		private Context context;
		
		public GetThemes(){}
		
		public GetThemes setContext(Context context){
			this.context = context;
			return this;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			if(context == null){
				return null;
			}
			themelist = ThemeDao.getInstance(context).getChildList();
			return null;
		}
		
		public void onPostExecute(Void unused){
			if(context == null){
				return;
			}
			if(themelist == null){
				Toast.makeText(context, context.getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
			}
			titles = new ArrayList<String>();
			
			for(Theme t : themelist){
				titles.add(t.getName());
			}
			Collections.sort(titles);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, 
					android.R.layout.simple_dropdown_item_1line, titles);
	        acTextView.setAdapter(adapter);
			Utils.crossfade(context, mainView, progressBar);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode != Activity.RESULT_OK) {
            return;
        }
        
        switch (requestCode){
            case REQUEST_CODE_GALLERY:
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                    Utils.copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    startCropImage();
                } catch (Exception e) {}

                break;

            case REQUEST_CODE_CROP_IMAGE:
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    return;
                }
                bitmap = BitmapFactory.decodeFile(tempFile.getPath());
                imageView.setImageBitmap(bitmap);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);        
    }
	
	private void startCropImage() {
	    Intent intent = new Intent(this, CropImage.class);
	    intent.putExtra(CropImage.IMAGE_PATH, tempFile.getPath());
	    intent.putExtra(CropImage.SCALE, true);
	
	    intent.putExtra(CropImage.ASPECT_X, 2);
	    intent.putExtra(CropImage.ASPECT_Y, 2);
	
	    startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
	}
	
	private String isValid(){
		
		if(acTextView.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_theme_name);
		}
		
		if(acTextView.getText().toString().length() < 3){
			return getResources().getString(R.string.short_theme_name);
		}
		
		if(acTextView.getText().toString().length() > 100){
			return getResources().getString(R.string.long_theme_name);
		}
		
		if(question.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_question);
		}
		
		if(question.getText().toString().length() < 5){
			return getResources().getString(R.string.short_question);
		}
		
		if(question.getText().toString().length() > 300){
			return getResources().getString(R.string.long_question);
		}
		
		if(ans1.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_ans1);
		}
		
		if(ans1.getText().toString().length() < 1){
			return getResources().getString(R.string.short_ans1);
		}
		
		if(ans1.getText().toString().length() > 100){
			return getResources().getString(R.string.long_ans1);
		}
		
		if(ans2.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_ans2);
		}
		
		if(ans2.getText().toString().length() < 1){
			return getResources().getString(R.string.short_ans2);
		}
		
		if(ans2.getText().toString().length() > 100){
			return getResources().getString(R.string.long_ans2);
		}
		
		if(ans3.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_ans3);
		}
		
		if(ans3.getText().toString().length() < 1){
			return getResources().getString(R.string.short_ans3);
		}
		
		if(ans3.getText().toString().length() > 100){
			return getResources().getString(R.string.long_ans3);
		}
		
		if(ans4.getText().toString().length() == 0){
			return getResources().getString(R.string.enter_ans4);
		}
		
		if(ans4.getText().toString().length() < 1){
			return getResources().getString(R.string.short_ans4);
		}
		
		if(ans4.getText().toString().length() > 100){
			return getResources().getString(R.string.long_ans4);
		}
		
		if(checked == null){
			return getResources().getString(R.string.select_right);
		}
		
		return null;
	}
	
	private void showConfirmDialog(final Context context){
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_game_dialog);
		
		TextView title = (TextView) dialog.findViewById(R.id.game_dialog_textview);
		Button ok = (Button) dialog.findViewById(R.id.game_dialog_ok_button);
		Button cancel = (Button) dialog.findViewById(R.id.game_dialog_cancel_button);
		
		title.setTypeface(tf);
		ok.setTypeface(tf);
		cancel.setTypeface(tf);
		title.setText(getResources().getString(R.string.confirm_warning));
		
		ok.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				new SubmitQuestion().setContext(context).execute();
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
}
