

package com.eaesthetics.scaleapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
/** The application */
public class MainActivity extends Activity implements SensorEventListener {


	// All of the Float are used to store the data used to calculate the mass, 
	// All of the text views display these number, but are for debug purposes only and not displayed
	protected Float calculatedMass;
	protected TextView calculatedMassView;	
	protected Float changeInAngle;
	protected TextView changeInAngleView;
	protected TextView debugView;
	protected Float finalAngle;
	protected TextView finalAngleView;
	protected Float fiveGramAngle;
	protected TextView fiveGramAngleView;
	protected Float initialAngle;
	protected TextView initialAngleView;
	protected TextView instructionsView;
	protected Sensor mSensor;
	protected SensorManager mSensorManager;
	protected TextView pitchView;
	protected Float tareAngle;
	protected Float theConstantNumber;
	protected TextView theConstantView;
	
	Filter[] m_filters = { new Filter(), new Filter(), new Filter() }; // A filter used to filter the raw sensor data
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
	final static int AVERAGE_BUFFER = 100;

	/**Called when the activity is first created.
	 * @param savedInstanceState the saved instances from the device */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove activity bar
		setContentView(R.layout.activity_main); // binds code to XML layout
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Forces landscape mode
		setButtonClickListener();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE); //Start phone sensors
		registerListeners(); //Register listeners of sensors

	}
	/** Called when the activity is destroyed. */
	@Override
	public void onDestroy() {
		unregisterListeners();
		super.onDestroy();
	}
	/** Called when the activity is paused. */
	@Override
	public void onPause() {
		unregisterListeners();
		super.onPause();

	}
	/** Called when the activity is resumed. */
	@Override
	public void onResume() {
		registerListeners();
		super.onResume();
	} 
	/** Called sensors are changed. */
	@Override
	public void onSensorChanged(SensorEvent event) {
		debug();
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accel(event);
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mag(event);
		}

	}
	/** Called when the activity is stopped. */
	@Override
	public void onStop() {
		unregisterListeners();
		super.onStop();
	} 

	/**Called when the device accelerates in any direction. 
	 * @param event The sensor event*/
	private void accel(SensorEvent event) {
		if (m_lastAccels == null) {
			m_lastAccels = new float[3];
		}

		System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
		
	}
	/** Called when final data button is clicked.*/

	public void bindFinalData() {
		finalAngleView = (TextView) findViewById(R.id.vFinalAngleView);
		finalAngleView.setText("The final Angle is " + finalAngle.toString());
		changeInAngleView = (TextView) findViewById(R.id.vChangeInAngleView);
		changeInAngleView.setText("Change in Angle is "
				+ changeInAngle.toString());
		theConstantView = (TextView) findViewById(R.id.vTheConstantView);
		theConstantView.setText("The Constant is  "
				+ theConstantNumber.toString());
		System.out.println(+theConstantNumber);
		calculatedMassView = (TextView) findViewById(R.id.vCalculatedMassView);
		
		calculatedMassView.setText(+ /*calculatedMass*/ (double)Math.round(calculatedMass * 100) / 100  +" g");
		calculatedMassView.setVisibility(1);

	}
	/** Called when the five gram tare angle button is clicked */
	public void bindFiveGramAngle() {
		fiveGramAngle = m_lastPitch;
		fiveGramAngleView = (TextView) findViewById(R.id.vFiveGramView);
		fiveGramAngleView.setText("The angle at 5 grams is "
				+ fiveGramAngle.toString());
	}
	/** Called when initial tare button is clicked. */
	public void bindInitialAngle() {
		initialAngle = m_lastPitch;
		initialAngleView = (TextView) findViewById(R.id.vInitialAngleView);
		initialAngleView.setText("The Initial angle is "
				+ initialAngle.toString());

	}
/** Calculates mass based on data */
	public void calculateData() {
		finalAngle = m_lastPitch;
		tareAngle = fiveGramAngle - initialAngle;
		theConstantNumber = tareAngle / 5;
		changeInAngle = finalAngle - initialAngle;
		calculatedMass = changeInAngle / theConstantNumber;
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
	/** Debug information */
	public void debug() {
		debugView = (TextView) findViewById(R.id.vDebug);
		Float output = m_lastPitch;
		debugView.setText(output.toString());
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
	/** Called when the accuracy of the device is changed. It isn't during the applications lifetime */
	@Override
	public void onAccuracyChanged(Sensor mSensor, int accuracy) {
		// TODO Auto-generated method stub

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
	/** Sets the click listeners of the buttons */
	public void setButtonClickListener() {
		Button initialButton = (Button) findViewById(R.id.bInitialButton);
		Button fiveGramButton = (Button) findViewById(R.id.bFiveGramButton);
		Button finalAngleButton = (Button) findViewById(R.id.bFinalAngleButton);
		final Button instructionsButton = (Button) findViewById(R.id.bInstructionsButton);
		initialButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeDelay();
				bindInitialAngle();
			}

		});
		fiveGramButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeDelay();
				bindFiveGramAngle();
			}
		});
		finalAngleButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeDelay();
				calculateData();
				bindFinalData();

			}
		});
		instructionsButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View popupView = layoutInflater.inflate(
						R.layout.instructions_main, null);
				final PopupWindow popupWindow = new PopupWindow(popupView,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

				Button btnDismiss = (Button) popupView
						.findViewById(R.id.dismiss);
				btnDismiss.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						popupWindow.dismiss();
					}
				});
				popupWindow.showAtLocation(arg0, Gravity.CENTER, 0, 0);
			}
		});
	}
	/** Unregisters the listeners of the sensors */
	private void unregisterListeners() {
		mSensorManager.unregisterListener(this);
	}
	private void timeDelay() {
		try {
            synchronized(this){
                // Wait given period of time or exit on touch
                wait(1000);
            }
        }
        catch(InterruptedException ex){                    
        }
	}
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
}


