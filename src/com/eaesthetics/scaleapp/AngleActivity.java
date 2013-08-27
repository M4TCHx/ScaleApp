package com.eaesthetics.scaleapp;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class AngleActivity extends Activity implements SensorEventListener {

    /** Filters raw sensor data into instantaneous angle */
	private class Filter {
		static final int AVERAGE_BUFFER = 15;
		float[] m_arr = new float[AVERAGE_BUFFER];
		int m_idx = 0;
	
		public float append(float val) {
			m_arr[m_idx] = val;
			m_idx++;
			if (m_idx == AVERAGE_BUFFER)
				m_idx = 0;
			
			return avg();
		}
		/** The average orientation over the AVERAGE_BUFFER */
		public float avg() {
			float sum = 0;
			for (float x : m_arr)
				sum += x;
			return sum / AVERAGE_BUFFER;
		}
	}

	final static int AVERAGE_BUFFER = 100;
	Filter[] m_filters = { new Filter(), new Filter(), new Filter() }; // A filter used to filtes the raw sensor data
	float[] m_lastAccels;
	float[] m_lastMagFields;
	float m_lastPitch = 0.3f;
	float m_lastRoll = 0.3f;
	float m_lastYaw = 0.3f;
	private float[] m_orientation = new float[4];
	int m_pitchIndex = 0;
	float[] m_prevPitch = new float[AVERAGE_BUFFER];
	float[] m_prevRoll = new float[AVERAGE_BUFFER];
	int m_rollIndex = 0;
	private float[] m_rotationMatrix = new float[16];
	protected Sensor mSensor;
	protected SensorManager mSensorManager;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_angle);
    }

	/**Called when the device accelerates in any direction. 
	 * @param event The sensor event*/
	private void accel(SensorEvent event) {
		if (m_lastAccels == null) {
			m_lastAccels = new float[3];
		}
	
		System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
		
	}

	/** Magnetic field information is added to an array here from the sensor data */
	private void mag(SensorEvent event) {
		if (m_lastMagFields == null) {
			m_lastMagFields = new float[3];
		}
	
		System.arraycopy(event.values, 0, m_lastMagFields, 0, 3);
	
		if (m_lastAccels != null) {
			computeOrientation();
		}
	}

	/** Registers the listeners of the sensor */
	private void registerListeners() {
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
	}

	/** Unregisters the listeners of the sensors */
	private void unregisterListeners() {
		mSensorManager.unregisterListener(this);
	}

	/** Computes device orientation based on sensor*/
	private void computeOrientation() {
	
	
		if (SensorManager.getRotationMatrix(m_rotationMatrix, null,
				m_lastAccels, m_lastMagFields)) {
			SensorManager.getOrientation(m_rotationMatrix, m_orientation);
	
			/* Radian Measure 57.295 degrees */
	
			float pitch = m_orientation[1] * 57.295f;
			m_lastPitch = m_filters[1].append(pitch);
	
		}
	}

	/** Called sensors are changed. */
	@Override
	public void onSensorChanged(SensorEvent event) {
		//debug();
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accel(event);
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mag(event);
		}
	
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

}
