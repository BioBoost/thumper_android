package be.vives.thumper;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class OrientationControlActivity extends Activity implements OrientationChangeListener {

	private Orientation mOrientation;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orientation_control);

	    mOrientation = new Orientation((SensorManager) getSystemService(Activity.SENSOR_SERVICE), getWindow().getWindowManager());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mOrientation.startListening(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mOrientation.stopListening();
	}

	@Override
	public void onOrientationChanged(float pitch, float roll) {
		((TextView)this.findViewById(R.id.txtPitch)).setText(pitch + "");
		((TextView)this.findViewById(R.id.txtRoll)).setText(roll + "");
	}
}
