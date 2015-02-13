package be.vives.thumper.trex;

import org.json.JSONException;
import org.json.JSONObject;

public class ThumperStatus {
	
	private double batteryVoltage;
	
	public ThumperStatus() {
	}
	
	public double getBatteryVoltage() {
		return this.batteryVoltage;
	}
	
	public void setBatteryVoltage(double volt) {
		this.batteryVoltage = volt;
	}
	
	/*
	 * Returns a request for status string in JSON format
	 */
	public static String getRequestSring() {
		return "{ \"request\":\"status\" }\r\n\r\n";
	}
	
	public void fromJson(String json) throws JSONException {
		JSONObject jsonObj = new JSONObject(json);
		
		// Battery Voltage
		double batteryvoltage = Double.parseDouble(jsonObj.getString("battery_voltage"));
		this.setBatteryVoltage(batteryvoltage);
	}
}
