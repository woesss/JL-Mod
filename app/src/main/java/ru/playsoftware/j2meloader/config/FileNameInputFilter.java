/*
 * Copyright 2023 Yury Kharchenko
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

package ru.playsoftware.j2meloader.config;

import android.text.InputFilter;
import android.text.Spanned;

class FileNameInputFilter implements InputFilter {
	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++) {
			char ch = source.charAt(i);
			if ("/\\:*?\"<>|".indexOf(ch) == -1) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}
