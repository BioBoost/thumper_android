package be.vives.thumper;

import be.vives.thumper.communication.channel.TcpCommunicationChannel;
import be.vives.thumper.communication.channel.ThumperCommunicationChannel;
import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.Side;
import be.vives.thumper.trex.ThumperCommand;
import be.vives.thumper.trex.ThumperStatus;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

public class OrientationControlActivity extends Activity implements OrientationChangeListener {

	private Orientation mOrientation;
	private boolean leftIsHeld;
	private boolean rightIsHeld;
	
	private ThumperCommunicationChannel commChannel;

	private long lastTimeUpdate;
	private int refreshDelay;
	private boolean stopped;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_orientation_control);

	    mOrientation = new Orientation((SensorManager) getSystemService(Activity.SENSOR_SERVICE), getWindow().getWindowManager(), this);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

	    // get masked (not specific to a pointer) action
	    int action = MotionEventCompat.getActionMasked(event);
        Log.v("HOLD", actionToString(action));
	   
	    // Get the index of the pointer associated with the action.
	    int index = MotionEventCompat.getActionIndex(event);
	    
	    // Get location of hold controls
        ImageView left_hold = ((ImageView)findViewById(R.id.hold_left));
        ImageView right_hold = ((ImageView)findViewById(R.id.hold_right));

        int[] l = new int[2];
        left_hold.getLocationOnScreen(l);
        Rect rectLeft = new Rect(l[0], l[1], l[0] + left_hold.getWidth(), l[1] + left_hold.getHeight());
        
        right_hold.getLocationOnScreen(l);
        Rect rectRight = new Rect(l[0], l[1], l[0] + right_hold.getWidth(), l[1] + right_hold.getHeight());
        
        // Get current pointer position
        int xPos = (int)MotionEventCompat.getX(event, index);
        int yPos = (int)MotionEventCompat.getY(event, index);
        
        // Determine which hold controls are pressed and released
	    switch (action) {
	        case MotionEvent.ACTION_DOWN:	// Single pointer
	            if (rectLeft.contains(xPos, yPos)) {
	            	leftIsHeld = true;
	            } else if (rectRight.contains(xPos, yPos)) {
	            	rightIsHeld = true;
	            }
	            break;
	        case MotionEvent.ACTION_UP:		// Single pointer
            	leftIsHeld = false;
            	rightIsHeld = false;
	        	break;
	        case MotionEvent.ACTION_MOVE:
	            if (rectLeft.contains(xPos, yPos)) {
	            	leftIsHeld = true;
	            } else if (rectRight.contains(xPos, yPos)) {
	            	rightIsHeld = true;
	            }
	        	break;
	        case MotionEvent.ACTION_POINTER_DOWN:	// Multi pointer goes down
	            if (rectLeft.contains(xPos, yPos)) {
	            	leftIsHeld = true;
	            } else if (rectRight.contains(xPos, yPos)) {
	            	rightIsHeld = true;
	            }
	        	break;
	        case MotionEvent.ACTION_POINTER_UP:		// Multi pointer goes up
	            if (rectLeft.contains(xPos, yPos)) {
	            	leftIsHeld = false;
	            } else if (rectRight.contains(xPos, yPos)) {
	            	rightIsHeld = false;
	            }
	        	break;
	    }
        
	    // Visual indication
        if (leftIsHeld) {
        	left_hold.setImageResource(R.drawable.ic_hold_true);
        } else {
        	left_hold.setImageResource(R.drawable.ic_hold_false);
        }
        
        if (rightIsHeld) {
        	right_hold.setImageResource(R.drawable.ic_hold_true);
        } else {
        	right_hold.setImageResource(R.drawable.ic_hold_false);
        }

	    return true;
	} 
	
	// Given an action int, returns a string description
	public static String actionToString(int action) {
	    switch (action) {
	                
	        case MotionEvent.ACTION_DOWN: return "Down";
	        case MotionEvent.ACTION_MOVE: return "Move";
	        case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
	        case MotionEvent.ACTION_UP: return "Up";
	        case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
	        case MotionEvent.ACTION_OUTSIDE: return "Outside";
	        case MotionEvent.ACTION_CANCEL: return "Cancel";
	    }
	    return "";
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Setup TCP communication channel with thumper
		commChannel = new TcpCommunicationChannel(this);

	    // Clear left and right indicators
	    leftIsHeld = false;
	    rightIsHeld = false;
	    stopped = true;		
		
		mOrientation.startListening(this);
		
		SharedPreferences appPrefs = getApplicationContext().getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
		refreshDelay = Integer.parseInt(appPrefs.getString("automatic_drive_refresh_time", "250"));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mOrientation.stopListening();
		
		// Close the communication channel
		commChannel.close();
		
		sendStop();
		stopped = true;
		((TextView)this.findViewById(R.id.txtStopped)).setText("Stopped");
	}
	
	private void sendStop() {
		ThumperCommand command = new ThumperCommand();
		command.setMotorSpeed(Side.LEFT, 0);
		command.setMotorSpeed(Side.RIGHT, 0);
		commChannel.sendThumperCommand(this, command, new IThumperStatusReady() {
			@Override
			public void onStatusReady(ThumperStatus status) {
			}
		});
	}
	
	@Override
	public void onOrientationChanged(float pitch, float roll) {
		if (leftIsHeld && rightIsHeld) {
			// Indicate driving control active
			((TextView)this.findViewById(R.id.txtStopped)).setText("Driving");
			stopped = false;
			
			((TextView)this.findViewById(R.id.txtPitch)).setText(pitch + "");
			((TextView)this.findViewById(R.id.txtRoll)).setText(roll + "");
			
			// We need to rescale the pitch to our speed range first

			// Device placement 
			int MAX_PITCH = -60;
			int MIN_PITCH = -20;
			int PITCH_RANGE = MAX_PITCH - MIN_PITCH;

			// Actual speed
			int MAX_SPEED = 150;
			int MIN_SPEED = -150;
			int SPEED_RANGE = MAX_SPEED - MIN_SPEED;

			// Limit pitch
			pitch = Math.max(MAX_PITCH, pitch);
			pitch = Math.min(MIN_PITCH, pitch);
			
			double pitch_mult = ((pitch - MIN_PITCH) / PITCH_RANGE);		// Between 0 and 1
			int base_speed = (int)((pitch_mult * SPEED_RANGE) + MIN_SPEED);
			
			// Now we need to add turn control
			
			// Device placement 
			int MAX_ROLL = 40;
			int MIN_ROLL = -40;
			int ROLL_RANGE = MAX_ROLL - MIN_ROLL;

			// Speed control for thumper itself
			int MAX_TURN = 100;
			int MIN_TURN = -100;
			int TURN_RANGE = MAX_TURN - MIN_TURN;

			// Limit roll
			roll = Math.min(MAX_ROLL, roll);
			roll = Math.max(MIN_ROLL, roll);
			
			double roll_mult = ((roll - MIN_ROLL) / ROLL_RANGE);			// Between 0 and 1
			int base_turn = (int)((roll_mult * TURN_RANGE) + MIN_TURN);
			
			int left_speed = base_speed - base_turn;
			int right_speed = base_speed + base_turn;
			
			// Make sure limits are not exceeded
			left_speed = Math.min(MAX_SPEED, left_speed);
			left_speed = Math.max(MIN_SPEED, left_speed);
			right_speed = Math.min(MAX_SPEED, right_speed);
			right_speed = Math.max(MIN_SPEED, right_speed);

			((TextView)this.findViewById(R.id.txtLeftTurnSpeed)).setText(left_speed + "");
			((TextView)this.findViewById(R.id.txtRightTurnSpeed)).setText(right_speed + "");
			
			long currentTime = System.currentTimeMillis();
			long time_delta = currentTime - lastTimeUpdate;
			
			if (time_delta >= refreshDelay) {
				
				ThumperCommand command = new ThumperCommand();
				command.setMotorSpeed(Side.LEFT, left_speed);
				command.setMotorSpeed(Side.RIGHT, right_speed);
				commChannel.sendThumperCommand(this, command, new IThumperStatusReady() {
					@Override
					public void onStatusReady(ThumperStatus status) {
					}
				});
				
				lastTimeUpdate = System.currentTimeMillis();
			}
			
		} else {
			if (!stopped) {
				sendStop();
				stopped = true;
				((TextView)this.findViewById(R.id.txtStopped)).setText("Stopped");
			} // else maybe status update every x seconds ?
		}
	}
}
