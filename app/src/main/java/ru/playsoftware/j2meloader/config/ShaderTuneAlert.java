/*
 * Copyright 2020-2023 Yury Kharchenko
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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.text.DecimalFormat;
import java.util.Arrays;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.ShaderInfo.Setting;
import ru.playsoftware.j2meloader.databinding.DialogShaderTuneBinding;
import ru.playsoftware.j2meloader.databinding.DialogShaderTuneItemBinding;

public class ShaderTuneAlert extends DialogFragment {
	private static final String SHADER_KEY = "shader";

	private final SeekBar[] seekBars = new SeekBar[4];
	private ShaderInfo shader;
	private Callback callback;
	private float[] values;

	static ShaderTuneAlert newInstance(ShaderInfo shader) {
		ShaderTuneAlert fragment = new ShaderTuneAlert();
		Bundle args = new Bundle();
		args.putParcelable(SHADER_KEY, shader);
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof Callback) {
			callback = ((Callback) context);
		}
		Bundle bundle = requireArguments();
		shader = bundle.getParcelable(SHADER_KEY);
		if (shader == null) {
			Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
			dismiss();
			return;
		}
		values = shader.values;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getLayoutInflater();
		DialogShaderTuneBinding binding = DialogShaderTuneBinding.inflate(inflater);
		Setting[] settings = shader.settings;
		DecimalFormat format = new DecimalFormat("#.######");
		for (int i = 0; i < 4; i++) {
			Setting setting = settings[i];
			if (setting == null) {
				continue;
			}
			DialogShaderTuneItemBinding itemBinding = DialogShaderTuneItemBinding.inflate(inflater, binding.getRoot(), true);
			SeekBar seekBar = itemBinding.sbShaderSettingValue;
			seekBars[i] = seekBar;
			float value = values != null ? values[i] : setting.def;
			itemBinding.tvShaderSettingName.setText(getString(R.string.shader_setting, setting.name, format.format(value)));
			if (setting.step <= 0.0f) {
				setting.step = (setting.max - setting.min) / 100.0f;
			}
			seekBar.setMax((int) ((setting.max - setting.min) / setting.step));
			int progress = (int) ((value - setting.min) / setting.step);
			seekBar.setProgress(progress);
			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					String value = format.format(setting.min + (progress * setting.step));
					itemBinding.tvShaderSettingName.setText(getString(R.string.shader_setting, setting.name, value));
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
		}
		return new AlertDialog.Builder(requireActivity())
				.setTitle(R.string.shader_tuning)
				.setView(binding.getRoot())
				.setPositiveButton(android.R.string.ok, (d, w) -> onClickOk())
				.setNegativeButton(android.R.string.cancel, null)
				.setNeutralButton(R.string.reset, null)
				.create();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog dialog = (AlertDialog) requireDialog();
		dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> onClickReset());
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		Arrays.fill(seekBars, null);
	}

	private void onClickReset() {
		for (int i = 0; i < 4; i++) {
			SeekBar seekBar = seekBars[i];
			if (seekBar != null) {
				Setting s = shader.settings[i];
				int progress = (int) ((s.def - s.min) / s.step);
				seekBar.setProgress(progress);
			}
		}
	}

	private void onClickOk() {
		float[] values = new float[4];
		for (int i = 0, sbValuesLength = seekBars.length; i < sbValuesLength; i++) {
			SeekBar bar = seekBars[i];
			if (bar != null) {
				Setting setting = shader.settings[i];
				values[i] = bar.getProgress() * setting.step + setting.min;
			}
		}
		callback.onTuneComplete(values);
	}

	interface Callback {
		void onTuneComplete(float[] values);
	}
}
