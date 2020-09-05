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

package javax.microedition.shell;

import android.util.Log;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.util.FileUtils;

public class AppClassLoader extends DexClassLoader {
	private static final String TAG = AppClassLoader.getName();

	private static AppClassLoader instance;
	private static String name;
	private static ZipFile zipFile;
	private String resFolderPath;

	AppClassLoader(String paths, String tmpDir, ClassLoader parent, File resDir) {
		super(paths, tmpDir, null, new CoreClassLoader(parent));
		resFolderPath = resDir.getPath();
		File appDir = resDir.getParentFile();
		if (appDir == null)
			throw new NullPointerException();
		name = appDir.getName();
		instance = this;
		prepareZipFile();
	}

	public static InputStream getResourceAsStream(Class<?> resClass, String resName) {
		Log.d(TAG, "CUSTOM GET RES CALLED WITH PATH: " + resName);
		if (resName == null || resName.equals("")) {
			Log.w(TAG, "Can't load res on empty path");
			return null;
		}
		// Add support for Siemens file path
		String normName = resName.replace('\\', '/');
		// Remove double slashes
		normName = normName.replaceAll("//+", "/");
		if (normName.charAt(0) != '/' && resClass != null && resClass.getPackage() != null) {
			String className = resClass.getPackage().getName().replace('.', '/');
			normName = className + "/" + normName;
		}
		// Remove leading slash
		if (normName.charAt(0) == '/') {
			normName = normName.substring(1);
		}
		byte[] data = getResourceBytes(normName);
		if (data == null) {
			Log.w(TAG, "Can't load res: " + resName);
			return null;
		}
		return new ByteArrayInputStream(data);
	}

	public void prepareZipFile() {
		File midletResFile = new File(Config.getAppDir(), name + Config.MIDLET_RES_FILE);
		if (midletResFile.exists()) {
			zipFile = new ZipFile(midletResFile);
		}
	}

	public static byte[] getResourceAsBytes(String resName) {
		if (resName == null || resName.equals("")) {
			Log.w(TAG, "Can't load res on empty path");
			return null;
		}
		// Add support for Siemens file path
		String normName = resName.replace('\\', '/');
		// Remove double slashes
		normName = normName.replaceAll("//+", "/");
		// Remove leading slash
		if (normName.charAt(0) == '/') {
			normName = normName.substring(1);
		}
		byte[] data = getResourceBytes(normName);
		if (data == null) {
			Log.w(TAG, "Can't load res: " + resName);
			return null;
		}
		return data;
	}

	private static byte[] getResourceBytes(String name) {
		File midletResFile = new File(Config.getAppDir(), getName() + Config.MIDLET_RES_FILE);
		if (!midletResFile.exists()) {
			final File file = new File(instance.resFolderPath, name);
			try {
				return FileUtils.getBytes(file);
			} catch (Exception e) {
				Log.w(TAG, "getResourceBytes: from file=" + file, e);
				return null;
			}
		}
		DataInputStream dis = null;
		try {
			FileHeader header = zipFile.getFileHeader(name);
			if (header == null) {
				return null;
			}
			dis = new DataInputStream(zipFile.getInputStream(header));
			byte[] data = new byte[(int) header.getUncompressedSize()];
			dis.readFully(data);
			return data;
		} catch (ZipException e) {
			Log.e(TAG, "getResourceBytes: ", e);
		} catch (IOException e) {
			Log.e(TAG, "getResourceBytes: ", e);
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					Log.e(TAG, "getResourceBytes: ", e);
				}
			}
		}
		return null;
	}

	public static String getName() {
		return name;
	}

	public static AppClassLoader getInstance() {
		return instance;
	}
}
