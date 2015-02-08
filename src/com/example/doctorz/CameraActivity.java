package com.example.doctorz;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.hardware.Camera;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import com.example.doctorz.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.hardware.Camera.PictureCallback;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.util.DisplayMetrics;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.lang.String;
import android.widget.Button;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.View;
import android.content.ContextWrapper;
import android.content.Context;
import android.content.Intent;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraAct";
	//private Camera mCamera;//extra
	private CameraPreview mPreview; 
    private ImageView mTakePicture;
    private DrawView mView;
 
    private boolean mAutoFocus = true;
    private boolean mInitialized = false;
    private boolean mFlashBoolean = false;
    public final static String EXTRA_MESSAGE_REC = "com.example.doctorz.RECMESSAGE";
 
    //private SensorManager mSensorManager;
    //private Sensor mAccel;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private Rect rec = new Rect();
    //private Camera.Parameters mParameters;//extra
 
    private int mScreenHeight;
    private int mScreenWidth;
 
    private boolean mInvalidate = false;
 
    private File mLocation = new File(Environment.
            getExternalStorageDirectory(),"test.jpg");
    long curTime = System.currentTimeMillis();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//mCamera = getCameraInstance();//extra
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_camera);
		// the accelerometer is used for autofocus
        /*mSensorManager = (SensorManager) getSystemService(Context.
                SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.
                TYPE_ACCELEROMETER);
 		*/
        // get the window width and height to display buttons
        // according to device screen size
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        Log.d(TAG, "height is "+mScreenHeight);
        mScreenWidth = displaymetrics.widthPixels;
        Log.d(TAG, "width is "+mScreenWidth);
        // I need to get the dimensions of this drawable to set margins
        // for the ImageView that is used to take pictures
        Drawable mButtonDrawable = this.getResources().
        getDrawable(R.drawable.camera);
 
        
        mTakePicture = (ImageView) findViewById(R.id.startcamerapreview);
 
        // setting where I will draw the ImageView for taking pictures
        LayoutParams lp = new LayoutParams(mTakePicture.getLayoutParams());
        lp.setMargins((int)((double)mScreenWidth*.85),
                (int)((double)mScreenHeight*.70) ,
                (int)((double)mScreenWidth*.85)+mButtonDrawable.
                getMinimumWidth(), 
                (int)((double)mScreenHeight*.70)+mButtonDrawable.
                getMinimumHeight());
        mTakePicture.setLayoutParams(lp);
        // rec is used for onInterceptTouchEvent. I pass this from the
        // highest to lowest layer so that when this area of the screen
        // is pressed, it ignores the TouchView events and passes it to
        // this activity so that the button can be pressed.
        rec.set((int)((double)mScreenWidth*.85),
                (int)((double)mScreenHeight*.10) ,
                (int)((double)mScreenWidth*.85)+mButtonDrawable.getMinimumWidth(), 
                (int)((double)mScreenHeight*.70)+mButtonDrawable.getMinimumHeight());
        mButtonDrawable = null;
        mTakePicture.setClickable(true);
        //Button btn = (Button) findViewById(R.id.button_capture);
        //btn.setClickable(true);
        //btn.setOnClickListener(previewListener);
        mTakePicture.setOnClickListener(previewListener);
        // get our Views from the XML layout
        mPreview = (CameraPreview) findViewById(R.id.preview);
        mView = (DrawView) findViewById(R.id.left_top_view);
        mView.setRec(rec);
        /*
        mPreview = new CameraPreview(this);
        mView = new DrawView(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        preview.addView(mView);*/
	}
	// this is the autofocus call back
    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
 
        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
            //Wait.oneSec();
            mAutoFocus = true;
        }};
        
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
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
	
	// with this I get the ratio between screen size and pixels
	// of the image so I can capture only the rectangular area of the
	// image and save it.
	public Double[] getRatio(){
	    Size s = mPreview.getCameraParameters().getPreviewSize();
	    double heightRatio = (double)s.height/(double)mScreenHeight;
	    double widthRatio = (double)s.width/(double)mScreenWidth;
	    Double[] ratio = {heightRatio,widthRatio};
	    return ratio;
	}
	
	// This method takes the preview image, grabs the rectangular
	// part of the image selected by the bounding box and saves it.
	// A thread is needed to save the picture so not to hold the UI thread.
	private OnClickListener previewListener = new OnClickListener() {
	 
	    @Override
	    public void onClick(View v) {
	        //if (mAutoFocus){
	            mAutoFocus = false;
	            //mPreview.setFlash(false);
	            //mPreview.setCameraFocus(myAutoFocusCallback);
	            Wait.oneSec();
	            Log.i("TESTESTEST","Taking picture");
	            Thread tGetPic = new Thread( new Runnable() {
	                public void run() {
	                    int left = (int) (mView.getmLeftTopPosX());	                    
	                    int top = (int) (mView.getmLeftTopPosY());	                    
	                    int right = (int)(mView.getmRightBottomPosX());
	                    int bottom = (int)(mView.getmRightBottomPosY());
	                    savePhoto(mPreview.getPic(left,top,right,bottom), curTime);
	                    mAutoFocus = true;
	                } 
	            });
	            tGetPic.start();
	            Wait.oneSec();
	            gotoRec(curTime);
	            
	        //}
	        boolean pressed = false;
	        if (!mTakePicture.isPressed()){
	            pressed = true;
	        }
	    }      
	};
	
	// just to close the app and release resources.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	 
	    if (keyCode == KeyEvent.KEYCODE_BACK){
	        finish();
	    }
	    return super.onKeyDown(keyCode, event); 
	}
	public void gotoRec(long cutTime){
		Intent intentRec = new Intent(this, RecActivity.class);
		Log.d(TAG, "cur time in camera activity "+ curTime);
		intentRec.putExtra(EXTRA_MESSAGE_REC, cutTime);
        startActivity(intentRec);
	}
	private boolean savePhoto(Bitmap bm, long curTime) {
		String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File imageOutput = new File(outputPath + "/" + curTime + ".jpg");
	    FileOutputStream image = null;
	    try {
	        image = new FileOutputStream(imageOutput);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    Matrix matrix = new Matrix();

	    matrix.postRotate(90);
	    matrix.postRotate(90);
	    matrix.postRotate(90);

	    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,256,256,true);

	    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, 256, 256, matrix, true);
	    rotatedBitmap.compress(CompressFormat.JPEG, 100, image);
	    MediaStore.Images.Media.insertImage(getContentResolver(), rotatedBitmap, "test.jpg" , "test");
	    //extra
	    if (bm != null) {
	        int h = bm.getHeight();
	        int w = bm.getWidth();
	        //Log.i(TAG, "savePhoto(): Bitmap WxH is " + w + "x" + h);
	    } else {
	        //Log.i(TAG, "savePhoto(): Bitmap is null..");
	        return false;
	    }
	    return true;
	}
	
	// extra overrides to better understand app lifecycle and assist debugging
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    //Log.i(TAG, "onDestroy()");
	}
	 
	@Override
	protected void onPause() {
	    super.onPause();
	    //Log.i(TAG, "onPause()");
	    //mSensorManager.unregisterListener(this);
	    /*if (mPreview != null){
	   	 mPreview.onPause();
	   	 mPreview = null;
	    }*///extra
	}
	 
	@Override
	protected void onResume() {
	    super.onResume();
	    //mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
	    //Log.i(TAG, "onResume()");
	}
	 
	@Override
	protected void onRestart() {
	    super.onRestart();
	    //Log.i(TAG, "onRestart()");
	}
	 
	@Override
	protected void onStop() {
	    super.onStop();
	    //Log.i(TAG, "onStop()");
	}
	 
	@Override
	protected void onStart() {
	    super.onStart();
	    //Log.i(TAG, "onStart()");
	}
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(Camera.getNumberOfCameras()-1); // attempt to get a Camera instance
	        //you can access specific cameras using Camera.open(int)
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

}
