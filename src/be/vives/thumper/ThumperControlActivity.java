package be.vives.thumper;

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
		ThumperCommunicationChannel thumper = new NodeJsCommunicationChannel();
		thumper.getThumperStatus(this, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				}
			}
		});
	}
	
	private void populateStatusView(ThumperStatus status) {
		((TextView)findViewById(R.id.txtBatteryVoltage)).setText(status.getBatteryVoltage() + "V");
	}
}
