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

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFprobe;
import com.arthenica.mobileffmpeg.MediaInformation;
import com.arthenica.mobileffmpeg.StreamInformation;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.microedition.media.protocol.DataSource;
import javax.microedition.media.protocol.SourceStream;
import javax.microedition.shell.AppClassLoader;
import javax.microedition.util.ContextHolder;

import ru.playsoftware.j2meloader.util.FileUtils;

public class InternalDataSource extends DataSource {
	private static final String TAG = InternalDataSource.class.getName();

	private File mediaFile;
	private final String type;

	public InternalDataSource(InputStream stream, String type) throws IllegalArgumentException, IOException {
		super(null);

		this.type = type;
		byte[] buf = new byte[0x10000];
		int read = stream.read(buf);
		if (read >= 4) {
			if (buf[0] == 'M' && buf[1] == 'T' && buf[2] == 'h' && buf[3] == 'd') {
				final File dlsFile = new File(new File(AppClassLoader.getDataDir()).getParentFile().getParentFile(), "soundbank.dls");
				if (dlsFile.exists()) {
					this.mediaFile = File.createTempFile("media", ".xmf", ContextHolder.getCacheDir());
					try (RandomAccessFile raf = new RandomAccessFile(mediaFile, "rw")) {
						Log.d(TAG, "Starting media pipe: " + mediaFile.getName());
						convertMidi(read, buf, stream, raf, FileUtils.getBytes(dlsFile));
					} finally {
						stream.close();
					}
					return;
				}
			}
		}


		String extension = "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
		this.mediaFile = File.createTempFile("media", extension, ContextHolder.getCacheDir());

		final RandomAccessFile raf = new RandomAccessFile(mediaFile, "rw");

		final String name = mediaFile.getName();
		Log.d(TAG, "Starting media pipe: " + name);

		int length = stream.available();
		if (length >= 0) {
			raf.setLength(length);
			Log.d(TAG, "Changing file size to " + length + " bytes: " + name);
		}

		try {
			do {
				raf.write(buf, 0, read);
			} while ((read = stream.read(buf)) != -1);
			Log.d(TAG, "Media pipe closed: " + name);
		} catch (IOException e) {
			Log.d(TAG, "Media pipe failure: " + e);
		} finally {
			raf.close();
			stream.close();
		}

		try {
			convert();
		} catch (Throwable t) {
			Log.e(TAG, "FFmpeg error", t);
		}
	}

	private void convertMidi(int read, byte[] buf, InputStream stream, RandomAccessFile raf, byte[] dlsBytes)
			throws IOException {

		int dlsNodeLength = 8 + dlsBytes.length;
		int smfNodeLength = 8 + read + stream.available();
		int rootNodeLength = 8 + smfNodeLength + dlsNodeLength;

		int fileLength = 18 + rootNodeLength;

		raf.setLength(fileLength);
		Log.d(TAG, "Changing file size to " + fileLength + " bytes: " + raf);

		raf.write("XMF_1.00".getBytes());
		writeVlq(fileLength, raf); // file length
		raf.write(0); // MetaDataTypesTable length
		raf.write(18); // TreeStart offset
		writeVlq(fileLength - 1, raf); // TreeEnd offset

		writeVlq(rootNodeLength, raf); // node length
		raf.write(2); // number of contained items
		raf.write(7); // node header length
		raf.write(0); // metadata length
		raf.write(1); // Reference Type

		writeVlq(dlsNodeLength, raf); // node length
		raf.write(0); // number of contained items
		raf.write(7); // node header length
		raf.write(0); // metadata length
		raf.write(1); // Reference Type

		raf.write(dlsBytes);

		writeVlq(smfNodeLength, raf); // node length
		raf.write(0); // number of contained items
		raf.write(7); // node header length
		raf.write(0); // metadata length
		raf.write(1); // Reference Type

		do {
			raf.write(buf, 0, read);
		} while ((read = stream.read(buf)) != -1);

	}

	/** Write fake VLQ (size fixed to 4 bytes for simplify node size computation) */
	private static void writeVlq(int v, DataOutput out) throws IOException {
		int b = v >>> 28;
		if (b > 0) {
			throw new RuntimeException("VLQ value is large");
		}
		out.write(v >>> 21 & 0x7f | 0x80);
		out.write(v >>> 14 & 0x7f | 0x80);
		out.write(v >>>  7 & 0x7f | 0x80);
		out.write(v & 0x7f);
	}

	private void convert() {
		MediaInformation mediaInformation = FFprobe.getMediaInformation(mediaFile.getPath());
		if (mediaInformation != null) {
			StreamInformation streamInformation = mediaInformation.getStreams().get(0);
			if (streamInformation.getCodec().contains("adpcm")) {
				String newName = mediaFile.getPath() + ".wav";
				String cmd = "-i " + mediaFile.getPath() + " -acodec pcm_u8 -ar 16000 " + newName;
				int rc = FFmpeg.execute(cmd);
				if (rc == Config.RETURN_CODE_SUCCESS) {
					Log.i(TAG, "Command execution completed successfully.");
					mediaFile.delete();
					mediaFile = new File(newName);
				} else {
					Log.i(TAG, String.format(
							"Command execution failed with rc=%d and the output below.", rc));
				}
			}
		}
	}

	@Override
	public String getLocator() {
		return mediaFile.getAbsolutePath();
	}

	@Override
	public String getContentType() {
		return type;
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public void disconnect() {
		if (mediaFile.delete()) {
			Log.d(TAG, "Temp file deleted: " + mediaFile.getAbsolutePath());
		}
	}

	@Override
	public void start() throws IOException {
	}

	@Override
	public void stop() throws IOException {
	}

	@Override
	public SourceStream[] getStreams() {
		return new SourceStream[0];
	}

	@Override
	public Control[] getControls() {
		return new Control[0];
	}

	@Override
	public Control getControl(String control) {
		return null;
	}

}
