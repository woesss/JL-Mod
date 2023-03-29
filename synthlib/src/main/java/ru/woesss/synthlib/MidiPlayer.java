package ru.woesss.synthlib;

import android.media.MediaPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MidiPlayer {
	private long handle;
	private ExecutorService executor;
	private Runnable stopCallback;

	public MidiPlayer(String sfPath) {
		handle = nInit(sfPath);
	}

	public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
		executor = Executors.newSingleThreadExecutor();
		stopCallback = () -> {
			if (MidiPlayer.this.nJoin(handle)) {
				listener.onCompletion(null);
			}
		};
	}

	public void start() {
		nPlay(handle);
		executor.execute(stopCallback);
	}

	public void reset() {
		nReset(handle);
	}

	public void stop() {
		nStop(handle);
	}

	public long getDuration() {
		return nGetDuration(handle);
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public void setLoopCount(int count) {
		nSetLoopCount(handle, count);
	}

	public long getMediaTime() {
		return nGetMediaTime(handle);
	}

	public long setMediaTime(long now) {
		return nSetMediaTime(handle, now);
	}

	public void release() {
		if (handle == 0) {
			return;
		}
		executor.shutdownNow();
		executor = null;
		nFinalize(handle);
		handle = 0;
	}

	public void setDataSource(String path) {
		nSetDataSource(handle, path);
	}

	public void setVolume(float left, float right) {
		nSetVolume(handle, left, right);
	}

	private native long nInit(String sfPath);

	private native boolean nPlay(long handle);

	private native void nStop(long handle);

	private native void nSetLoopCount(long handle, int count);

	private native void nReset(long handle);

	private native boolean nJoin(long handle);

	private native long nGetDuration(long handle);

	private native void nFinalize(long handle);

	private native long nSetMediaTime(long handle, long now);

	private native long nGetMediaTime(long handle);

	private native void nSetDataSource(long handle, String path);

	private native void nSetVolume(long handle, float left, float right);

	static {
		System.loadLibrary("synthlib");
	}
}
