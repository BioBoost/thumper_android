package be.vives.thumper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class NodeJsCommunicationChannel extends ThumperCommunicationChannel{

	@Override
	public void getThumperStatus(Context context, IThumperStatusReady callback) {
		new ThumperStatusFetch(context, callback).execute();
	}
	
	private class ThumperStatusFetch extends AsyncTask<Void, Void, ThumperStatus> {
		
		private static final String TAG = "ThumperStatusFetch";
		
		// Needed for fetching prefs
		private Context context;
		
		// Callback when result is ready
		private IThumperStatusReady callback;
		
		public ThumperStatusFetch(Context context, IThumperStatusReady callback) {
			this.context = context;
			this.callback = callback;
		}		
		
        @Override
        protected ThumperStatus doInBackground(Void... params) {
    		SharedPreferences appPrefs = context.getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
    		String ip = appPrefs.getString("nodejs_server_ip", "127.0.0.1");
    		String port = appPrefs.getString("nodejs_server_port", "1337");
    		String uri = "http://" + ip + ":" + port;
    		
    		Log.v(TAG, "Fetching status from thumper @ " + uri);
        	
    		ThumperStatus status = null;
    		try {
    			// Do a HTTP request to the server
    			HttpGet httpRequest = new HttpGet(uri);
    			HttpClient httpclient = new DefaultHttpClient();
    			HttpResponse response = httpclient.execute(httpRequest);

    			//  Check the response status
    			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    				String json = reader.readLine();
    				status = new ThumperStatus();
    				status.fromJson(json);
    	    		Log.v(TAG, "Status converted from json");
    			} else {
    	    		Log.v(TAG, "Server responded with status: " + response.getStatusLine().getStatusCode());
                }
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    		
    		return status;
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(ThumperStatus status) {
            if (this.callback != null) {
	    		Log.v(TAG, "Status ready for callback");
            	callback.onStatusReady(status);
            }
        }
    }

}

