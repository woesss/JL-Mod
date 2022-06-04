/*
 *  Copyright 2022 Yury Kharchenko
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

package ru.playsoftware.j2meloader.util;

import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS;
import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS;
import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class StoragePermissionContract extends ActivityResultContract<Void, Boolean> {

	@NonNull
	@Override
	public Intent createIntent(@NonNull Context context, @NonNull Void input) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
		} else {
			String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
			return new Intent(ACTION_REQUEST_PERMISSIONS).putExtra(EXTRA_PERMISSIONS, permissions);
		}
	}

	@NonNull
	@Override
	public Boolean parseResult(int resultCode, @Nullable Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		}
		if (intent == null || resultCode != Activity.RESULT_OK) {
			return false;
		}
		int[] grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS);
		return grantResults != null && grantResults.length != 0 &&
				grantResults[0] == PackageManager.PERMISSION_GRANTED;
	}

	@Override
	public @Nullable SynchronousResult<Boolean> getSynchronousResult(
			@NonNull Context context, @Nullable Void input) {
		return isGranted(context) ? new SynchronousResult<>(true) : null;
	}

	public static boolean isGranted(@NonNull Context context) {
		boolean granted;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			granted = Environment.isExternalStorageManager();
		} else {
			int status = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			granted = status == PackageManager.PERMISSION_GRANTED;
		}
		return granted;
	}
}
