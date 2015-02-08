package com.example.doctorz;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.annotation.TargetApi;
import android.os.Build;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.content.Context;
import android.widget.TextView;
import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.content.Intent;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.example.doctorz.FreqDisplay;
import edu.emory.mathcs.jtransforms.fft.*;

public class CheckLinguistic extends Activity {
	
	public final static int mButtonHeight = 100;
    public final static int mButtonWidth = 150;

    private AudioRecord recorder_;
    private int buffSize;
    private final static int SOURCE = AudioSource.MIC;
    //refers to the microphone audio source
    private final static int SAMPLE_RATE = 44100; 
    //human vocie frequency range: 300-3.5K Hz 
    private final static int CHANNEL_MODE = AudioFormat.CHANNEL_IN_MONO;
    //describes the configuration of the audio channels
    private final static int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //the audio data is represented PCM 16 bit per sample
    
    private static final String TAG = "MyActivity";
    private final static int CHUNK_SIZE_IN_SAMPLES = 2048; //2^12 while 12 = 3*4 
    //public native void DoFFT(double[] data, int size);
    FreqDisplay ff;
    LinkedHashMap<Double, Double> pitches = new LinkedHashMap<Double, Double>();
    LinkedHashMap<Double, Double> interpo_pitches = new LinkedHashMap<Double, Double>();
    private final static int MIN_FREQUENCY = 50; 
    private final static int MAX_FREQUENCY = 3000;
    //range of human voice fundamental frequencies 

    private final static int DRAW_FREQUENCY_STEP = 5;
    
