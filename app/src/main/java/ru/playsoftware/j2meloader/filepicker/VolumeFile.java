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

package ru.playsoftware.j2meloader.filepicker;

import androidx.annotation.NonNull;

import java.io.File;

class VolumeFile extends File {
	private final String description;

	public VolumeFile(String path, String description) {
		super(path);
		this.description = description;
	}

	@Override
	public @NonNull String getName() {
		if (description != null) {
			return description;
		}
		return super.getName();
	}
}
