package be.vives.thumper.communication.channel;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ThumperCommunicationChannel {
	private static ICommunicationChannel channel = null;

	public static ICommunicationChannel getInstance(Context context) {
		if (channel == null) {
			SharedPreferences appPrefs = context.getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
			String type = appPrefs.getString("type_of_communication_channel", "tcp_channel");
			
			if (type.equals("tcp_channel")) {
				channel = new TcpCommunicationChannel(context);
			} else if (type.equals("fake_channel")) {
				channel = new FakeCommunicationChannel();
			} else {
				Log.d("ThumperCommunicationChannel", "Unknown comm type: " + type);
			}
		}
		return channel;
	}
}
