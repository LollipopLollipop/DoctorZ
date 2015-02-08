package com.example.doctorz;

import android.util.Log;

public class PitchDetectionRepresentation {
	PitchDetectionRepresentation(double pitch_) {
		pitch = pitch_;
		creation_date_ = System.currentTimeMillis();
	}

	public int GetAlpha() {
		final long age = System.currentTimeMillis() - creation_date_;
		if (age > LIFE_TIME_MS) return 0;
		if (age < BRIGHT_TIME_MS) return 255;
		return (int) Math.floor(255 - (age - BRIGHT_TIME_MS) * 1.0 / 
				                      (LIFE_TIME_MS - BRIGHT_TIME_MS) * 255);
	}
	
	
	public double pitch;
	private long creation_date_;
	
	private final static int LIFE_TIME_MS = 4000;
	private final static int BRIGHT_TIME_MS = 2000;

}
