package be.vives.thumper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onThumperControl(View v) {
    	startActivity(new Intent(this, ThumperControlActivity.class));
    }
    
    public void onSettings(View v) {
    	startActivity(new Intent(this, AppPreferenceActivity.class));
    }
}
