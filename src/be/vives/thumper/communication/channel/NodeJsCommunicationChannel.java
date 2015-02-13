package be.vives.thumper.communication.channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.ThumperCommand;
import be.vives.thumper.trex.ThumperStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class NodeJsCommunicationChannel extends ThumperCommunicationChannel{

	@Override
	public void getThumperStatus(Context context, IThumperStatusReady callback) {
		new ThumperStatusFetch(context, callback).execute();
	}
	
	@Override
	public void sendThumperCommand(Context context, ThumperCommand command, IThumperStatusReady callback) {
		new ThumperCommandSend(context, command, callback).execute();
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void close() {}
	
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
	
	private class ThumperCommandSend extends AsyncTask<Void, Void, ThumperStatus> {
		
		private static final String TAG = "ThumperCommandSend";
		
		// Needed for fetching prefs
		private Context context;
		
		// Callback when result is ready
		private IThumperStatusReady callback;
		
		// Thumper command to send
		private ThumperCommand command;
		
		public ThumperCommandSend(Context context, ThumperCommand command, IThumperStatusReady callback) {
			this.context = context;
			this.callback = callback;
			this.command = command;
		}		
		
        @Override
        protected ThumperStatus doInBackground(Void... params) {
    		SharedPreferences appPrefs = context.getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
    		String ip = appPrefs.getString("nodejs_server_ip", "127.0.0.1");
    		String port = appPrefs.getString("nodejs_server_port", "1337");
    		String uri = "http://" + ip + ":" + port;
    		
    		Log.v(TAG, "Sending command to thumper @ " + uri);
        	
    		ThumperStatus status = null;
    		try {
    			// Do a HTTP request to the server
    			HttpPost httppost = new HttpPost(uri);
    			HttpClient httpclient = new DefaultHttpClient();
    			
    	        // Add our json Thumper command
    			httppost.setEntity(new StringEntity(command.toJson()));
    			
    			//sets a request header so the page receving the request
    		    //will know what to do with it
    			httppost.setHeader("Accept", "application/json");
    			httppost.setHeader("Content-type", "application/json");
    			
    			// Execute HTTP Post Request
    	        HttpResponse response = httpclient.execute(httppost);
    			
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

