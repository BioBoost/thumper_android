package be.vives.thumper.communication.channel;

import android.content.Context;
import be.vives.thumper.trex.IThumperStatusReady;
import be.vives.thumper.trex.ThumperCommand;

public class FakeCommunicationChannel implements ICommunicationChannel {

	@Override
	public void getThumperStatus(Context context, IThumperStatusReady callback) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendThumperCommand(Context context, ThumperCommand command,
			IThumperStatusReady callback) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isPersistent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}
}
