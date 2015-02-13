package be.vives.thumper;

import android.content.Context;

public abstract class ThumperCommunicationChannel {
	public abstract void getThumperStatus(Context context, IThumperStatusReady callback);
	public abstract void sendThumperCommand(Context context, ThumperCommand command, IThumperStatusReady callback);
	public abstract boolean isPersistent();
	public abstract boolean isConnected();
	public abstract void close();
}
