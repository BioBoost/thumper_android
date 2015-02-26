package be.vives.thumper;

import be.vives.thumper.communication.channel.ThumperCommunicationChannel;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppPreferenceActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Load the preferences from the xml file
		addPreferencesFromResource(R.xml.app_preferences);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// Kill current instance of comm channel to make sure latest type is used
		ThumperCommunicationChannel.killInstance();
	}
}
