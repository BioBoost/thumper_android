package be.vives.thumper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import be.vives.thumper.communication.channel.ICommunicationChannel;
import be.vives.thumper.communication.channel.ThumperCommunicationChannel;
import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.Side;
import be.vives.thumper.trex.ThumperCommand;
import be.vives.thumper.trex.ThumperStatus;

import org.codeandmagic.android.gauge.GaugeView;

public class ThumperControlActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

	private static final String TAG = "ManualThumperControl";

	private SeekBar speedControl;
	private GaugeView gaugeLeft;
	private GaugeView gaugeRight;

	private static final int MAX_SPEED = 200;
	private static final int TURN_SPEED = 50;
	
	private boolean heldButtons[];

	private static int FORWARD = 0;
	private static int REVERSE = 1;
	private static int LEFT = 2;
	private static int RIGHT = 3;	
	
	private ICommunicationChannel commChannel;

	private long lastTimeUpdate;
	private int refreshDelay;
	private boolean stopped;
	
	private static double BATTERY_THRESHOLD = 7.0;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thumper_control);

		speedControl = ((SeekBar)findViewById(R.id.speed));
		speedControl.setOnSeekBarChangeListener(this);
		speedControl.setMax(MAX_SPEED);

		gaugeLeft = ((GaugeView)findViewById(R.id.speedLeftGauge));
		gaugeRight = ((GaugeView)findViewById(R.id.speedRightGauge));
		
		heldButtons = new boolean[4];
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences appPrefs = getApplicationContext().getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
		refreshDelay = Integer.parseInt(appPrefs.getString("automatic_drive_refresh_time", "500"));
		
		// Set speed to 0
		speedControl.setProgress(0);
		gaugeLeft.setTargetValue(0);
		gaugeRight.setTargetValue(0);
	    stopped = true;
	    setDrivingState();
	    lastTimeUpdate = 0;
		
		// Setup TCP communication channel with thumper
	    commChannel = ThumperCommunicationChannel.getInstance(this);
		
        for (int i = 0; i < 4; i++) {
    		heldButtons[i] = false;
        }
        
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
		
		// Clear all held controls
        for (int i = 0; i < 4; i++) {
    		heldButtons[i] = false;
        }
		
        // Make sure thumper is stopped
		sendStop();
		stopped = true;
		setDrivingState();
		
		// Close the communication channel
		commChannel.close();
	}
	
	private void sendStop() {
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

	private void populateStatusView(ThumperStatus status) {		
		// Battery Voltage
		double voltage = status.getBatteryVoltage();
		TextView view = (TextView)findViewById(R.id.txtBatteryVoltage);
		view.setText(voltage + "V");
		if (voltage < BATTERY_THRESHOLD) {
			view.setTextColor(Color.RED);
		} else {
			view.setTextColor(Color.GREEN);
		}	
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    // Get masked (not specific to a pointer) action
	    int action = MotionEventCompat.getActionMasked(event);

	    // Get the index of the pointer associated with the action.
	    int index = MotionEventCompat.getActionIndex(event);

	    // Get location of hold controls
        int[] resources = new int[4];
        resources[FORWARD] = R.id.imgForward;
        resources[LEFT] = R.id.imgLeft;
        resources[RIGHT] = R.id.imgRight;
        resources[REVERSE] = R.id.imgReverse;

        int[] drawables_not_pressed = new int[4];
        drawables_not_pressed[FORWARD] = R.drawable.drive_control_forward;
        drawables_not_pressed[LEFT] = R.drawable.drive_control_left;
        drawables_not_pressed[RIGHT] = R.drawable.drive_control_right;
        drawables_not_pressed[REVERSE] = R.drawable.drive_control_reverse;

        int[] drawables_pressed = new int[4];
        drawables_pressed[FORWARD] = R.drawable.drive_control_forward_held;
        drawables_pressed[LEFT] = R.drawable.drive_control_left_held;
        drawables_pressed[RIGHT] = R.drawable.drive_control_right_held;
        drawables_pressed[REVERSE] = R.drawable.drive_control_reverse_held;

        ImageView[] controls = new ImageView[4];
        Rect rects[] = new Rect[4];

        int[] coord = new int[2];
        for (int i = 0; i < 4; i++) {
    		controls[i] = ((ImageView)findViewById(resources[i]));
    		controls[i].getLocationOnScreen(coord);
    		rects[i] = new Rect(coord[0], coord[1], coord[0] + controls[i].getWidth(), coord[1] + controls[i].getHeight());
        }
        
        // Get current pointer position
        int xPos = (int)MotionEventCompat.getX(event, index);
        int yPos = (int)MotionEventCompat.getY(event, index);
        
        // Determine which hold controls are pressed and released
	    switch (action) {
	        case MotionEvent.ACTION_DOWN:	// Single pointer
	            for (int i = 0; i < 4; i++) {
            		if (rects[i].contains(xPos, yPos)) {
            			//controls[i].setImageResource(drawables_pressed[i]);
            			heldButtons[i] = true;
    	            }
	            }
	            break;
	        case MotionEvent.ACTION_UP:		// Single pointer
	            for (int i = 0; i < 4; i++) {
            		heldButtons[i] = false;
	            }
	        	break;
	        case MotionEvent.ACTION_MOVE:
	            for (int i = 0; i < 4; i++) {
            		if (rects[i].contains(xPos, yPos)) {
            			heldButtons[i] = true;
    	            }
	            }
	        	break;
	        case MotionEvent.ACTION_POINTER_DOWN:	// Multi pointer goes down
	            for (int i = 0; i < 4; i++) {
            		if (rects[i].contains(xPos, yPos)) {
            			heldButtons[i] = true;
    	            }
	            }
	        	break;
	        case MotionEvent.ACTION_POINTER_UP:		// Multi pointer goes up
	            for (int i = 0; i < 4; i++) {
            		if (rects[i].contains(xPos, yPos)) {
            			heldButtons[i] = false;
    	            }
	            }
	        	break;
	    }
        
	    // Visual indication
        for (int i = 0; i < 4; i++) {
    		if (heldButtons[i]) {
    			controls[i].setImageResource(drawables_pressed[i]);
            } else {
            	controls[i].setImageResource(drawables_not_pressed[i]);
            }
        }
        
        driveThumper();
	    
	    return true;
	}
	
	private void driveThumper() {
		// Check if any button is held
		boolean anyIsHeld = false;
        for (int i = 0; i < 4; i++) {
    		if (heldButtons[i]) {
    			anyIsHeld = true;
            }
        }
		
		if (anyIsHeld) {
			stopped = false;
			
			long currentTime = System.currentTimeMillis();
			long time_delta = currentTime - lastTimeUpdate;
			
			if (time_delta >= refreshDelay) {
				int base_speed = speedControl.getProgress();
				
				int left_speed = 0;
				int right_speed = 0;			

				if (heldButtons[FORWARD] || heldButtons[REVERSE]) {
					left_speed = base_speed;
					right_speed = base_speed;
				}
				
				if (heldButtons[LEFT]) {
					right_speed = right_speed + TURN_SPEED;
					left_speed = left_speed - TURN_SPEED;
				}
				
				if (heldButtons[RIGHT]) {
					right_speed = right_speed - TURN_SPEED;
					left_speed = left_speed + TURN_SPEED;
				}	

				if (heldButtons[REVERSE]) {
					right_speed = -right_speed;
					left_speed = -left_speed;
				}
				
				// Limit drive speed
				left_speed = Math.min(MAX_SPEED, left_speed);	
				right_speed = Math.min(MAX_SPEED, right_speed);	
				left_speed = Math.max(-MAX_SPEED, left_speed);	
				right_speed = Math.max(-MAX_SPEED, right_speed);	
				
				gaugeLeft.setTargetValue(Math.abs(100*left_speed/MAX_SPEED));
				gaugeRight.setTargetValue(Math.abs(100*right_speed/MAX_SPEED));
								
				ThumperCommand command = new ThumperCommand();
				command.setMotorSpeed(Side.LEFT, left_speed);
				command.setMotorSpeed(Side.RIGHT, right_speed);
				commChannel.sendThumperCommand(this, command, new IThumperStatusReady() {
					@Override
					public void onStatusReady(ThumperStatus status) {
						if (status != null) {
							double voltage = status.getBatteryVoltage();
							TextView view = (TextView)findViewById(R.id.txtBatteryVoltage);
							view.setText(voltage + "V");
							if (voltage < BATTERY_THRESHOLD) {
								view.setTextColor(Color.RED);
							} else {
								view.setTextColor(Color.GREEN);
							}	
						}
					}
				});
				
				lastTimeUpdate = System.currentTimeMillis();
			}
		} else {
			sendStop();
			stopped = true;
			((GaugeView)findViewById(R.id.speedLeftGauge)).setTargetValue(0);
			((GaugeView)findViewById(R.id.speedRightGauge)).setTargetValue(0);
		}
		setDrivingState();
	}
	
	private void setDrivingState() {
		if (stopped) {
			((TextView)this.findViewById(R.id.txtIsStopped)).setText("Stopped");
			((TextView)this.findViewById(R.id.txtIsStopped)).setTextColor(Color.RED);
		} else {
			((TextView)this.findViewById(R.id.txtIsStopped)).setText("Driving");
			((TextView)this.findViewById(R.id.txtIsStopped)).setTextColor(Color.GREEN);
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
}
