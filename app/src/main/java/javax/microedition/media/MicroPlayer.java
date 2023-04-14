/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2017-2018 Nikita Shakarun
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

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.amms.control.PanControl;
import javax.microedition.amms.control.audioeffect.EqualizerControl;
import javax.microedition.media.control.MetaDataControl;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.util.ContextHolder;

import ru.woesss.synthlib.MidiPlayer;

public class MicroPlayer extends BasePlayer implements MediaPlayer.OnCompletionListener,
		VolumeControl, PanControl {
	private static String soundFont;
	private final boolean useSoundFont;
	protected InternalDataSource source;
	protected int state = UNREALIZED;
	private MediaPlayer player;
	private MidiPlayer midiPlayer;
	private int loopCount = 1;

	private final ArrayList<PlayerListener> listeners = new ArrayList<>();
	private final HashMap<String, Control> controls = new HashMap<>();

	private boolean mute = false;
	private int level = 100;
	private int pan;

	private final InternalMetaData metadata = new InternalMetaData();

	public MicroPlayer(InternalDataSource datasource) {
		source = datasource;
		boolean useSoundFont = false;
		if (soundFont != null && datasource.isMidi) {
			try {
				midiPlayer = new MidiPlayer(soundFont);
				midiPlayer.setOnCompletionListener(this);
				useSoundFont = true;
			} catch (Exception e) {
				Log.e("MicroPlayer", "Init soundfont failed", e);
				ContextHolder.getActivity().runOnUiThread(() -> {
					Toast.makeText(ContextHolder.getActivity(),
							"Failed to load soundfont, default selected",
							Toast.LENGTH_SHORT)
							.show();
				});
			}
		}
		if (!useSoundFont) {
			player = new AndroidPlayer();
			player.setOnCompletionListener(this);
		}
		this.useSoundFont = useSoundFont;

		controls.put(VolumeControl.class.getName(), this);
		controls.put(PanControl.class.getName(), this);
		controls.put(MetaDataControl.class.getName(), metadata);
		controls.put(EqualizerControl.class.getName(), new InternalEqualizer());
	}

	public static void setSoundFont(String path) {
		soundFont = path;
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

	public synchronized void postEvent(String event, Object eventData) {
		for (PlayerListener listener : listeners) {
			// Callbacks should be async
			Runnable r = () -> listener.playerUpdate(this, event, eventData);
			(new Thread(r, "MIDletPlayerCallback")).start();
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
			if (useSoundFont) {
				midiPlayer.reset();
			} else {
				player.reset();
			}
		} else if (loopCount > 1) {
			loopCount--;
		}

		if (state == STARTED) {
			if (useSoundFont) {
				midiPlayer.start();
			} else if (loopCount != -1) {
				player.start();
			}
		}
	}

	@Override
	public synchronized void realize() throws MediaException {
		checkClosed();

		if (state == UNREALIZED) {
			try {
				source.connect();
				if (useSoundFont) {
					midiPlayer.setDataSource(source.getLocator());
				} else {
					player.setDataSource(source.getLocator());
				}
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
			try {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				retriever.setDataSource(source.getLocator());
				metadata.updateMetaData(retriever);
				retriever.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
			state = PREFETCHED;
		}
	}

	@Override
	public synchronized void start() throws MediaException {
		prefetch();

		if (state == PREFETCHED) {
			if (useSoundFont) {
				midiPlayer.start();
			} else {
				player.start();
			}

			state = STARTED;
			postEvent(PlayerListener.STARTED, getMediaTime());
		}
	}

	@Override
	public synchronized void stop() {
		checkClosed();
		if (state == STARTED) {
			if (useSoundFont) {
				midiPlayer.stop();
			} else {
				player.pause();
			}

			state = PREFETCHED;
			postEvent(PlayerListener.STOPPED, getMediaTime());
		}
	}

	@Override
	public synchronized void deallocate() {
		stop();

		if (state == PREFETCHED) {
			if (useSoundFont) {
				midiPlayer.reset();
			} else {
				player.reset();
			}
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
			if (useSoundFont) {
				midiPlayer.release();
			} else {
				player.release();
			}
		}

		source.disconnect();

		state = CLOSED;
		postEvent(PlayerListener.CLOSED, null);
	}

	protected void checkClosed() {
		if (state == CLOSED) {
			throw new IllegalStateException("player is closed");
		}
	}

	protected void checkRealized() {
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
		} else if (useSoundFont) {
			return midiPlayer.setMediaTime(now);
		} else {
			int time = (int) (now / 1000);
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
		} else if (useSoundFont) {
			return midiPlayer.getMediaTime();
		} else {
			return player.getCurrentPosition() * 1000L;
		}
	}

	@Override
	public long getDuration() {
		checkClosed();
		if (state < PREFETCHED) {
			return TIME_UNKNOWN;
		} else if (useSoundFont) {
			return midiPlayer.getDuration();
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

		if (useSoundFont) {
//			midiPlayer.setLoopCount(count);
		} else {
			player.setLooping(count == -1);
		}

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

		if (useSoundFont) {
			midiPlayer.setVolume(left, right);
		} else {
			player.setVolume(left, right);
		}
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

}
