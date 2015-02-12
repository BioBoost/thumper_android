package be.vives.thumper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    
    public void onOrientation(View v) {
    	startActivity(new Intent(this, OrientationControlActivity.class));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.show_settings:
                startActivity(new Intent(this, AppPreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
