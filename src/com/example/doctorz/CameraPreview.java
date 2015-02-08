package com.example.doctorz;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.util.AttributeSet;
import android.util.Log;
import android.hardware.Camera.Parameters;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    String TAG = "CameraPreview";
    private Camera.Parameters mParameters;
    private byte[] mBuffer;

    /*
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera; 
        init();
    }*///extra
    public CameraPreview(Context context) {
        super(context);
        init();
    }
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    //to take the picture and save it 
    //This method is the one that will get an area of the image, 
    //or a set of coordinates in the image, and turn it into a Bitmap. 
    public Bitmap getPic(int left, int top, int right, int bottom) {
        System.gc(); 
        Bitmap b = null;
        //mParameters = mCamera.getParameters();
        Size s = mParameters.getPreviewSize();
        //Returns the dimensions setting for preview pictures.
        Log.d(TAG, "the width of s is " + s.width);
        Log.d(TAG, "the height of s is " + s.height);
        //YuvImage is used, compressed to jpeg and turned to bitmap.
        YuvImage yuvimage = new YuvImage(mBuffer, ImageFormat.NV21, s.width, s.height, null);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(s.width-bottom + 80, left - 20, s.width-top + 80, right - 20), 100, outStream); 
        // make JPG
        b = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.size());
        if (b != null) {
            Log.d(TAG, "getPic() WxH:" + b.getWidth() + "x" + b.getHeight());
        } else {
            Log.d(TAG, "getPic(): Bitmap is null..");
        }
        yuvimage = null;
        outStream = null;
        System.gc();
        return b;
    }
    //in order to keep the image, we need to set up a buffer. 
    //This will ensure that when the image is captured, 
    //the buffer is big enough to hold the data.
    private void updateBufferSize() {
        mBuffer = null;
        System.gc();
        // prepare a buffer for copying preview data to
        int h = mCamera.getParameters().getPreviewSize().height;
        int w = mCamera.getParameters().getPreviewSize().width;
        int bitsPerPixel =     
                     ImageFormat.getBitsPerPixel(mCamera.getParameters().getPreviewFormat());
        mBuffer = new byte[w * h * bitsPerPixel / 8];
        //Log.i("surfaceCreated", "buffer length is " + mBuffer.length + " bytes");
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
    	// The Surface has been created, acquire the camera and tell it where to draw.
        try {
            mCamera = Camera.open(Camera.getNumberOfCameras()-1); 
            // WARNING: without permission in Manifest.xml, crashes
        }
        catch (RuntimeException exception) {
            //Log.i(TAG, "Exception on Camera.open(): " + exception.toString());
            
        }
        
    	// The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            //mCamera.startPreview();
            updateBufferSize();
            mCamera.addCallbackBuffer(mBuffer); // where we'll store the image data
            //Installs a callback to be invoked for every preview frame, 
            //using buffers supplied with addCallbackBuffer(byte[]), 
            //in addition to displaying them on the screen.
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                public synchronized void onPreviewFrame(byte[] data, Camera c) {
     
                    if (mCamera != null) { // there was a race condition when onStop() was called..
                        mCamera.addCallbackBuffer(mBuffer); // it was consumed by the call, add it back
                    }
                }
            });
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            if (mCamera != null){
                mCamera.release();
                mCamera = null;
            }
        }
        
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	try {
            mParameters = mCamera.getParameters();
            //mParameters.set("orientation","portrait");
            for (Integer i : mParameters.getSupportedPreviewFormats()) {
                Log.d(TAG, "supported preview format: " + i);
            } 
 
            List<Size> sizes = mParameters.getSupportedPreviewSizes();
            for (Size size : sizes) {
                Log.i(TAG, "supported preview size: " + size.width + "x" + size.height);
            }
            mCamera.setParameters(mParameters); // apply the changes
        } catch (Exception e) {
            // older phone - doesn't support these calls
        }
 
        Size p = mCamera.getParameters().getPreviewSize();
        //Log.i(TAG, "Preview: checking it was set: " + p.width + "x" + p.height); // DEBUG

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    
    public Parameters getCameraParameters(){
        return mCamera.getParameters();
    }
    
    
    public void setCameraFocus(AutoFocusCallback autoFocus){
        if (mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_AUTO) ||
                mCamera.getParameters().getFocusMode().equals(mCamera.getParameters().FOCUS_MODE_MACRO)){
            mCamera.autoFocus(autoFocus);
        }
    }
     
    
    public void onPause() {
	    mCamera.release();
	    mCamera = null;
	}
}