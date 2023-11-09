/*
 * Copyright 2018 Nikita Shakarun
 * Copyright 2019-2023 Yury Kharchenko
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

import static ru.playsoftware.j2meloader.util.Constants.KEY_CONFIG_PATH;
import static ru.playsoftware.j2meloader.util.Constants.PREF_DEFAULT_PROFILE;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.databinding.DialogSaveProfileBinding;

public class SaveProfileAlert extends DialogFragment {
	private DialogSaveProfileBinding binding;
	private String configPath;

	@NonNull
	public static SaveProfileAlert getInstance(String parent) {
		SaveProfileAlert saveProfileAlert = new SaveProfileAlert();
		Bundle bundleSave = new Bundle();
		bundleSave.putString(KEY_CONFIG_PATH, parent);
		saveProfileAlert.setArguments(bundleSave);
		return saveProfileAlert;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		configPath = requireArguments().getString(KEY_CONFIG_PATH);
		binding = DialogSaveProfileBinding.inflate(getLayoutInflater());
		binding.editText.setFilters(new InputFilter[]{new FileNameInputFilter()});
		return new AlertDialog.Builder(requireActivity())
				.setTitle(R.string.save_profile)
				.setView(binding.getRoot())
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, null)
				.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		Button btPositive = ((AlertDialog) requireDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
		btPositive.setOnClickListener(v -> {
			DialogSaveProfileBinding binding = this.binding;
			if (binding == null) {
				return;
			}
			EditText editText = binding.editText;
			String name = editText.getText().toString().trim();
			if (name.isEmpty()) {
				editText.requestFocus();
				Toast.makeText(requireActivity(), R.string.error_name, Toast.LENGTH_SHORT).show();
				return;
			}

			final File config = new File(Config.getProfilesDir(), name + Config.MIDLET_CONFIG_FILE);
			if (config.exists()) {
				alertRewriteExists(name);
				return;
			}
			save(name);
		});
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		binding = null;
	}

	private void alertRewriteExists(String name) {
		new AlertDialog.Builder(requireContext())
				.setMessage(getString(R.string.alert_rewrite_profile, name))
				.setPositiveButton(android.R.string.ok, (dialog, which) -> save(name))
				.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
					DialogSaveProfileBinding binding = this.binding;
					if (binding == null) {
						return;
					}
					binding.editText.setText(name);
					binding.editText.requestFocus();
					binding.editText.setSelection(0, binding.editText.getText().length());
				})
				.show();
	}

	private void save(String name) {
		DialogSaveProfileBinding binding = this.binding;
		if (binding == null) {
			return;
		}
		try {
			Profile profile = new Profile(name);
			ProfilesManager.save(profile, this.configPath,
					binding.cbConfig.isChecked(), binding.cbKeyboard.isChecked());
			if (binding.cbDefault.isChecked()) {
				PreferenceManager.getDefaultSharedPreferences(requireContext())
						.edit().putString(PREF_DEFAULT_PROFILE, name).apply();
			}
			Toast.makeText(requireContext(), getString(R.string.saved, name), Toast.LENGTH_SHORT).show();
			dismiss();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(requireActivity(), R.string.error, Toast.LENGTH_SHORT).show();
		}
	}
}
