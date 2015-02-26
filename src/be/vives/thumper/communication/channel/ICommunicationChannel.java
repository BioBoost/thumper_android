package be.vives.thumper.communication.channel;

import android.content.Context;
import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.ThumperCommand;

public interface ICommunicationChannel {
	public void getThumperStatus(Context context, IThumperStatusReady callback);
	public void sendThumperCommand(Context context, ThumperCommand command, IThumperStatusReady callback);
	public boolean isPersistent();
	public boolean isConnected();
	public void close();
}
