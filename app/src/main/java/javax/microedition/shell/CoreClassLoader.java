/*
 * Copyright 2018 Nikita Shakarun
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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CoreClassLoader extends ClassLoader {
	public static final Pattern INCLUDE = Pattern.compile("java\\..+|com\\..+|javax\\..+|mmpp\\..+|org.xml.sax.+");
	public static final Pattern EXCLUDE = initExcludePattern();

	private static Pattern initExcludePattern() {
		String prop = MidletSystem.getProperty("emulator.classpath.exclude");
		if (prop == null) {
			return null;
		}
		String[] list = prop.split("[:;]");
		List<String> parts = new ArrayList<>(list.length);
		for (String value : list) {
			String s = value.trim();
			if (s.isEmpty()) {
				continue;
			}
			parts.add(s.replace(".", "\\.") + ".*");
		}
		if (parts.isEmpty()) {
			return null;
		}
		return Pattern.compile(TextUtils.join("|", parts));
	}

	public CoreClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (EXCLUDE != null && EXCLUDE.matcher(name).matches()) {
			throw new ClassNotFoundException();
		}
		if (INCLUDE.matcher(name).matches()) {
			return super.loadClass(name, resolve);
		}
		throw new ClassNotFoundException();
	}
}
