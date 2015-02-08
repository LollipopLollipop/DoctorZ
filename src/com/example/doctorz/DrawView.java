package com.example.doctorz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.SurfaceView;
import android.view.View;

public class DrawView extends View {
 	//Paints will draw lines, Drawables are the corners, 
 	//floats are the locations and Booleans are so that 
 	//we can only move one corner at a time.
    private Drawable mLeftTopIcon;
    private Drawable mRightTopIcon;
    private Drawable mLeftBottomIcon;
    private Drawable mRightBottomIcon;
     
    // Starting positions of the bounding box
    private float mLeftTopPosX = 142;
    private float mLeftTopPosY = 150;
 
    private float mRightTopPosX = 398;
    private float mRightTopPosY = 150;
 
    private float mLeftBottomPosX = 142;
    private float mLeftBottomPosY = 406;
 
    private float mRightBottomPosX = 398;
    private float mRightBottomPosY = 406;
 
    private Paint recLine;
 
    private Rect buttonRec;
 
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
 
    /* you can ignore this for this code
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    */
    //The constructors set the line colors, size, etc. 
    //It also sets the positions of the paints.
    public DrawView(Context context){
        super(context);
        init(context);
        // This call is necessary, or else the 
        // draw method will not be called. 
        setWillNotDraw(false);
    }
    public DrawView(Context context, AttributeSet attrs){
        super (context,attrs);
        init(context);
    }
     
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    
    @Override
    protected void onDraw(Canvas canvas){
    	//recLine = new Paint();    
        //setLineParameters(Color.GREEN,2);
    // A Simple Text Render to test the display
    	canvas.drawLine(mLeftTopPosX, mLeftTopPosY, mRightTopPosX, mRightTopPosY, recLine);
    	canvas.drawLine(mLeftTopPosX, mLeftTopPosY, mLeftBottomPosX, mLeftBottomPosY, recLine);
    	canvas.drawLine(mRightTopPosX, mRightTopPosY, mRightBottomPosX, mRightBottomPosY, recLine);
    	canvas.drawLine(mLeftBottomPosX, mLeftBottomPosY, mRightBottomPosX, mRightBottomPosY, recLine);
    	
    }
     
    private void init(Context context) {
     
        // I need to create lines for the bouding box to connect     
    	recLine = new Paint();    
        setLineParameters(Color.GREEN,2);
     
    }
     
    private void setLineParameters(int color, float width){
     
    	recLine.setColor(color);
    	recLine.setStrokeWidth(width);
     
    }

     	public float getmLeftTopPosX(){
    	    return mLeftTopPosX;
    	}
    	public float getmLeftTopPosY(){
    	    return mLeftTopPosY;
    	}
    	public float getmRightTopPosX(){
    	    return mRightTopPosX;
    	}
    	public float getmRightTopPosY(){
    	    return mRightTopPosY;
    	}
    	public float getmLeftBottomPosX() {
    	    return mLeftBottomPosX;
    	}
    	public float getmLeftBottomPosY() {
    	    return mLeftBottomPosY;
    	}
    	public float getmRightBottomPosY() {
    	    return mRightBottomPosY;
    	}
    	public float getmRightBottomPosX() {
    	    return mRightBottomPosX;
    	}
    	public void setRec(Rect rec) {
    	    this.buttonRec = rec;
    	}
    	// calls the onDraw method, I used it in my app Translanguage OCR
    	// because I have a thread that needs to invalidate, or redraw
    	// you cannot call onDraw from a thread not the UI thread.
    	public void setInvalidate() {
    	    invalidate();
    	     
    	}
 
}