package be.vives.thumper;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.codeandmagic.android.gauge.GaugeView;

public class ThumperControlActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

	private GaugeView batteryVoltageGauge;
	private GaugeView speedGauge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thumper_control);
		batteryVoltageGauge = (GaugeView)findViewById(R.id.batteryVoltageGauge);
		batteryVoltageGauge.setTargetValue(15);
		speedGauge = (GaugeView)findViewById(R.id.speedGauge);
		speedGauge.setTargetValue(0);
		((SeekBar)findViewById(R.id.speed)).setOnSeekBarChangeListener(this);
		((SeekBar)findViewById(R.id.speed)).setProgress(0);
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
		//((TextView)findViewById(R.id.txtBatteryVoltage)).setText(status.getBatteryVoltage() + "V");
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    // get masked (not specific to a pointer) action
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
		speedGauge.setTargetValue(((SeekBar)findViewById(R.id.speed)).getProgress());
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
