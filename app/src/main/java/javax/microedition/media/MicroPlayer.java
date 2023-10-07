/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2017-2020 Nikita Shakarun
 * Copyright 2020-2023 Yury Kharchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.media;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.amms.control.PanControl;
import javax.microedition.amms.control.audioeffect.EqualizerControl;
import javax.microedition.media.control.MetaDataControl;
import javax.microedition.media.control.ToneControl;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.media.protocol.DataSource;
import javax.microedition.media.tone.MidiToneConstants;
import javax.microedition.media.tone.ToneSequence;

import kotlin.io.FilesKt;
import ru.woesss.j2me.mmapi.FileCacheDataSource;
import ru.woesss.j2me.mmapi.protocol.device.DeviceMetaData;

class MicroPlayer extends BasePlayer implements MediaPlayer.OnCompletionListener,
		VolumeControl, PanControl, ToneControl {
	private static final String TAG = MicroPlayer.class.getSimpleName();

	protected final HashMap<String, Control> controls = new HashMap<>();
	protected final MediaPlayer player = new AndroidPlayer();
	protected final DataSource source;
	protected int state = UNREALIZED;

	private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r, "MidletPlayerCallback");
		thread.setUncaughtExceptionHandler((t, e) ->
				Log.e(t.getName(), "UncaughtException in " + t, e));
		return thread;
	});
	private final ArrayList<PlayerListener> listeners = new ArrayList<>();
	private final InternalMetaData metadata;

	private int loopCount = 1;
	private boolean mute = false;
	private int level = 100;
	private int pan;

	public MicroPlayer(String locator) throws IOException {
		if (!Manager.TONE_DEVICE_LOCATOR.equals(locator)) {
			throw new IllegalArgumentException();
		}
		source = new FileCacheDataSource("audio/x-tone-seq", "mid");
		controls.put(MidiToneConstants.TONE_CONTROL_FULL_NAME, this);
		metadata = new DeviceMetaData();
		init();
	}

	MicroPlayer(DataSource datasource) {
		source = datasource;
		metadata = new InternalMetaData();
		init();
	}

	private void init() {
		player.setOnCompletionListener(this);
		controls.put(VolumeControl.class.getName(), this);
		controls.put(PanControl.class.getName(), this);
		controls.put(MetaDataControl.class.getName(), metadata);
		controls.put(EqualizerControl.class.getName(), new InternalEqualizer());
	}

	@Override
	public Control getControl(String controlType) {
		checkRealized();
		if (!controlType.contains(".")) {
			controlType = "javax.microedition.media.control." + controlType;
		}
		return controls.get(controlType);
	}

	@Override
	public Control[] getControls() {
		checkRealized();
		return controls.values().toArray(new Control[0]);
	}

	@Override
	public synchronized void addPlayerListener(PlayerListener playerListener) {
		checkClosed();
		if (!listeners.contains(playerListener) && playerListener != null) {
			listeners.add(playerListener);
		}
	}

	@Override
	public synchronized void removePlayerListener(PlayerListener playerListener) {
		checkClosed();
		listeners.remove(playerListener);
	}

	private synchronized void postEvent(String event, Object eventData) {
		for (PlayerListener listener : listeners) {
			// Callbacks should be async
			callbackExecutor.execute(() -> listener.playerUpdate(this, event, eventData));
		}
	}

	@Override
	public synchronized void onCompletion(MediaPlayer mp) {
		if (state == CLOSED) {
			return;
		}
		postEvent(PlayerListener.END_OF_MEDIA, getMediaTime());

		if (loopCount == 1) {
			state = PREFETCHED;
			player.reset();
		} else if (loopCount > 1) {
			loopCount--;
		}

		if (state == STARTED && loopCount != -1) {
			player.start();
			postEvent(PlayerListener.STARTED, getMediaTime());
		}
	}

	@Override
	public synchronized void realize() throws MediaException {
		checkClosed();

		if (state == UNREALIZED) {
			try {
				source.connect();
				player.setDataSource(source.getLocator());
			} catch (IOException e) {
				throw new MediaException(e.getMessage());
			}

			state = REALIZED;
		}
	}

	@Override
	public synchronized void prefetch() throws MediaException {
		checkClosed();

		if (state == UNREALIZED) {
			realize();
		}

		if (state == REALIZED) {
			metadata.updateMetaData(source);
			state = PREFETCHED;
		}
	}

	@Override
	public synchronized void start() throws MediaException {
		prefetch();

		if (state == PREFETCHED) {
			player.start();

			state = STARTED;
			postEvent(PlayerListener.STARTED, getMediaTime());
		}
	}

	@Override
	public synchronized void stop() {
		checkClosed();
		if (state == STARTED) {
			player.pause();

			state = PREFETCHED;
			postEvent(PlayerListener.STOPPED, getMediaTime());
		}
	}

	@Override
	public synchronized void deallocate() {
		stop();

		if (state == PREFETCHED) {
			player.reset();
			state = UNREALIZED;

			try {
				realize();
			} catch (MediaException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void close() {
		if (state != CLOSED) {
			player.release();
		}

		source.disconnect();

		state = CLOSED;
		postEvent(PlayerListener.CLOSED, null);
	}

	private void checkClosed() {
		if (state == CLOSED) {
			throw new IllegalStateException("player is closed");
		}
	}

	private void checkRealized() {
		checkClosed();

		if (state == UNREALIZED) {
			throw new IllegalStateException("call realize() before using the player");
		}
	}

	@Override
	public long setMediaTime(long now) throws MediaException {
		checkRealized();
		if (state < PREFETCHED) {
			return 0;
		} else {
			int time = (int) (now / 1000L);
			if (time != player.getCurrentPosition()) {
				player.seekTo(time);
			}
			return getMediaTime();
		}
	}

	@Override
	public long getMediaTime() {
		checkClosed();
		if (state < PREFETCHED) {
			return TIME_UNKNOWN;
		} else {
			return player.getCurrentPosition() * 1000L;
		}
	}

	@Override
	public long getDuration() {
		checkClosed();
		if (state < PREFETCHED) {
			return TIME_UNKNOWN;
		} else {
			return player.getDuration() * 1000L;
		}
	}

	@Override
	public void setLoopCount(int count) {
		checkClosed();
		if (state == STARTED)
			throw new IllegalStateException("player must not be in STARTED state while using setLoopCount()");

		if (count == 0) {
			throw new IllegalArgumentException("loop count must not be 0");
		}

		player.setLooping(count == -1);

		loopCount = count;
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public String getContentType() {
		checkRealized();
		return source.getContentType();
	}

	// VolumeControl

	private void updateVolume() {
		float left, right;

		if (mute) {
			left = right = 0;
		} else {
			if (level == 100) {
				left = right = 1.0f;
			} else {
				left = right = (float) (1 - (Math.log(100 - level) / Math.log(100)));
			}

			if (pan >= 0) {
				left *= (float) (100 - pan) / 100f;
			}

			if (pan < 0) {
				right *= (float) (100 + pan) / 100f;
			}
		}

		player.setVolume(left, right);
		postEvent(PlayerListener.VOLUME_CHANGED, this);
	}

	@Override
	public void setMute(boolean mute) {
		if (state == CLOSED) {
			// Avoid IllegalStateException in MediaPlayer.setVolume()
			return;
		}

		this.mute = mute;
		updateVolume();
	}

	@Override
	public boolean isMuted() {
		return mute;
	}

	@Override
	public int setLevel(int level) {
		if (state == CLOSED) {
			// Avoid IllegalStateException in MediaPlayer.setVolume()
			return this.level;
		}

		if (level < 0) {
			level = 0;
		} else if (level > 100) {
			level = 100;
		}

		this.level = level;
		updateVolume();

		return level;
	}

	@Override
	public int getLevel() {
		return level;
	}


	// PanControl

	@Override
	public int setPan(int pan) {
		if (pan < -100) {
			pan = -100;
		} else if (pan > 100) {
			pan = 100;
		}

		this.pan = pan;
		updateVolume();

		return pan;
	}

	@Override
	public int getPan() {
		return pan;
	}

	// ToneControl

	@Override
	public void setSequence(byte[] sequence) {
		if (state >= PREFETCHED) {
			throw new IllegalStateException();
		} else if (sequence == null) {
			throw new IllegalArgumentException("sequence is NULL");
		}
		try {
			ToneSequence tone = new ToneSequence(sequence);
			tone.process();
			byte[] data = tone.getByteArray();
			String locator = source.getLocator();
			FilesKt.writeBytes(new File(locator), data);
		} catch (Exception e) {
			Log.e(TAG, "setSequence: ", e);
			throw new IllegalArgumentException(e);
		}
	}
}
