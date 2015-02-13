package be.vives.thumper.trex;

public class ThumperCommand {

	private int left_motor_speed;
	private int right_motor_speed;
	
	public ThumperCommand() {
		setMotorSpeed(Side.LEFT, 0);
		setMotorSpeed(Side.RIGHT, 0);
	}
	
	public int getMotorSpeed(Side side) {
		if (side == Side.LEFT) {
			return left_motor_speed;
		} else {
			return right_motor_speed;
		}
	}
	
	public void setMotorSpeed(Side side, int speed) {
		if (side == Side.LEFT) {
			this.left_motor_speed = speed;
		} else {
			this.right_motor_speed = speed;
		}
	}
	
	public String toJson() {
		return "{\"device_path\": \"/dev/i2c-1\"," +
				"\"i2c_address\": 7," +
				"\"pwm_frequency\": 6," +
				"\"motor_speed\": {\"left\": " + left_motor_speed + ", \"right\": " + right_motor_speed + "}," +
				"\"brake\": {\"left\": 0, \"right\": 0}," +
				"\"servos\": [0, 0, 0, 0, 0, 0]," +
				"\"accelero_meter_devibrate\": 50," +
				"\"impact_sensitivity\": 50," +
				"\"low_battery_voltage\": 550," +
				"\"i2c_slave_address\": 7," +
				"\"clock_frequency\": 0}";
	}
} 
