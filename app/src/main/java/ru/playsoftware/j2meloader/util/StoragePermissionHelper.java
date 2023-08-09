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

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.CATEGORY_DEFAULT;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.ACTION_REQUEST_PERMISSIONS;
import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSIONS;
import static androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.EXTRA_PERMISSION_GRANT_RESULTS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class StoragePermissionHelper extends ActivityResultContract<Intent, Boolean> {
	private static final String TAG = "StoragePermContract";
	private final ActivityResultLauncher<Intent> permissionsLauncher;
	private final ActivityResultCallback<Boolean> callback;

	public StoragePermissionHelper(ActivityResultCaller caller, ActivityResultCallback<Boolean> callback) {
		this.callback = callback;
		permissionsLauncher = caller.registerForActivityResult(this, callback);
	}

	public void launch(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			try {
				permissionsLauncher.launch(new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
						Uri.parse("package:" + context.getPackageName()))
						.addCategory(CATEGORY_DEFAULT));
				return;
			} catch (Exception e) {
				Log.w(TAG, "launch: failed", e);
			}
			try {
				permissionsLauncher.launch(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
				return;
			} catch (Exception e) {
				Log.w(TAG, "launch: failed", e);
			}
			context.getMainExecutor().execute(() -> callback.onActivityResult(false));
		} else {
			permissionsLauncher.launch(new Intent(ACTION_REQUEST_PERMISSIONS)
					.putExtra(EXTRA_PERMISSIONS, new String[]{WRITE_EXTERNAL_STORAGE}));
		}
	}

	@NonNull
	@Override
	public Intent createIntent(@NonNull Context context, @NonNull Intent input) {
		return input;
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
		return grantResults != null && grantResults.length != 0 && grantResults[0] == PERMISSION_GRANTED;
	}

	@Override
	public @Nullable SynchronousResult<Boolean> getSynchronousResult(
			@NonNull Context context, @Nullable Intent input) {
		return isGranted(context) ? new SynchronousResult<>(true) : null;
	}

	public static boolean isGranted(@NonNull Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.isExternalStorageManager();
		}
		return ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
	}
}
