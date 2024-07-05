/*
 *  Copyright 2020-2024 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.woesss.j2me.jar;

import android.content.Context;
import android.text.SpannableStringBuilder;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.util.FileUtils;

public class Descriptor {
	// required in JAD and Manifest
	public static final String MIDLET_NAME = "MIDlet-Name";
	public static final String MIDLET_VERSION = "MIDlet-Version";
	public static final String MIDLET_VENDOR = "MIDlet-Vendor";

	// required in JAD
	public static final String MIDLET_JAR_URL = "MIDlet-Jar-URL";
	public static final String MIDLET_JAR_SIZE = "MIDlet-Jar-Size";

	// required in JAD and/or Manifest
	public static final String MIDLET_N = "MIDlet-";
	public static final String MICROEDITION_PROFILE = "MicroEdition-Profile";
	public static final String MICROEDITION_CONFIGURATION = "MicroEdition-Configuration";

	// optional
	public static final String MIDLET_CERTIFICATE_N_S = "MIDlet-Certificate-";
	public static final String MIDLET_DATA_SIZE = "MIDlet-Data-Size";
	public static final String MIDLET_DELETE_CONFIRM = "MIDlet-Delete-Confirm ";
	public static final String MIDLET_DELETE_NOTIFY = "MIDlet-Delete-Notify";
	public static final String MIDLET_DESCRIPTION = "MIDlet-Description";
	public static final String MIDLET_ICON = "MIDlet-Icon";
	public static final String MIDLET_INFO_URL = "MIDlet-Info-URL";
	public static final String MIDLET_INSTALL_NOTIFY = "MIDlet-Install-Notify";
	public static final String MIDLET_JAR_RSA_SHA1 = "MIDlet-Jar-RSA-SHA1";
	public static final String MIDLET_PERMISSIONS = "MIDlet-Permissions";
	public static final String MIDLET_PERMISSIONS_OPT = "MIDlet-Permissions-Opt";
	public static final String MIDLET_PUSH_N = "MIDlet-Push-";

	private static final char UNICODE_BOM = '\uFEFF';
	private static final char SEPARATOR = ':';
	private static final String FAIL_ATTRIBUTE = "Fail attribute '%s: %s'";

	private final boolean isJad;
	private final Map<String, String> attributes = new HashMap<>();

	public Descriptor(String source, boolean isJad) throws IOException {
		this.isJad = isJad;
		try {
			parse(source);
		} catch (Exception e) {
			throw new DescriptorException("Bad descriptor: \n" + source, e);
		}
		if (isJad) {
			verifyJadAttrs();
		}
		verify();

	}

	public Descriptor(File file, boolean isJad) throws IOException {
		this(FileUtils.getText(file.getPath()), isJad);
	}

	public int compareVersion(String version) {
		if (version == null) {
			return 1;
		}
		String[] mv = getVersion().split("\\.");
		String[] ov = version.split("\\.");
		int len = Math.max(mv.length, ov.length);
		for (int i = 0; i < len; i++) {
			int m = 0;
			if (i < mv.length) {
				try {
					m = Integer.parseInt(mv[i].trim());
				} catch (NumberFormatException ignored) { }
			}
			int o = 0;
			if (i < ov.length) {
				try {
					o = Integer.parseInt(ov[i].trim());
				} catch (NumberFormatException ignored) { }
			}
			if (m != o) {
				return Integer.signum(m - o);
			}
		}

		return 0;
	}

	private void verifyJadAttrs() throws DescriptorException {
		String jarSize = getJarSize();
		if (jarSize == null) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_JAR_SIZE, "not found"));
		}
		if (jarSize.isEmpty()) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_JAR_SIZE, "empty value"));
		}
		try {
			Integer.parseInt(jarSize);
		} catch (NumberFormatException e) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_JAR_SIZE, jarSize), e);
		}
		attributes.put(MIDLET_JAR_SIZE, jarSize);
		String url = attributes.get(MIDLET_JAR_URL);
		if (url == null) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_JAR_URL, "not found"));
		} else if (url.isEmpty()) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_JAR_URL, "empty value"));
		}
		attributes.put(MIDLET_JAR_URL, url);
	}

	private void verify() throws DescriptorException {
		if (getName() == null) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_NAME, "not found"));
		}
		if (getVendor() == null) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_VENDOR, "not found"));
		}
		if (getVersion() == null) {
			throw new DescriptorException(String.format(FAIL_ATTRIBUTE, MIDLET_VERSION, "not found"));
		}
	}

	public String getVersion() {
		return attributes.get(MIDLET_VERSION);
	}

	public final Map<String, String> getAttrs() {
		return attributes;
	}

	private void parse(String source) {
		String[] lines = source.split("[\\n\\r]+");
		for (int i = lines.length - 1; i > 0; i--) {
			String line = lines[i];
			int separatorIdx = line.indexOf(SEPARATOR);
			if (separatorIdx == -1) {
				if (line.isEmpty()) {
					continue;
				}
				if (line.charAt(0) == ' ') {
					lines[i - 1] += line.substring(1);
				} else {
					lines[i - 1] += line;
				}
			} else {
				String name = substringStripped(line, 0, separatorIdx);
				String value = substringStripped(line, separatorIdx + 1, line.length());
				attributes.put(name, value);
			}
		}
		String line = lines[0];
		int colon = line.indexOf(SEPARATOR);
		if (colon != -1) {
			String name = substringStripped(line, line.charAt(0) == UNICODE_BOM ? 1 : 0, colon);
			String value = substringStripped(line, colon + 1, line.length());
			attributes.put(name, value);
		}
	}

	private static String substringStripped(String string, int beginIndex, int endIndex) {
		while (beginIndex < endIndex) {
			int c = string.codePointAt(beginIndex);
			if (c != ' ' && c != '\t') {
				break;
			}
			beginIndex += Character.charCount(c);
		}
		while (beginIndex < endIndex) {
			int c = string.codePointBefore(endIndex);
			if (c != ' ' && c != '\t') {
				break;
			}
			endIndex -= Character.charCount(c);
		}
		return string.substring(beginIndex, endIndex);
	}

	public String getName() {
		return attributes.get(MIDLET_NAME);
	}

	private static String getSizePretty(String number) {
		long size = Long.parseLong(number);
		if (size < 1024L) {
			return number + " B";
		}
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);
		float kb = (float) size / 1024.0F;
		if (kb < 1024.0F) {
			return formatter.format(kb) + " KB";
		}
		float mb = kb / 1024F;
		if (mb < 1024F) {
			return formatter.format(mb) + " MB";
		}
		float gb = mb / 1024F;
		return formatter.format(gb) + " GB";
	}

	public String getVendor() {
		return attributes.get(MIDLET_VENDOR);
	}

	public String getIcon() {
		String icon = attributes.get(MIDLET_ICON);
		if (icon == null || icon.isEmpty()) {
			String midlet = attributes.get(MIDLET_N + 1);
			if (midlet == null) {
				return null;
			}
			int start = midlet.indexOf(',');
			if (start == -1) {
				return null;
			}
			int end = midlet.indexOf(',', ++start);
			if (end == -1) {
				return null;
			}
			icon = midlet.substring(start, end).trim();
		}
		for (int i = 0; i < icon.length(); i++) {
			if (icon.charAt(i) != '/') {
				return icon.substring(i);
			}
		}
		return null;
	}

	public String getJarUrl() {
		return attributes.get(MIDLET_JAR_URL);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Descriptor o) {
			return getName().equals(o.getName()) && getVendor().equals(o.getVendor());
		}
		return false;
	}

	public SpannableStringBuilder getInfo(Context c) {
		SpannableStringBuilder info = new SpannableStringBuilder();
		String description = attributes.get(MIDLET_DESCRIPTION);
		if (description != null) {
			info.append(description).append("\n\n");
		}
		info.append(c.getText(R.string.midlet_name)).append(getName()).append('\n');
		info.append(c.getText(R.string.midlet_vendor)).append(getVendor()).append('\n');
		info.append(c.getText(R.string.midlet_version)).append(getVersion()).append('\n');
		String jarSize = getJarSize();
		if (jarSize != null) {
			info.append(c.getText(R.string.midlet_size)).append(getSizePretty(jarSize)).append('\n');
		}
		info.append('\n');
		return info;
	}

	private String getJarSize() {
		return isJad ? attributes.get(MIDLET_JAR_SIZE) : null;
	}

	public void merge(Descriptor newDescriptor) {
		attributes.putAll(newDescriptor.attributes);
	}

	public void writeTo(File file) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			writeTo(fos);
		}
	}

	private void writeTo(OutputStream outputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
		}
		outputStream.write(sb.toString().getBytes());
	}

	public boolean containsAllAttributes(Descriptor o) {
		for (Map.Entry<String, String> e : o.attributes.entrySet()) {
			if (!e.getValue().equals(o.attributes.get(e.getKey()))) {
				return false;
			}
		}
		return true;
	}
}
