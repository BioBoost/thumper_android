package be.vives.thumper.communication.channel;

import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.ThumperCommand;
import android.content.Context;

public abstract class ThumperCommunicationChannel {
	public abstract void getThumperStatus(Context context, IThumperStatusReady callback);
	public abstract void sendThumperCommand(Context context, ThumperCommand command, IThumperStatusReady callback);
	public abstract boolean isPersistent();
	public abstract boolean isConnected();
	public abstract void close();
}
