package be.vives.thumper;

import android.content.Context;

public abstract class ThumperCommunicationChannel {
	public abstract void getThumperStatus(Context context, IThumperStatusReady callback);
}
