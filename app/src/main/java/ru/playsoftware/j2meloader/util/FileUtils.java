/*
 * Copyright 2015-2016 Nickolay Savchenko
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

package ru.playsoftware.j2meloader.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class FileUtils {

	private static String TAG = FileUtils.class.getName();
	private static final String TEMP_JAR_NAME = "tmp.jar";
	private static final String TEMP_JAD_NAME = "tmp.jad";
	private static final int BUFFER_SIZE = 1024;

	public static void copyFiles(String src, String dest, FilenameFilter filter) {
		File srcFile = new File(src);
		File dstFile = new File(dest);
		if (!dstFile.exists() && !dstFile.mkdirs())
			return;
		File[] list = srcFile.listFiles(filter);
		if (list == null) {
			return;
		}
		for (File entry : list) {
			String to = entry.getPath().replace(src, dest);
			if (entry.isDirectory()) {
				copyFiles(entry.getPath(), to, filter);
			} else {
				try {
					copyFileUsingChannel(entry, new File(to));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void copyFileUsingChannel(File source, File dest) throws IOException {
		try (FileChannel sourceChannel = new FileInputStream(source).getChannel(); FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
			destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
		}
	}

	public static void deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] listFiles = dir.listFiles();
			if (listFiles != null && listFiles.length != 0) {
				for (File file : listFiles) {
					deleteDirectory(file);
				}
			}
		}
		if (!dir.delete()) {
			Log.w(TAG, "Can't delete file: " + dir);
		}
	}

	public static String getAppPath(Context context, Uri uri) throws IOException {
		if ("file".equals(uri.getScheme())) {
			String path = uri.getPath();
			if (path != null && new File(path).exists()) {
				return path;
			}
		}
		InputStream in = context.getContentResolver().openInputStream(uri);
		OutputStream out = null;
		File folder = context.getCacheDir();
		byte[] signature = new byte[2];
		byte[] jarSignature = new byte[]{0x50, 0x4B};
		if (in == null || in.read(signature) == -1)
			throw new IOException("Can't read data from uri: " + uri);
		File file;
		if (Arrays.equals(signature, jarSignature)) {
			file = new File(folder, TEMP_JAR_NAME);
		} else {
			file = new File(folder, TEMP_JAD_NAME);
		}
		try {
			out = new FileOutputStream(file);
			byte[] buf = new byte[BUFFER_SIZE];
			int len;
			out.write(signature);
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file.getPath();
	}

	public static byte[] getBytes(File file) throws IOException {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			byte[] b = new byte[(int) file.length()];
			dis.readFully(b);
			return b;
		}
	}

	public static void clearDirectory(File dir) {
		if (!dir.isDirectory()) return;
		final File[] files = dir.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDirectory(dir);
			} else {
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}
	}
}
