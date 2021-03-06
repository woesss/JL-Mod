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

package ru.playsoftware.j2meloader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import androidx.multidex.MultiDex;

import org.acra.ACRA;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.microedition.util.ContextHolder;

public class EmulatorApplication extends Application {

	private static final byte[] SIGNATURE_SHA = {
			125, 47, 64, 33, 91, -86, -121, 89, 11, 24, -118, -93, 35, 53, -34, -114, -119, -60, -48, 55
	};

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		if (BuildConfig.DEBUG) {
			MultiDex.install(this);
		}
		ContextHolder.setApplication(this);
		if (isSignatureValid()) {
			CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);
			builder.withBuildConfigClass(BuildConfig.class)
					.withParallel(false)
					.withSendReportsInDevMode(false)
					.withEnabled(true);
			builder.getPluginConfigurationBuilder(DialogConfigurationBuilder.class)
					.withResTitle(R.string.crash_dialog_title)
					.withResText(R.string.crash_dialog_message)
					.withResPositiveButtonText(R.string.report_crash)
					.withResTheme(R.style.Theme_AppCompat_Dialog)
					.withEnabled(true);
			ACRA.init(this, builder);
		}
	}

	@SuppressLint("PackageManagerGetSignatures")
	private boolean isSignatureValid() {
		try {
			Signature[] signatures;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				PackageInfo info = getPackageManager()
						.getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
				signatures = info.signingInfo.getApkContentsSigners();
			} else {
				PackageInfo info = getPackageManager()
						.getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
				signatures = info.signatures;
			}
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			for (Signature signature : signatures) {
				md.update(signature.toByteArray());
				if (Arrays.equals(SIGNATURE_SHA, md.digest())) {
					return true;
				}
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
}
