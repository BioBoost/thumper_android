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
	
	public void onForward(View v) {
		ThumperCommunicationChannel thumper = new NodeJsCommunicationChannel();
		ThumperCommand command = new ThumperCommand();
		command.setMotorSpeed(Side.LEFT, 50);
		command.setMotorSpeed(Side.RIGHT, 50);
		thumper.sendThumperCommand(this, command, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				}
			}
		});
	}
	
	public void onStop(View v) {
		ThumperCommunicationChannel thumper = new NodeJsCommunicationChannel();
		ThumperCommand command = new ThumperCommand();
		command.setMotorSpeed(Side.LEFT, 0);
		command.setMotorSpeed(Side.RIGHT, 0);
		thumper.sendThumperCommand(this, command, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				}
			}
		});
	}
}