    Thread recordThread = new Thread(){
        @Override
        public void run()
        {	
        	buffSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,CHANNEL_MODE,ENCODING_FORMAT);
        	Log.v(TAG, buffSize + " is the buffer size");
        	//Returns the minimum buffer size required for the successful creation 
        	//of an AudioRecord object, in byte units. 
        	recorder_ = new AudioRecord(SOURCE, SAMPLE_RATE, CHANNEL_MODE, ENCODING_FORMAT, buffSize);//bufferSizeInBytes
        	//recorder_ = new AudioRecord(AudioSource.MIC, RATE, CHANNEL_MODE, ENCODING, 6144);
        	if (recorder_.getState() != AudioRecord.STATE_INITIALIZED) {
        		Log.e(TAG, "cant initialize");
        		return;
        	}

        	recorder_.startRecording();
        	Double t = 0.0;
            while(!interrupted())
            {
            	//short[] audio_data = new short[BUFFER_SIZE_IN_BYTES / 2];
            	short[] audio_data = new short[CHUNK_SIZE_IN_SAMPLES];
            	int numRead = recorder_.read(audio_data, 0, CHUNK_SIZE_IN_SAMPLES);
            	//1/8000 * 128 = 20 ms
            	//pick audio of 20ms each time, 1024 samples, 1/44100 * 2048 = 40+ms 
            	//Log.v(TAG, numRead + " the number of shorts that were read ");
                FreqResult fr = AnalyzeFrequencies(audio_data);
                if(fr.best_frequency < 1000){
                	pitches.put(t++, fr.best_frequency);
                	ff.setPitchResults(fr.best_frequency); 
                }
            }
        }
    }; 
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Intent intent = getIntent();
		//System.loadLibrary("fft-jni");
		//page layout 
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
		ll.setBackgroundColor(Color.TRANSPARENT);
		
		
        LinearLayout subLayout = new LinearLayout(this);
        subLayout.setOrientation(LinearLayout.VERTICAL);
        subLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT-mButtonHeight, 1));
        subLayout.setBackgroundColor(Color.TRANSPARENT);
        subLayout.setGravity(Gravity.CENTER);
        TextView tv = new TextView(this);
        tv.setText("Talk according to what you want to check. Speech pitch will be updated below.");
        tv.setTextColor(getResources().getColor(R.color.myBlue));
        tv.setTextSize(25);
        tv.setPadding(10,10,10,10);
        ff = new FreqDisplay(this);
        subLayout.addView(tv);
        subLayout.addView(ff);
        
        LinearLayout btnLayer = new LinearLayout(this);
		btnLayer.setOrientation(LinearLayout.HORIZONTAL);
		btnLayer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 
                        mButtonHeight, 
                        1));
		btnLayer.setGravity(Gravity.CENTER);
		btnLayer.setBackgroundColor(Color.TRANSPARENT);

        // -- create four buttons inside top layout
		RecordButton recordBtn = new RecordButton(this);
		StopButton stopBtn = new StopButton(this);
        DiagButton diagBtn = new DiagButton(this);
        
        recordBtn.setWidth(mButtonWidth);
        stopBtn.setWidth(mButtonWidth);
        diagBtn.setWidth(mButtonWidth);
        recordBtn.setTextSize(15);
        stopBtn.setTextSize(15);
        diagBtn.setTextSize(15);
        recordBtn.setText("Record");
        stopBtn.setText("Stop");
        diagBtn.setText("Diagnosis");
        
        btnLayer.addView(recordBtn);
        btnLayer.addView(stopBtn);
        btnLayer.addView(diagBtn);
        
        
        ll.addView(subLayout);
        ll.addView(btnLayer);
        setContentView(ll);

		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	class RecordButton extends Button {
        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
            	recordThread.start();    
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setOnClickListener(clicker);
        }
    }
	
	class StopButton extends Button {
        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
            	recordThread.interrupt();
            }
        };

        public StopButton(Context ctx) {
            super(ctx);
            setOnClickListener(clicker);
        }
    }
	class DiagButton extends Button {
        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
            	curveFitting();
            	Log.d(TAG, "the size of interpo pitches is " + interpo_pitches.size());
            	ff.setContourResults(interpo_pitches, pitches);
            	//ff.setContourResults(pitches);
            	
            }
        };

        public DiagButton(Context ctx) {
            super(ctx);
            setOnClickListener(clicker);
        }
    }
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public double[] HanningWindow(short[] signal_in ,int pos ,int size)
	{	
		double[] signal_out = new double[size];
	    for (int i= pos; i < pos+size; i++)
	    {
	    	signal_out[i] = ((signal_in[i]) * ( 0.5 * (1-Math.cos( (2 * Math.PI * i) /    (size - 1))))) ;
	    }
	    return signal_out;
	}
	
	public FreqResult AnalyzeFrequencies(short[] audio_data) {
		//cepstrum analysis 		
		FreqResult fr = new FreqResult();
        DoubleFFT_1D fft = new DoubleFFT_1D(CHUNK_SIZE_IN_SAMPLES);

        double[] data = new double[CHUNK_SIZE_IN_SAMPLES];
        
        final int min_frequency_fft = Math.round(MIN_FREQUENCY
				* CHUNK_SIZE_IN_SAMPLES / SAMPLE_RATE);
		final int max_frequency_fft = Math.round(MAX_FREQUENCY
				* CHUNK_SIZE_IN_SAMPLES / SAMPLE_RATE);
		
		//add a Hanning window for lower frequency leakage 
        data = HanningWindow(audio_data, 0, CHUNK_SIZE_IN_SAMPLES);
        //step 1 of cepstrum 
        fft.realForward(data);
        
        double[] squared_magnitude = new double [CHUNK_SIZE_IN_SAMPLES/2];
        double[] log_magnitude = new double [CHUNK_SIZE_IN_SAMPLES];
        //perform cepstrum and test if vocalic 
        double total_energy = 0;
        double band_energy = 0;
        for (int j = 0; j < CHUNK_SIZE_IN_SAMPLES/2; j++) {
        	double current_frequency  = j * SAMPLE_RATE / CHUNK_SIZE_IN_SAMPLES;
        	//step 2 of cepstrum 
			squared_magnitude[j] = Math.pow(data[2*j],2) + Math.pow(data[2*j + 1],2);
			total_energy += squared_magnitude[j];
			if(current_frequency >= 80 && current_frequency <= 2500)
				band_energy += squared_magnitude[j];
			//step 3 of cepstrum 
			log_magnitude[2*j] = Math.log10(squared_magnitude[j]);
			log_magnitude[2*j+1] = 0;
		}
        //step 4 of cepstrum 
		fft.realInverse(log_magnitude, false);
		
		double max_amplitude = 0;
		double pitch = 0;
		for(int k = SAMPLE_RATE/MAX_FREQUENCY; k <= SAMPLE_RATE/MIN_FREQUENCY; k++){
			if(log_magnitude[k] > max_amplitude){
				max_amplitude = log_magnitude[k];
				pitch = SAMPLE_RATE/k;
			}
		}
		fr.best_frequency = pitch;
        return fr;
	}
	
	private void curveFitting(){
		SimpleRegression regression = new SimpleRegression();
		int size = pitches.size();
		double[] tVal = new double[size];
		double[] pVal = new double[size];
		for(int i=0; i < size; i++){
			tVal[i] = (double)(i);
			pVal[i] = pitches.get(tVal[i]);
			regression.addData(tVal[i], pVal[i]);
		}
		double b = regression.getIntercept();
		Log.d(TAG, "intervept value is " + b);
		double k = regression.getSlope();
		Log.d(TAG, "slope value is " + k);
		double ipt;
		double rst;
		for(int i = 0; i < size - 1; i ++){
			for(int j = 0; j < 10; j ++){
				ipt = (double)(i)+(double)(j)/10;
				Log.d(TAG, "value of ipt is "+ ipt);
				rst = regression.predict(ipt);
				Log.d(TAG, "value of rst is "+ rst);
				interpo_pitches.put(ipt, rst);
			}
		}
		
	}
	//functions to calculate pitch 
	private static class FreqResult {
        public LinkedHashMap<Double, Double> frequencies;
        public double best_frequency;
	}

	public static class FrequencyCluster {
        public double average_frequency = 0;
        public double total_amplitude = 0;
        
        public void add(double freq, double amplitude) {
                double new_total_amp = total_amplitude + amplitude;
                average_frequency = (total_amplitude * average_frequency + freq * amplitude) / new_total_amp;
                total_amplitude = new_total_amp;
        }
        
        public boolean isNear(double freq) {
                if (Math.abs(1 - (average_frequency / freq)) < 0.01) {
                        // only 1% difference
                        return true;
                } else {
                        return false;
                }
        }
        
        public boolean isHarmonic(double freq) {
                double harmonic_factor = freq / average_frequency;
                double distance_from_int = Math.abs(Math.round(harmonic_factor) - harmonic_factor);
                if (distance_from_int < 0.01) {
                        // only 1% distance
                        return true;
                } else {
                        return false;
                }                       
        }

        public void addHarmony(double freq, double amp) {
                total_amplitude += amp;
        }
        
        @Override public String toString() {
                return "(" + average_frequency + ", " + total_amplitude + ")";
        }
	}
}
