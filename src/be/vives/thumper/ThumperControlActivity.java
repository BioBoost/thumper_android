package be.vives.thumper;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import be.vives.thumper.communication.channel.TcpCommunicationChannel;
import be.vives.thumper.communication.channel.ThumperCommunicationChannel;
import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.Side;
import be.vives.thumper.trex.ThumperCommand;
import be.vives.thumper.trex.ThumperStatus;

import org.codeandmagic.android.gauge.GaugeView;

public class ThumperControlActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

	private static final String TAG = "ManualThumperControl";

	private GaugeView batteryVoltageGauge;
	private GaugeView speedGauge;
	private SeekBar speedControl;

	private static final double MAX_BATTERY_VOLTAGE = 12;
	private static final double MIN_BATTERY_VOLTAGE = 7;
	private static final int MAX_SPEED = 255;
	
	private ThumperCommunicationChannel commChannel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thumper_control);

		batteryVoltageGauge = (GaugeView)findViewById(R.id.batteryVoltageGauge);
		speedGauge = (GaugeView)findViewById(R.id.speedGauge);
		speedControl = ((SeekBar)findViewById(R.id.speed));
		speedControl.setOnSeekBarChangeListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Set speed to 0
		speedControl.setProgress(0);
		speedGauge.setTargetValue(0);
		
		// Setup TCP communication channel with thumper
		commChannel = new TcpCommunicationChannel(this);
		
		// Get current status of thumper
		getThumperStatus();
	}
	
	private void getThumperStatus() {
		commChannel.getThumperStatus(getApplicationContext(), new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				} else {
					Log.d(TAG, "Status message nil");
				}
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Close the communication channel
		commChannel.close();
	}

	private void populateStatusView(ThumperStatus status) {		
		// Battery voltage
		double voltage = status.getBatteryVoltage();
		Log.i(TAG, "Battery voltage = " + voltage);
		int percentage = (int)(100 * (voltage - MIN_BATTERY_VOLTAGE) / (MAX_BATTERY_VOLTAGE - MIN_BATTERY_VOLTAGE));
		batteryVoltageGauge.setTargetValue(percentage);
	}

	public void onForward(View v) {
		Log.i(TAG, "Forward");
		ThumperCommand command = new ThumperCommand();
		command.setMotorSpeed(Side.LEFT, (int)((MAX_SPEED * speedControl.getProgress())/100));
		command.setMotorSpeed(Side.RIGHT, (int)((MAX_SPEED * speedControl.getProgress())/100));
		commChannel.sendThumperCommand(this, command, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				}
			}
		});
	}

	public void onStop(View v) {
		Log.i(TAG, "Stop");
		ThumperCommand command = new ThumperCommand();
		command.setMotorSpeed(Side.LEFT, 0);
		command.setMotorSpeed(Side.RIGHT, 0);
		commChannel.sendThumperCommand(this, command, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
				if (status != null) {
					populateStatusView(status);
				}
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    // Get masked (not specific to a pointer) action
	    int action = MotionEventCompat.getActionMasked(event);

	    // Get the index of the pointer associated with the action.
	    int index = MotionEventCompat.getActionIndex(event);

	    // Get location of hold controls
        int[][] resources = new int[3][3];
        resources[0][0] = R.id.imgForwardLeft;
        resources[0][1] = R.id.imgForward;
        resources[0][2] = R.id.imgForwardRight;
        resources[1][0] = R.id.imgLeft;
        resources[1][1] = R.id.imgStop;
        resources[1][2] = R.id.imgRight;
        resources[2][0] = R.id.imgReverseLeft;
        resources[2][1] = R.id.imgReverse;
        resources[2][2] = R.id.imgReverseRight;

        int[][] drawables_not_pressed = new int[3][3];
        drawables_not_pressed[0][0] = R.drawable.drive_control_forward_left;
        drawables_not_pressed[0][1] = R.drawable.drive_control_forward;
        drawables_not_pressed[0][2] = R.drawable.drive_control_forward_right;
        drawables_not_pressed[1][0] = R.drawable.drive_control_left;
        drawables_not_pressed[1][1] = R.drawable.drive_control_stopped;
        drawables_not_pressed[1][2] = R.drawable.drive_control_right;
        drawables_not_pressed[2][0] = R.drawable.drive_control_reverse_left;
        drawables_not_pressed[2][1] = R.drawable.drive_control_reverse;
        drawables_not_pressed[2][2] = R.drawable.drive_control_reverse_right;

        int[][] drawables_pressed = new int[3][3];
        drawables_pressed[0][0] = R.drawable.drive_control_forward_left_held;
        drawables_pressed[0][1] = R.drawable.drive_control_forward_held;
        drawables_pressed[0][2] = R.drawable.drive_control_forward_right_held;
        drawables_pressed[1][0] = R.drawable.drive_control_left_held;
        drawables_pressed[1][1] = R.drawable.drive_control_stop;
        drawables_pressed[1][2] = R.drawable.drive_control_right_held;
        drawables_pressed[2][0] = R.drawable.drive_control_reverse_left_held;
        drawables_pressed[2][1] = R.drawable.drive_control_reverse_held;
        drawables_pressed[2][2] = R.drawable.drive_control_reverse_right_held;

        ImageView[][] controls = new ImageView[3][3];
        Rect rects[][] = new Rect[3][3];

        int[] coord = new int[2];
        for (int row = 0; row < 3; row++) {
        	for (int col = 0; col < 3; col++) {
        		controls[row][col] = ((ImageView)findViewById(resources[row][col]));
        		controls[row][col].getLocationOnScreen(coord);
        		rects[row][col] = new Rect(coord[0], coord[1], coord[0] + controls[row][col].getWidth(), coord[1] + controls[row][col].getHeight());
        	}
        }

        // Get current pointer position
        int xPos = (int)MotionEventCompat.getX(event, index);
        int yPos = (int)MotionEventCompat.getY(event, index);

        // Determine which hold controls are pressed and released
	    switch (action) {
	        case MotionEvent.ACTION_DOWN:	// Single pointer
	        case MotionEvent.ACTION_MOVE:
	            for (int row = 0; row < 3; row++) {
	            	for (int col = 0; col < 3; col++) {
	            		if (rects[row][col].contains(xPos, yPos)) {
	            			controls[row][col].setImageResource(drawables_pressed[row][col]);
	    	            } else {
	    	            	controls[row][col].setImageResource(drawables_not_pressed[row][col]);
	    	            }
	            	}
	            }
	            break;
	        case MotionEvent.ACTION_UP:		// Single pointer
	            for (int row = 0; row < 3; row++) {
	            	for (int col = 0; col < 3; col++) {
	            		if (rects[row][col].contains(xPos, yPos)) {
	            			controls[row][col].setImageResource(drawables_not_pressed[row][col]);
	    	            }
	            	}
	            }
	        	break;
	    }

	    return true;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		speedGauge.setTargetValue(speedControl.getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
}
