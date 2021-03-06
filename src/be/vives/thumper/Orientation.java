package be.vives.thumper;

//Based on: https://github.com/kplatfoot/android-rotation-sensor-sample
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class Orientation implements SensorEventListener {

	private static final String TAG = "Orientation";
	private static final int SENSOR_DELAY_MICROS = 100 * 1000;	// 100ms
	
	private final SensorManager mSensorManager;
	private final Sensor mRotationSensor;
	private final WindowManager mWindowManager;
	
	private int mLastAccuracy;
	private OrientationChangeListener mListener;
	
	private Context context;
	
	public Orientation(SensorManager sensorManager, WindowManager windowManager, Context context) {
		mSensorManager = sensorManager;
		mWindowManager = windowManager;
		
		// Can be null if the sensor hardware is not available
		mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		
		this.context = context;
	}
	
	public void startListening(OrientationChangeListener listener) {
		if (mListener == listener) {
			return;
		}
		mListener = listener;
		if (mRotationSensor == null) {
			Log.w(TAG, "Rotation vector sensor not available; will not provide orientation data.");
			return;
		}

		SharedPreferences appPrefs = context.getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
		int delay = Integer.parseInt(appPrefs.getString("automatic_drive_refresh_time", "250"));
		
		mSensorManager.registerListener(this, mRotationSensor, delay*1000);
	}
	
	public void stopListening() {
		mSensorManager.unregisterListener(this);
		mListener = null;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (mLastAccuracy != accuracy) {
			mLastAccuracy = accuracy;
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (mListener == null) {
			return;
		}
		if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}
		if (event.sensor == mRotationSensor) {
			updateOrientation(event.values);
		}
	}
	
	private void updateOrientation(float[] rotationVector) {
		 float[] rotationMatrix = new float[9];
		 SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
		
		 // By default, remap the axes as if the front of the
		 // device screen was the instrument panel.
		 int worldAxisForDeviceAxisX = SensorManager.AXIS_X;
		 int worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
		
		 // Adjust the rotation matrix for the device orientation
		 int screenRotation = mWindowManager.getDefaultDisplay().getRotation();
		 if (screenRotation == Surface.ROTATION_0) {
			 worldAxisForDeviceAxisX = SensorManager.AXIS_X;
			 worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
		 } else if (screenRotation == Surface.ROTATION_90) {
			 worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
		   	worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
		 } else if (screenRotation == Surface.ROTATION_180) {
			 worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
			 worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
		 } else if (screenRotation == Surface.ROTATION_270) {
			 worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
			 worldAxisForDeviceAxisY = SensorManager.AXIS_X;
		 }
	
		 float[] adjustedRotationMatrix = new float[9];
		 SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);
		
		 // Transform rotation matrix into azimuth/pitch/roll
		 float[] orientation = new float[3];
		 SensorManager.getOrientation(adjustedRotationMatrix, orientation);
		
		 // Convert radians to degrees
		 float pitch = orientation[1] * -57;
		 float roll = orientation[2] * -57;
		
		 mListener.onOrientationChanged(pitch, roll);
	}
}

