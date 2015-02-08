package com.example.doctorz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class RecActivity extends Activity {

	private static final String TAG = "RecActivity";
	private ImageView imgView;
	private int emStatus = 0;
	String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public final static String EXTRA_MESSAGE_DET = "com.example.doctorz.DETMESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
	    final long curTime = intent.getLongExtra(CameraActivity.EXTRA_MESSAGE_REC, 1);
	    Log.d(TAG, "cur time in rec activity "+ curTime);
		setContentView(R.layout.activity_rec);
		
		imgView = (ImageView) findViewById(R.id.iconimage);
		imgView.setImageBitmap(BitmapFactory.decodeFile(outputPath + "/" + curTime + ".jpg"));
		
		Button buttonX = (Button)findViewById(R.id.button3);
		buttonX.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	detectEM(curTime);
		    } 
		});
		Button buttonY = (Button)findViewById(R.id.button2);
		buttonY.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	loadTrainingData();
		    } 
		});
		Button buttonZ = (Button)findViewById(R.id.button4);
		buttonZ.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	
		    } 
		});
		Button buttonM = (Button)findViewById(R.id.button5);
		buttonM.setOnClickListener(new OnClickListener() {
		    public void onClick(View v)
		    {
		    	backToHome();
		    } 
		});
		
		
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	private void backToHome(){
		Intent intentBack = new Intent(this, HomePage.class);
		startActivity(intentBack);
	}
	public void detectEM(long curTime){
		final FaceRecognition faceRecognition = new FaceRecognition();		
		emStatus = faceRecognition.recognizeFileList(outputPath + "/" + curTime + ".jpg");
		Log.d(TAG, "the value of emStatus is "+ emStatus);
		imgView = (ImageView) findViewById(R.id.iconimage);
		if (emStatus == 1)
	    	imgView.setImageResource(R.drawable.sadface);
	    else if (emStatus == 2)
	    	imgView.setImageResource(R.drawable.happyface);
		
		/*Intent intentDetect = new Intent(this, ResultActivity.class);
		Log.d(TAG, "cur time in camera activity "+ curTime);
		intentDetect.putExtra(EXTRA_MESSAGE_DET, emStatus);
        startActivity(intentDetect);*/
		
    }
	private void loadTrainingData() {
	    AssetManager assetManager = getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.e("tag", "Failed to get asset file list.", e);
	    }
	    	String filename="facedata.xml";
	        InputStream in = null;
	        OutputStream out = null;
	        String outDir= Environment.getExternalStorageDirectory().getAbsolutePath();
	        Log.d(TAG, "the output location is = "+ outDir);
	        try {
	          in = assetManager.open(filename);
	          if (in != null)
	        	  Log.d(TAG, "load face success");
	          //File outFile = new File(getExternalFilesDir(null), filename);
	          File outFile = new File(outDir, filename);
	          out = new FileOutputStream(outFile);
	          copyFile(in, out);
	         if(out != null)
	        	 Log.d(TAG, "copy success");
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
	        } catch(IOException e) {
	            Log.e("tag", "Failed to copy asset file: " + filename, e);
	        }       
	    
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rec, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
