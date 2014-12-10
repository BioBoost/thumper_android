package be.vives.thumper;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ThumperControlActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thumper_control);
	}
	
	public void onRefresh(View v) {
		ThumperStatus status = this.getThumperStatus();
		populateStatusView(status);
	}
	
	private ThumperStatus getThumperStatus() {
		ThumperStatus status = new ThumperStatus();
		try {
			status.fromJson("{\"device_path\": \"/dev/i2c-1\", \"i2c_address\": 7, \"errors\": {\"start_byte\": false, \"pwm_frequency\": false, \"motor_speed\": false, \"servo_position\": false, \"impact_sensitivity\": false, \"low_battery\": false, \"i2c_address\": false, \"i2c_speed\": false}, \"battery_voltage\": 7.52, \"motor_current\": {\"left\": 2.15, \"right\": 5.71}, \"encoder_count\": {\"left\": 125, \"right\": 800}, \"accelero_meter\": [0, 125, 88], \"impact\": [0, 125, 88]}");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return status;
	}
	
	private void populateStatusView(ThumperStatus status) {
		((TextView)findViewById(R.id.txtBatteryVoltage)).setText(status.getBatteryVoltage() + "V");
	}
}
