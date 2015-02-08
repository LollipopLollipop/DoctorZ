package com.example.doctorz;

import android.content.Context;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.os.Handler;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.example.doctorz.PitchDetectionRepresentation;

public class FreqDisplay extends View {
	
	private LinkedHashMap<Double, Double> frequencies_ = null;
	private LinkedHashMap<Double, Double> raw_frequencies_ = null;
	private double pitch_;
	private PitchDetectionRepresentation representation_;
	private Handler handler_;
	private Timer timer_;
	final static int HEADSTOCK_HEIGHT = 10;
    final static int HEADSTOCK_WIDTH = 50;
    private final static int MIN_AMPLITUDE = 50;
    private final static int MAX_AMPLITUDE = 500;

	private final static int UI_UPDATE_MS = 100;
	
	private static final String TAG = "MyActivity";
	
	public FreqDisplay(Context context) {
		super(context);
		// UI update cycle.
		handler_ = new Handler();
		timer_ = new Timer();
		timer_.schedule(new TimerTask() {
				public void run() {
					handler_.post(new Runnable() {
						public void run() {
							invalidate();
						}
					});
				}
			},
			UI_UPDATE_MS ,
			UI_UPDATE_MS );
	}
	
	private void DrawCurrentFrequency(Canvas canvas, int x, int y) {
		
			final int alpha = representation_.GetAlpha();
			if (alpha == 0) return;
			Paint paint = new Paint();
			paint.setARGB(alpha, 192, 108, 132);
			paint.setTextSize(70);
		
			canvas.drawText(Math.round(representation_.pitch * 10) / 10.0 + " Hz", 130, 150, paint);
		
	}
	private void DrawHistogram(Canvas canvas, Rect rect) {
        if (frequencies_ == null) 
        	return;
        Paint paint = new Paint();
        // Draw border.
        paint.setARGB(80, 236, 236, 238);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);
        
        /* Draw threshold.
        paint.setARGB(180, 200, 0, 0);
        final long threshold_screen_height = GetAmplitudeScreenHeight(canvas, MIN_AMPLITUDE, rect);
        canvas.drawLine(rect.left, rect.bottom - threshold_screen_height, rect.right, rect.bottom - threshold_screen_height, paint);
		*/
        // Draw histogram.
        paint.setARGB(255, 192, 108, 132);
        int column_no = 0;
        Iterator<Entry<Double, Double>> it = frequencies_.entrySet().iterator();
        int size = raw_frequencies_.size();
        //Log.d(TAG, "the size of frequencies is "+ frequencies_.size());
        double ipt;
        double rst;
        for(int i = 0; i < size - 1; i ++){
			for(int j = 0; j < 10; j ++){
				ipt = (double)(i)+(double)(j)/10;
				//Log.d(TAG, "value of ipt is "+ ipt);
				rst = frequencies_.get(ipt);
				//Log.d(TAG, "value of rst is "+ rst);
				final double amplitude = Math.min(rst, MAX_AMPLITUDE);
                //Log.d(TAG, "the value of frequencies at here is " + entry.getValue());
                final long height = GetAmplitudeScreenHeight(canvas, amplitude, rect);
                //Log.v(TAG, "height is " + height);
                final long topValue = rect.bottom - height;
                final long leftValue = rect.left + rect.width() * column_no / frequencies_.size();
                final long rightValue = rect.left + rect.width() * (column_no + 1) / frequencies_.size();
                final long bottomValue = rect.bottom;
                //Log.v(TAG, "left value is " + leftValue);
                //Log.v(TAG, "top value is " + topValue);
                //Log.v(TAG, "right value is " + rightValue);
                //Log.v(TAG, "bottom value is " + bottomValue);
                canvas.drawRect(leftValue, topValue, rightValue, bottomValue, paint);
                column_no++;
				
			}
		}
        return;
	}
	private long GetAmplitudeScreenHeight(Canvas canvas, double amplitude, Rect histogram_rect) {
		return Math.round(amplitude / MAX_AMPLITUDE * histogram_rect.height());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		if (frequencies_ != null) {
			final int MARGIN = 20;
            final int effective_height = canvas.getHeight() - 4 * MARGIN;
            final int effective_width = canvas.getWidth() - 2 * MARGIN;
     
            final Rect histogram = new Rect(MARGIN, 2 * MARGIN, 
            		effective_width + MARGIN, effective_height + MARGIN*2);
            DrawHistogram(canvas, histogram);
            return;
		}
		else{
			representation_ = new PitchDetectionRepresentation(pitch_);
			DrawCurrentFrequency(canvas, 150, 100);
		}
	}

	public void setPitchResults(double pitch) {
		pitch_ = pitch;
		
	}
	public void setContourResults(final LinkedHashMap<Double, Double> frequencies, final LinkedHashMap<Double, Double> pitches) {
		frequencies_ = frequencies;
		raw_frequencies_ = pitches;
	}
}
