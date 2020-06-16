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

import org.acra.ACRA;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;

import dalvik.system.DexClassLoader;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.util.FileUtils;
import ru.playsoftware.j2meloader.util.ZipFileCompat;
import ru.playsoftware.j2meloader.util.ZipUtils;

public class AppClassLoader extends DexClassLoader {
	private static final String TAG = AppClassLoader.getName();

	private static AppClassLoader instance;
	private static String name;

	private final HashMap<String, String> resources = new HashMap<>();
	private String resFolderPath;

	AppClassLoader(String paths, String tmpDir, ClassLoader parent, File resDir) {
		super(paths, tmpDir, null, new CoreClassLoader(parent));
		resFolderPath = resDir.getPath();
		File appDir = resDir.getParentFile();
		if (appDir == null)
			throw new NullPointerException();
		name = appDir.getName();
		ACRA.getErrorReporter().putCustomData("Running app", name);
		instance = this;
		File midletResFile = new File(Config.getAppDir(), name + Config.MIDLET_RES_FILE);
		if (midletResFile.exists()) loadNamesFromJar(midletResFile);
		else loadNamesFromDir(resDir);
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

	private void loadNamesFromJar(File jar) {
		try (ZipFileCompat zip = new ZipFileCompat(jar)) {
			while (true) {
				try {
					ZipEntry e = zip.getNextEntry();
					if (e == null) break;
					String name = e.getName();
					if (name.endsWith("/")) {
						continue;
					}
					String ln = name.toLowerCase();
					if (ln.endsWith(".class")) {
						continue;
					}
					resources.put(name, name);
					if (!resources.containsKey(ln)) {
						resources.put(ln, name);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadNamesFromDir(File dir) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) {
				loadNamesFromDir(file);
				continue;
			}
			if (file.getName().toLowerCase().endsWith(".class")) {
				continue;
			}
			String name = file.getPath().substring(resFolderPath.length() + 1);
			resources.put(name, name);
			String low = name.toLowerCase();
			if (!resources.containsKey(low)) {
				resources.put(low, name);
			}
		}
	}

	private static byte[] getResourceBytes(String name) {
		HashMap<String, String> resources = instance.resources;
		String path = resources.get(name);
		if (path == null) path = resources.get(name.toLowerCase());
		if (path == null) {
			Log.w(TAG, "getResourceBytes: not found res: " + name);
			return null;
		}
		File midletResFile = new File(Config.getAppDir(), getName() + Config.MIDLET_RES_FILE);
		if (midletResFile.exists()) {
			try {
				return ZipUtils.unzipEntry(midletResFile, path);
			} catch (IOException e) {
				Log.w(TAG, "getResourceBytes: from jar [entry=" + name + "]", e);
				return null;
			}
		} else {
			final File file = new File(instance.resFolderPath, path);
			try {
				return FileUtils.getBytes(file);
			} catch (Exception e) {
				Log.w(TAG, "getResourceBytes: from file=" + file, e);
				return null;
			}
		}
	}

	public static String getName() {
		return name;
	}

	public static AppClassLoader getInstance() {
		return instance;
	}
}
