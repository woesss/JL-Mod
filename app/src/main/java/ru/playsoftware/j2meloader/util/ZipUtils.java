/*
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

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	private static final int BUFFER_SIZE = 8096;
	private static final String TAG = ZipUtils.class.getName();

	public static void zip(File sourceFolder, File zipFile) throws IOException {
		FileOutputStream dest = new FileOutputStream(zipFile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		zipSubFolder(out, sourceFolder, sourceFolder.getPath().length() + 1);
		out.close();
	}

	private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength)
			throws IOException {
		File[] fileList = folder.listFiles();
		if (fileList == null) {
			throw new IOException("Can't access dir: " + folder);
		}
		BufferedInputStream origin;
		for (File file : fileList) {
			if (file.isDirectory()) {
				zipSubFolder(out, file, basePathLength);
			} else {
				byte[] data = new byte[BUFFER_SIZE];
				String unmodifiedFilePath = file.getPath();
				String relativePath = unmodifiedFilePath.substring(basePathLength);
				FileInputStream fi = new FileInputStream(unmodifiedFilePath);
				origin = new BufferedInputStream(fi, BUFFER_SIZE);
				ZipEntry entry = new ZipEntry(relativePath);
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
		}
	}

	public static boolean unzip(File zipFile, File extractFolder) throws IOException {
		boolean warn = false;
		try (ZipFile zip = new ZipFile(zipFile)) {
			if (!extractFolder.exists() && !extractFolder.mkdir()) {
				throw new IOException("Can't make directory: " + extractFolder);
			}
			Enumeration zipFileEntries = zip.entries();
			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				File dstFile = new File(extractFolder, entry.getName());
				if (dstFile.exists()) {
					warn = true;
					continue;
				}
				File dstDir = dstFile.getParentFile();
				if (dstDir != null && !dstDir.exists() && !dstDir.mkdirs()) {
					throw new IOException("Can't make directory: " + dstDir);
				}
				try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
					BufferedOutputStream dest = new BufferedOutputStream(
							new FileOutputStream(dstFile), BUFFER_SIZE)) {
					int currentByte;
					byte[] data = new byte[BUFFER_SIZE];
					// write the current file to disk
					while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
				} catch (Exception e) {
					Log.w(TAG, "unzip: [entry=" + entry.getName() + "]", e);
				}
			}
		}
		return warn;
	}

	public static void unzipEntry(File srcZip, String name, File dst) throws IOException {
		try (ZipFile zip = new ZipFile(srcZip)) {
			ZipEntry entry = zip.getEntry(name);
			if (entry == null) {
				throw new IOException("Entry '" + name + "' not found in zip: " + srcZip);
			}
			try (BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(entry));
				 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE)) {
				byte[] data = new byte[BUFFER_SIZE];
				int read;
				while ((read = bis.read(data)) != -1) {
					bos.write(data, 0, read);
				}
			}
		}
	}

	public static byte[] unzipEntry(File srcZip, String name) throws IOException {
		try (ZipFile zip = new ZipFile(srcZip)) {
			ZipEntry entry = zip.getEntry(name);
			if (entry == null) {
				throw new IOException("Entry '" + name + "' not found in zip: " + srcZip);
			}
			try (BufferedInputStream bis = new BufferedInputStream(zip.getInputStream(entry));
				 ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE)) {
				byte[] data = new byte[BUFFER_SIZE];
				int read;
				while ((read = bis.read(data)) != -1) {
					bos.write(data, 0, read);
				}
				return bos.toByteArray();
			}
		}
	}
}
