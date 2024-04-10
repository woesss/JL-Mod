/*
 * Copyright 2019 Nikita Shakarun
 * Copyright 2020-2024 Yury Kharchenko
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

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import ru.playsoftware.j2meloader.config.Config;

public class LogUtils {

	public static void writeLog() throws IOException {
		File logFile = new File(Config.getEmulatorDir(), "log.txt");
		if (logFile.exists()) {
			logFile.delete();
		}
		Runtime.getRuntime().exec("logcat -t 500 -f " + logFile);
	}

	@NonNull
	public static CharSequence getPrettyText(Throwable throwable) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		printMessages(throwable, sb, "");
		sb.setSpan(new TypefaceSpan("monospace"), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sb;
	}

	private static void printMessages(Throwable throwable, SpannableStringBuilder sb, String indent) {
		sb.append(throwable.toString());
		sb.append("\n");

		// Print suppressed exceptions indented one level deeper.
		Throwable[] suppressedExceptions = throwable.getSuppressed();
		for (Throwable t : suppressedExceptions) {
			sb.append(indent);
			sb.append("\tSuppressed: ");
			printMessages(t, sb, indent + "\t");
		}

		Throwable cause = throwable.getCause();
		if (cause != null) {
			sb.append(indent);
			sb.append("Caused by: ");
			printMessages(cause, sb, indent);
		}
	}

	public static String getStackTraceString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
}
