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

package ru.playsoftware.j2meloader.settings;

import static ru.playsoftware.j2meloader.util.Constants.PREF_EMULATOR_DIR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import ru.playsoftware.j2meloader.EmulatorApplication;
import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.config.ProfilesActivity;
import ru.playsoftware.j2meloader.util.FileUtils;
import ru.playsoftware.j2meloader.util.PickDirResultContract;
import ru.playsoftware.j2meloader.util.XmlUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
	private static final String TAG = "SettingsFragment";

	private Preference prefFolder;
	private final ActivityResultLauncher<String> openDirLauncher = registerForActivityResult(
			new PickDirResultContract(),
			this::onPickDirResult);

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {
		addPreferencesFromResource(R.xml.preferences);
		Objects.<Preference>requireNonNull(findPreference("pref_default_settings"))
				.setIntent(new Intent(requireActivity(), ProfilesActivity.class));
		initLanguages();
		prefFolder = Objects.requireNonNull(findPreference(PREF_EMULATOR_DIR));
		prefFolder.setSummary(Config.getEmulatorDir());
		prefFolder.setOnPreferenceClickListener(preference -> {
			openDirLauncher.launch(null);
			return true;
		});
	}

	private void initLanguages() {
		ListPreference prefLanguage = Objects.requireNonNull(findPreference("pref_language"));
		StringBuilder sb = new StringBuilder();
		Context context = EmulatorApplication.getInstance();
		Resources resources = context.getResources();
		@SuppressLint("DiscouragedApi")
		int id = resources.getIdentifier("_generated_res_locale_config", "xml", context.getPackageName());
		try (XmlResourceParser parser = resources.getXml(id)) {
			if (XmlUtils.nextElement(parser, "locale")) {
				sb.append(parser.getAttributeValue(0));
			}
			while (XmlUtils.nextElement(parser, "locale")) {
				sb.append(',').append(parser.getAttributeValue(0));
			}
		} catch (Exception e) {
			Log.e(TAG, "loadLanguagesList: ", e);
		}
		LocaleListCompat locales = LocaleListCompat.forLanguageTags(sb.toString());
		int size = locales.size();
		String[] languageTags = new String[size + 1];
		String[] languageNames = new String[size + 1];
		languageTags[0] = "";
		languageNames[0] = context.getString(R.string.pref_theme_system);
		for (int i = 0; i < size; ) {
			Locale locale1 = locales.get(i++);
			if (locale1 == null) {
				break;
			}
			languageTags[i] = locale1.getLanguage();
			languageNames[i] = locale1.getDisplayLanguage(locale1);
		}
		prefLanguage.setEntryValues(languageTags);
		prefLanguage.setEntries(languageNames);
		Locale locale = AppCompatDelegate.getApplicationLocales().get(0);
		prefLanguage.setValue(locale != null ? locale.getLanguage() : "");
		prefLanguage.setOnPreferenceChangeListener((preference, value) -> {
			LocaleListCompat list = LocaleListCompat.forLanguageTags((String) value);
			AppCompatDelegate.setApplicationLocales(list);
			return true;
		});
	}

	private void onPickDirResult(Uri uri) {
		if (uri == null || uri.getPath() == null) {
			return;
		}
		File file = new File(uri.getPath());
		String path = file.getAbsolutePath();
		if (!FileUtils.initWorkDir(file)) {
			new AlertDialog.Builder(requireActivity())
					.setTitle(R.string.error)
					.setCancelable(false)
					.setMessage(getString(R.string.create_apps_dir_failed, path))
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.choose, (d, w) -> openDirLauncher.launch(null))
					.show();
			return;
		}
		Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).edit()
				.putString(PREF_EMULATOR_DIR, path)
				.apply();
		prefFolder.setSummary(path);
	}
}
