package be.vives.thumper.communication.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.ThumperCommand;
import be.vives.thumper.trex.ThumperStatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class TcpCommunicationChannel extends ThumperCommunicationChannel {
	
	private static final String TAG = "TcpCommunicationChannel";
		
	private PrintWriter mBufferOut;
	private BufferedReader mBufferIn;
	private Socket socket;
	
	public TcpCommunicationChannel(Context context) {		
		// Open socket
		open(context);
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public boolean isConnected() {
    	if (socket != null && mBufferOut != null && mBufferIn != null) {
    		return true;
    	} else {
    		return false;
    	}
	}

	@Override
	public void close() {
		try {
			socket.close();
			mBufferOut.close();
			mBufferIn.close();
		} catch (IOException e) {
			Log.e(TAG, "Socket close giving error " + e);
		}
	}
	
	/*
	 * Open TCP socket to the TRex master app
	 */
	private void open(Context context) {
		SharedPreferences appPrefs = context.getSharedPreferences("be.vives.thumper_preferences", Context.MODE_PRIVATE);
		String ip = appPrefs.getString("tcp_server_ip", "127.0.0.1");
		String port = appPrefs.getString("tcp_server_port", "1337");
		
		Log.v(TAG, "Setting up socket with TCP TRex master");
		try {
            // Create socket to make connection with the server
            InetAddress serverAddr = InetAddress.getByName(ip);
            socket = new Socket(serverAddr, Integer.parseInt(port));
			
            try {                	
            	// Create reader and writer for communication
            	mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            	mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (Exception e) {
                Log.e(TAG, "S: Error", e);
                
                // Close socket if anything fails
                socket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);
        }
	}
	
	@Override
	public void getThumperStatus(Context context, IThumperStatusReady callback) {
		new ThumperStatusFetch(context, callback).execute();
	}
	
	@Override
	public void sendThumperCommand(Context context, ThumperCommand command, IThumperStatusReady callback) {
		new ThumperCommandSend(context, command, callback).execute();
	}
	
	private class ThumperStatusFetch extends AsyncTask<Void, Void, ThumperStatus> {
		
		private static final String TAG = "ThumperStatusFetch";
		
		// Callback when result is ready
		private IThumperStatusReady callback;
		private Context context;
		
		public ThumperStatusFetch(Context context, IThumperStatusReady callback) {
			this.callback = callback;
			this.context = context;
		}		
		
        @Override
        protected ThumperStatus doInBackground(Void... params) {
    		// Check connection
    		if (!isConnected()) {
    			open(context);
    		}

    		ThumperStatus status = null;
    		if (isConnected()) {   	
                try {                	
                    // Send request for status to TCP server
                	String request = ThumperStatus.getRequestSring();
                	if (mBufferOut != null && !mBufferOut.checkError()) {
                    	mBufferOut.print(request);
                        mBufferOut.flush();
                	} else {
                		Log.e(TAG, "mBufferOut giving error");
                	}

                    // Read response from TCP server
                    String statusJson = mBufferIn.readLine();
                    if (statusJson != null) {
        				status = new ThumperStatus();
        				status.fromJson(statusJson);
        				Log.v(TAG, "Status converted from json");
                    } else {
                    	Log.e(TAG, "Status response failed");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "S: Error", e);
                }
        		
        	} else {
        		Log.e(TAG, "Cannot fetch status from thumper, not connected");
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
    		// Check connection
    		if (!isConnected()) {
    			open(context);
    		}

    		ThumperStatus status = null;
    		if (isConnected()) {   	
        		Log.v(TAG, "Sending command to thumper");
                try {                	
                    // Send command to TCP server
                	if (mBufferOut != null && !mBufferOut.checkError()) {
                    	mBufferOut.print(command.toJson());
                        mBufferOut.flush();
                	} else {
                		Log.e(TAG, "mBufferOut giving error");
                	}

                    // Read response from TCP server
                    String statusJson = mBufferIn.readLine();
                    if (statusJson != null) {
        				status = new ThumperStatus();
        				status.fromJson(statusJson);
        				Log.v(TAG, "Status converted from json");
                    } else {
                    	Log.e(TAG, "Status response failed");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "S: Error", e);
                }
        		
        	} else {
        		Log.e(TAG, "Cannot fetch status from thumper, not connected");
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
