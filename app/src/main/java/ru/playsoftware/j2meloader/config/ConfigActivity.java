/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2015-2016 Nickolay Savchenko
 * Copyright 2018-2019 Nikita Shakarun
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

import static ru.playsoftware.j2meloader.util.Constants.ACTION_EDIT;
import static ru.playsoftware.j2meloader.util.Constants.ACTION_EDIT_PROFILE;
import static ru.playsoftware.j2meloader.util.Constants.KEY_MIDLET_NAME;
import static ru.playsoftware.j2meloader.util.Constants.PREF_DEFAULT_PROFILE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.microedition.shell.MicroActivity;
import javax.microedition.util.ContextHolder;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.model.Size;
import ru.playsoftware.j2meloader.databinding.ActivityConfigBinding;
import ru.playsoftware.j2meloader.settings.KeyMapperActivity;
import ru.playsoftware.j2meloader.util.FileUtils;
import ru.playsoftware.j2meloader.util.ViewUtils;
import yuku.ambilwarna.AmbilWarnaDialog;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener, ShaderTuneAlert.Callback {
	private static final String TAG = ConfigActivity.class.getSimpleName();

	protected ArrayList<Size> screenPresets = new ArrayList<>();
	protected ArrayList<int[]> fontPresetValues = new ArrayList<>();
	protected ArrayList<String> fontPresetTitles = new ArrayList<>();

	private File keylayoutFile;
	private File dataDir;
	private ProfileModel params;
	private boolean isProfile;
	private Display display;
	private File configDir;
	private String defProfile;
	private ArrayAdapter<ShaderInfo> spShaderAdapter;
	private String workDir;
	private boolean needShow;
	private ArrayAdapter<String> spSoundBankAdapter;
	private ActivityConfigBinding binding;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String action = intent.getAction();
		isProfile = ACTION_EDIT_PROFILE.equals(action);
		needShow = isProfile || ACTION_EDIT.equals(action);
		String path = intent.getDataString();
		if (path == null) {
			needShow = false;
			onBackPressed();
			return;
		}
		if (isProfile) {
			setResult(RESULT_OK, new Intent().setData(intent.getData()));
			configDir = new File(Config.getProfilesDir(), path);
			workDir = Config.getEmulatorDir();
			setTitle(path);
		} else {
			setTitle(intent.getStringExtra(KEY_MIDLET_NAME));
			File appDir = new File(path);
			File convertedDir = appDir.getParentFile();
			if (!appDir.isDirectory() || convertedDir == null
					|| (workDir = convertedDir.getParent()) == null) {
				needShow = false;
				String storageName = "";
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
					StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
					if (sm != null) {
						StorageVolume storageVolume = sm.getStorageVolume(appDir);
						if (storageVolume != null) {
							String desc = storageVolume.getDescription(this);
							if (desc != null) {
								storageName = "\"" + desc + "\" ";
							}
						}
					}
				}
				new AlertDialog.Builder(this)
						.setTitle(R.string.error)
						.setMessage(getString(R.string.err_missing_app, storageName))
						.setPositiveButton(R.string.exit, (d, w) -> onBackPressed())
						.setCancelable(false)
						.show();
				return;
			}
			dataDir = new File(workDir + Config.MIDLET_DATA_DIR + appDir.getName());
			dataDir.mkdirs();
			configDir = new File(workDir + Config.MIDLET_CONFIGS_DIR + appDir.getName());
		}
		configDir.mkdirs();

		defProfile = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getString(PREF_DEFAULT_PROFILE, null);
		loadConfig();
		if (!params.isNew && !needShow) {
			startMIDlet();
			return;
		}
		loadKeyLayout();
		binding = ActivityConfigBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		display = getWindowManager().getDefaultDisplay();

		fillScreenSizePresets(display.getWidth(), display.getHeight());

		addFontSizePreset("128 x 128", 9, 13, 15);
		addFontSizePreset("128 x 160", 13, 15, 20);
		addFontSizePreset("176 x 220", 15, 18, 22);
		addFontSizePreset("240 x 320", 18, 22, 26);

		binding.cbLockAspect.setOnCheckedChangeListener(this::onLockAspectChanged);
		binding.cmdScreenSizePresets.setOnClickListener(this::showScreenPresets);
		binding.cmdSwapSizes.setOnClickListener(this);
		binding.cmdAddToPreset.setOnClickListener(v -> addResolutionToPresets());
		binding.cmdFontSizePresets.setOnClickListener(this);
		binding.cmdScreenBack.setOnClickListener(this);
		binding.cmdKeyMappings.setOnClickListener(this);
		binding.cmdVKBack.setOnClickListener(this);
		binding.cmdVKFore.setOnClickListener(this);
		binding.cmdVKSelBack.setOnClickListener(this);
		binding.cmdVKSelFore.setOnClickListener(this);
		binding.cmdVKOutline.setOnClickListener(this);
		binding.btEncoding.setOnClickListener(this::showCharsetPicker);
		binding.btShaderTune.setOnClickListener(this::showShaderSettings);
		binding.tfScaleRatioValue.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int length = s.length();
				if (length > 4) {
					int st = Math.min(start + count, 4);
					int end = st + length - 4;
					binding.tfScaleRatioValue.getText().delete(st, end);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) return;
				try {
					int progress = Integer.parseInt(s.toString());
					if (progress > 1000) {
						s.replace(0, s.length(), "1000");
					}
				} catch (NumberFormatException e) {
					s.clear();
				}
			}
		});
		binding.spGraphicsMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
					case 3:
						binding.cxParallel.setVisibility(View.VISIBLE);
						binding.shaderContainer.setVisibility(View.GONE);
						break;
					case 1:
						binding.cxParallel.setVisibility(View.GONE);
						binding.shaderContainer.setVisibility(View.VISIBLE);
						initShaderSpinner();
						break;
					case 2:
						binding.cxParallel.setVisibility(View.GONE);
						binding.shaderContainer.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		binding.cxIsShowKeyboard.setOnClickListener((b) -> {
			View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					View focus = binding.getRoot().findFocus();
					if (focus != null) focus.clearFocus();
					v.scrollTo(0, binding.rootInputConfig.getTop());
					v.removeOnLayoutChangeListener(this);
				}
			};
			binding.getRoot().addOnLayoutChangeListener(onLayoutChangeListener);
			binding.groupVkConfig.setVisibility(binding.cxIsShowKeyboard.isChecked() ? View.VISIBLE : View.GONE);
		});
		binding.tfScreenBack.addTextChangedListener(new ColorTextWatcher(binding.tfScreenBack));
		binding.tfVKFore.addTextChangedListener(new ColorTextWatcher(binding.tfVKFore));
		binding.tfVKBack.addTextChangedListener(new ColorTextWatcher(binding.tfVKBack));
		binding.tfVKSelFore.addTextChangedListener(new ColorTextWatcher(binding.tfVKSelFore));
		binding.tfVKSelBack.addTextChangedListener(new ColorTextWatcher(binding.tfVKSelBack));
		binding.tfVKOutline.addTextChangedListener(new ColorTextWatcher(binding.tfVKOutline));
		initSoundBankSpinner();
	}

	private void initSoundBankSpinner() {
		File dir = new File(workDir + Config.SOUNDBANKS_DIR);
		if (!dir.exists()) {
			//noinspection ResultOfMethodCallIgnored
			dir.mkdirs();
		}
		spSoundBankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
		spSoundBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		binding.spSoundBank.setAdapter(spSoundBankAdapter);
		spSoundBankAdapter.add(getString(R.string.default_label, "Android"));
		String[] files = dir.list((d, n) -> new File(d, n).isFile());
		if (files != null) {
			Arrays.sort(files, (o1, o2) -> {
				int res = o1.toLowerCase().compareTo(o2.toLowerCase());
				if (res != 0) {
					return res;
				}
				return o1.compareTo(o2);
			});
			spSoundBankAdapter.addAll(files);
		}
		spSoundBankAdapter.notifyDataSetChanged();
		int position = spSoundBankAdapter.getPosition(params.soundBank);
		binding.spSoundBank.setSelection(Math.max(position, 0));
	}

	private void onLockAspectChanged(CompoundButton cb, boolean isChecked) {
		if (isChecked) {
			float w;
			try {
				w = Integer.parseInt(binding.tfScreenWidth.getText().toString());
			} catch (Exception ignored) {
				w = 0;
			}
			if (w <= 0) {
				cb.setChecked(false);
				return;
			}
			float h;
			try {
				h = Integer.parseInt(binding.tfScreenHeight.getText().toString());
			} catch (Exception ignored) {
				h = 0;
			}
			if (h <= 0) {
				cb.setChecked(false);
				return;
			}
			float finalW = w;
			float finalH = h;
			binding.tfScreenWidth.setOnFocusChangeListener(new ResolutionAutoFill(binding.tfScreenWidth, binding.tfScreenHeight, finalH / finalW));
			binding.tfScreenHeight.setOnFocusChangeListener(new ResolutionAutoFill(binding.tfScreenHeight, binding.tfScreenWidth, finalW / finalH));

		} else {
			View.OnFocusChangeListener listener = binding.tfScreenWidth.getOnFocusChangeListener();
			if (listener != null) {
				listener.onFocusChange(binding.tfScreenWidth, false);
				binding.tfScreenWidth.setOnFocusChangeListener(null);
			}
			listener = binding.tfScreenHeight.getOnFocusChangeListener();
			if (listener != null) {
				listener.onFocusChange(binding.tfScreenHeight, false);
				binding.tfScreenHeight.setOnFocusChangeListener(null);
			}
		}
	}

	void loadConfig() {
		params = ProfilesManager.loadConfig(configDir);
		if (params == null && defProfile != null) {
			FileUtils.copyFiles(new File(Config.getProfilesDir(), defProfile), configDir, null);
			params = ProfilesManager.loadConfig(configDir);
		}
		if (params == null) {
			params = new ProfileModel(configDir);
		}
	}

	private void showShaderSettings(View v) {
		ShaderInfo shader = (ShaderInfo) binding.spShader.getSelectedItem();
		params.shader = shader;
		ShaderTuneAlert.newInstance(shader).show(getSupportFragmentManager(), "ShaderTuning");
	}

	private void initShaderSpinner() {
		if (spShaderAdapter != null) {
			return;
		}
		File dir = new File(workDir + Config.SHADERS_DIR);
		if (!dir.exists()) {
			//noinspection ResultOfMethodCallIgnored
			dir.mkdirs();
		}
		ArrayList<ShaderInfo> list = new ArrayList<>();
		spShaderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
		spShaderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		binding.spShader.setAdapter(spShaderAdapter);
		File[] files = dir.listFiles((f) -> f.isFile() && f.getName().toLowerCase().endsWith(".ini"));
		if (files != null) {
			for (File file : files) {
				String text = FileUtils.getText(file.getAbsolutePath());
				String[] split = text.split("[\\n\\r]+");
				ShaderInfo info = null;
				for (String line : split) {
					if (line.startsWith("[")) {
						if (info != null && info.fragment != null && info.vertex != null) {
							list.add(info);
						}
						info = new ShaderInfo(line.replaceAll("[\\[\\]]", ""), "unknown");
					} else if (info != null) {
						try {
							info.set(line);
						} catch (Exception e) {
							Log.e(TAG, "initShaderSpinner: ", e);
						}
					}
				}
				if (info != null && info.fragment != null && info.vertex != null) {
					list.add(info);
				}
			}
			Collections.sort(list);
		}
		list.add(0, new ShaderInfo(getString(R.string.identity_filter), "woesss"));
		spShaderAdapter.notifyDataSetChanged();
		ShaderInfo selected = params.shader;
		if (selected != null) {
			int position = list.indexOf(selected);
			if (position > 0) {
				list.get(position).values = selected.values;
				binding.spShader.setSelection(position);
			}
		}
		binding.spShader.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ShaderInfo item = (ShaderInfo) parent.getItemAtPosition(position);
				ShaderInfo.Setting[] settings = item.settings;
				float[] values = item.values;
				if (values == null) {
					for (int i = 0; i < 4; i++) {
						if (settings[i] != null) {
							if (values == null) {
								values = new float[4];
							}
							values[i] = settings[i].def;
						}
					}
				}
				if (values == null) {
					binding.btShaderTune.setVisibility(View.GONE);
				} else {
					item.values = values;
					binding.btShaderTune.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void showCharsetPicker(View v) {
		String[] charsets = Charset.availableCharsets().keySet().toArray(new String[0]);
		new AlertDialog.Builder(this).setItems(charsets, (d, w) -> {
			String text = binding.tfSystemProperties.getText().toString();
			String key = "microedition.encoding:";
			int idx = text.lastIndexOf(key);
			if (idx != -1) {
				int nl = text.indexOf('\n', idx);
				text = text.substring(0, idx + key.length()) + " " + charsets[w] + (nl == -1 ? "\n" : text.substring(nl));
				binding.tfSystemProperties.setText(text);
				return;
			}

			if (!text.endsWith("\n")) {
				binding.tfSystemProperties.append("\n");
			}
			binding.tfSystemProperties.append(key);
			binding.tfSystemProperties.append(" ");
			binding.tfSystemProperties.append(charsets[w]);
			binding.tfSystemProperties.append("\n");
		}).setTitle(R.string.pref_encoding_title).show();
	}

	private void loadKeyLayout() {
		File file = new File(configDir, Config.MIDLET_KEY_LAYOUT_FILE);
		keylayoutFile = file;
		if (isProfile || file.exists()) {
			return;
		}
		if (defProfile == null) {
			return;
		}
		File defaultKeyLayoutFile = new File(Config.getProfilesDir() + defProfile, Config.MIDLET_KEY_LAYOUT_FILE);
		if (!defaultKeyLayoutFile.exists()) {
			return;
		}
		try {
			FileUtils.copyFileUsingChannel(defaultKeyLayoutFile, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		if (needShow && configDir != null) {
			saveParams();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (needShow) {
			loadParams(true);
		}
	}

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		fillScreenSizePresets(display.getWidth(), display.getHeight());
	}

	private void fillScreenSizePresets(int w, int h) {
		ArrayList<Size> screenPresets = this.screenPresets;
		screenPresets.clear();

		screenPresets.add(new Size(128, 128));
		screenPresets.add(new Size(128, 160));
		screenPresets.add(new Size(132, 176));
		screenPresets.add(new Size(176, 220));
		screenPresets.add(new Size(240, 320));
		screenPresets.add(new Size(352, 416));
		screenPresets.add(new Size(640, 360));
		screenPresets.add(new Size(800, 480));

		if (w > h) {
			screenPresets.add(new Size(h * 3 / 4, h));
			screenPresets.add(new Size(h * 4 / 3, h));
		} else {
			screenPresets.add(new Size(w, w * 4 / 3));
			screenPresets.add(new Size(w, w * 3 / 4));
		}

		screenPresets.add(new Size(w, h));
		Set<String> preset = PreferenceManager.getDefaultSharedPreferences(this)
				.getStringSet("ResolutionsPreset", null);
		if (preset != null) {
			for (String s : preset) {
				Size size = Size.parse(s);
				if (size != null) {
					screenPresets.add(size);
				}
			}
		}
		Collections.sort(screenPresets);
		Size prev = null;
		for (Iterator<Size> iterator = screenPresets.iterator(); iterator.hasNext(); ) {
			Size next = iterator.next();
			if (next.equals(prev)) iterator.remove();
			else prev = next;
		}
	}

	private void addFontSizePreset(String title, int small, int medium, int large) {
		fontPresetValues.add(new int[]{small, medium, large});
		fontPresetTitles.add(title);
	}

	@SuppressLint("SetTextI18n")
	public void loadParams(boolean reloadFromFile) {
		if (reloadFromFile) {
			loadConfig();
		}
		int screenWidth = params.screenWidth;
		if (screenWidth != 0) {
			binding.tfScreenWidth.setText(Integer.toString(screenWidth));
		}
		int screenHeight = params.screenHeight;
		if (screenHeight != 0) {
			binding.tfScreenHeight.setText(Integer.toString(screenHeight));
		}
		binding.tfScreenBack.setText(String.format("%06X", params.screenBackgroundColor));
		binding.tfScaleRatioValue.setText(Integer.toString(params.screenScaleRatio));
		binding.spOrientation.setSelection(params.orientation);
		binding.spScaleType.setSelection(params.screenScaleType);
		binding.spScreenGravity.setSelection(params.screenGravity);
		binding.cxFilter.setChecked(params.screenFilter);
		binding.cxImmediate.setChecked(params.immediateMode);
		binding.cxParallel.setChecked(params.parallelRedrawScreen);
		binding.cxForceFullscreen.setChecked(params.forceFullscreen);
		binding.spGraphicsMode.setSelection(params.graphicsMode);
		binding.cxShowFps.setChecked(params.showFps);

		binding.tfFontSizeSmall.setText(Integer.toString(params.fontSizeSmall));
		binding.tfFontSizeMedium.setText(Integer.toString(params.fontSizeMedium));
		binding.tfFontSizeLarge.setText(Integer.toString(params.fontSizeLarge));
		binding.cxFontSizeInSP.setChecked(params.fontApplyDimensions);
		binding.cxFontAA.setChecked(params.fontAA);
		boolean showVk = params.showKeyboard;
		binding.cxIsShowKeyboard.setChecked(showVk);
		binding.groupVkConfig.setVisibility(showVk ? View.VISIBLE : View.GONE);
		binding.cxVKFeedback.setChecked(params.vkFeedback);
		binding.cxVKForceOpacity.setChecked(params.vkForceOpacity);
		binding.cxTouchInput.setChecked(params.touchInput);
		int fpsLimit = params.fpsLimit;
		binding.etFpsLimit.setText(fpsLimit > 0 ? Integer.toString(fpsLimit) : "");

		binding.spLayout.setSelection(params.keyCodesLayout);
		binding.spButtonsShape.setSelection(params.vkButtonShape);
		binding.sbVKAlpha.setProgress(params.vkAlpha);
		int vkHideDelay = params.vkHideDelay;
		binding.tfVKHideDelay.setText(vkHideDelay > 0 ? Integer.toString(vkHideDelay) : "");

		binding.tfVKBack.setText(String.format("%06X", params.vkBgColor));
		binding.tfVKFore.setText(String.format("%06X", params.vkFgColor));
		binding.tfVKSelBack.setText(String.format("%06X", params.vkBgColorSelected));
		binding.tfVKSelFore.setText(String.format("%06X", params.vkFgColorSelected));
		binding.tfVKOutline.setText(String.format("%06X", params.vkOutlineColor));

		String systemProperties = params.systemProperties;
		if (systemProperties == null) {
			systemProperties = ContextHolder.getAssetAsString("defaults/system.props");
		}
		binding.tfSystemProperties.setText(getSystemProperties(systemProperties));
	}

	private void saveParams() {
		try {
			int width;
			try {
				width = Integer.parseInt(binding.tfScreenWidth.getText().toString());
			} catch (NumberFormatException e) {
				width = 0;
			}
			params.screenWidth = width;
			int height;
			try {
				height = Integer.parseInt(binding.tfScreenHeight.getText().toString());
			} catch (NumberFormatException e) {
				height = 0;
			}
			params.screenHeight = height;
			try {
				params.screenBackgroundColor = Integer.parseInt(binding.tfScreenBack.getText().toString(), 16);
			} catch (NumberFormatException ignored) {
			}
			try {
				params.screenScaleRatio = Integer.parseInt(binding.tfScaleRatioValue.getText().toString());
			} catch (NumberFormatException e) {
				params.screenScaleRatio = 100;
			}
			params.orientation = binding.spOrientation.getSelectedItemPosition();
			params.screenGravity = binding.spScreenGravity.getSelectedItemPosition();
			params.screenScaleType = binding.spScaleType.getSelectedItemPosition();
			params.screenFilter = binding.cxFilter.isChecked();
			params.immediateMode = binding.cxImmediate.isChecked();
			int mode = binding.spGraphicsMode.getSelectedItemPosition();
			params.graphicsMode = mode;
			if (mode == 1) {
				if (binding.spShader.getSelectedItemPosition() == 0)
					params.shader = null;
				else
					params.shader = (ShaderInfo) binding.spShader.getSelectedItem();
			}
			params.parallelRedrawScreen = binding.cxParallel.isChecked();
			params.forceFullscreen = binding.cxForceFullscreen.isChecked();
			params.showFps = binding.cxShowFps.isChecked();
			try {
				params.fpsLimit = Integer.parseInt(binding.etFpsLimit.getText().toString());
			} catch (NumberFormatException e) {
				params.fpsLimit = 0;
			}

			try {
				params.fontSizeSmall = Integer.parseInt(binding.tfFontSizeSmall.getText().toString());
			} catch (NumberFormatException e) {
				params.fontSizeSmall = 0;
			}
			try {
				params.fontSizeMedium = Integer.parseInt(binding.tfFontSizeMedium.getText().toString());
			} catch (NumberFormatException e) {
				params.fontSizeMedium = 0;
			}
			try {
				params.fontSizeLarge = Integer.parseInt(binding.tfFontSizeLarge.getText().toString());
			} catch (NumberFormatException e) {
				params.fontSizeLarge = 0;
			}
			params.fontApplyDimensions = binding.cxFontSizeInSP.isChecked();
			params.fontAA = binding.cxFontAA.isChecked();
			params.showKeyboard = binding.cxIsShowKeyboard.isChecked();
			params.vkFeedback = binding.cxVKFeedback.isChecked();
			params.vkForceOpacity = binding.cxVKForceOpacity.isChecked();
			params.touchInput = binding.cxTouchInput.isChecked();

			params.keyCodesLayout = binding.spLayout.getSelectedItemPosition();
			params.vkButtonShape = binding.spButtonsShape.getSelectedItemPosition();
			params.vkAlpha = binding.sbVKAlpha.getProgress();
			try {
				params.vkHideDelay = Integer.parseInt(binding.tfVKHideDelay.getText().toString());
			} catch (NumberFormatException e) {
				params.vkHideDelay = 0;
			}
			try {
				params.vkBgColor = Integer.parseInt(binding.tfVKBack.getText().toString(), 16);
			} catch (Exception ignored) {
			}
			try {
				params.vkFgColor = Integer.parseInt(binding.tfVKFore.getText().toString(), 16);
			} catch (Exception ignored) {
			}
			try {
				params.vkBgColorSelected = Integer.parseInt(binding.tfVKSelBack.getText().toString(), 16);
			} catch (Exception ignored) {
			}
			try {
				params.vkFgColorSelected = Integer.parseInt(binding.tfVKSelFore.getText().toString(), 16);
			} catch (Exception ignored) {
			}
			try {
				params.vkOutlineColor = Integer.parseInt(binding.tfVKOutline.getText().toString(), 16);
			} catch (Exception ignored) {
			}
			params.soundBank = binding.spSoundBank.getSelectedItemPosition() > 0 ? (String) binding.spSoundBank.getSelectedItem() : null;
			params.systemProperties = getSystemProperties(binding.tfSystemProperties.getText().toString());

			ProfilesManager.saveConfig(params);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@NonNull
	private String getSystemProperties(String text) {
		String[] lines = text.split("[\\r\\n]+");
		ArrayList<String> list = new ArrayList<>();
		Set<String> keys = new HashSet<>();
		for (int i = lines.length - 1; i >= 0; i--) {
			String line = lines[i];
			int colon = line.indexOf(':');
			if (colon != -1 && keys.add(line.substring(0, colon).trim())) {
				list.add(line);
			}
		}
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		for (String string : list) {
			sb.append(string);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.config, menu);
		if (isProfile) {
			menu.findItem(R.id.action_start).setVisible(false);
			menu.findItem(R.id.action_clear_data).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_start) {
			startMIDlet();
		} else if (itemId == R.id.action_clear_data) {
			showClearDataDialog();
		} else if (itemId == R.id.action_reset_settings) {
			params = new ProfileModel(configDir);
			loadParams(false);
		} else if (itemId == R.id.action_reset_layout) {
			//noinspection ResultOfMethodCallIgnored
			keylayoutFile.delete();
			loadKeyLayout();
		} else if (itemId == R.id.action_load_profile) {
			LoadProfileAlert.newInstance(keylayoutFile.getParent())
					.show(getSupportFragmentManager(), "load_profile");
		} else if (itemId == R.id.action_save_profile) {
			saveParams();
			SaveProfileAlert.getInstance(keylayoutFile.getParent())
					.show(getSupportFragmentManager(), "save_profile");
		} else if (itemId == android.R.id.home) {
			onBackPressed();
		} else {
			return false;
		}
		return true;
	}

	private void showClearDataDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(R.string.message_clear_data)
				.setPositiveButton(android.R.string.ok, (d, w) -> FileUtils.clearDirectory(dataDir))
				.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}

	private void startMIDlet() {
		if (needShow && configDir != null) {
			saveParams();
		}
		Intent i = new Intent(this, MicroActivity.class);
		i.setData(getIntent().getData());
		i.putExtra(KEY_MIDLET_NAME, getIntent().getStringExtra(KEY_MIDLET_NAME));
		startActivity(i);
		onBackPressed();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.cmdSwapSizes) {
			String tmp = binding.tfScreenWidth.getText().toString();
			binding.tfScreenWidth.setText(binding.tfScreenHeight.getText().toString());
			binding.tfScreenHeight.setText(tmp);
		} else if (id == R.id.cmdFontSizePresets) {
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.SIZE_PRESETS))
					.setItems(fontPresetTitles.toArray(new String[0]),
							(dialog, which) -> {
								int[] values = fontPresetValues.get(which);
								binding.tfFontSizeSmall.setText(Integer.toString(values[0]));
								binding.tfFontSizeMedium.setText(Integer.toString(values[1]));
								binding.tfFontSizeLarge.setText(Integer.toString(values[2]));
							})
					.show();
		} else if (id == R.id.cmdScreenBack) {
			showColorPicker(binding.tfScreenBack);
		} else if (id == R.id.cmdVKBack) {
			showColorPicker(binding.tfVKBack);
		} else if (id == R.id.cmdVKFore) {
			showColorPicker(binding.tfVKFore);
		} else if (id == R.id.cmdVKSelFore) {
			showColorPicker(binding.tfVKSelFore);
		} else if (id == R.id.cmdVKSelBack) {
			showColorPicker(binding.tfVKSelBack);
		} else if (id == R.id.cmdVKOutline) {
			showColorPicker(binding.tfVKOutline);
		} else if (id == R.id.cmdKeyMappings) {
			Intent i = new Intent(getIntent().getAction(), Uri.parse(configDir.getPath()),
					this, KeyMapperActivity.class);
			startActivity(i);
		}
	}

	@SuppressLint("SetTextI18n")
	private void showScreenPresets(View v) {
		ListPopupWindow popup = new ListPopupWindow(this);
		popup.setAnchorView(v);
		popup.setModal(true);
		ArrayAdapter<Size> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, screenPresets);
		popup.setAdapter(adapter);
		final Resources res = getResources();
		int maxWidth = res.getDisplayMetrics().widthPixels;
		popup.setWidth(ViewUtils.measureListViewWidth(adapter, null, this, maxWidth));
		popup.setOnItemClickListener((parent, view, position, id) -> {
			Size size = ((Size) parent.getItemAtPosition(position));
			binding.tfScreenWidth.setText(Integer.toString(size.width));
			binding.tfScreenHeight.setText(Integer.toString(size.height));
			popup.dismiss();
		});
		popup.show();
	}

	private void showColorPicker(EditText et) {
		AmbilWarnaDialog.OnAmbilWarnaListener colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@SuppressLint("NewApi")
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				et.setText(String.format("%06X", color & 0xFFFFFF));
				ColorDrawable drawable = (ColorDrawable) TextViewCompat.getCompoundDrawablesRelative(et)[2];
				drawable.setColor(color);
			}

			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
			}
		};

		int color;
		try {
			color = Integer.parseInt(et.getText().toString().trim(), 16);
		} catch (NumberFormatException ignored) {
			color = 0;
		}
		new AmbilWarnaDialog(this, color | 0xFF000000, colorListener).show();
	}

	private void addResolutionToPresets() {
		String width = binding.tfScreenWidth.getText().toString();
		String height = binding.tfScreenHeight.getText().toString();
		int w;
		try {
			w = Integer.parseInt(width);
		} catch (NumberFormatException e) {
			Toast.makeText(this, R.string.invalid_resolution_not_saved, Toast.LENGTH_SHORT).show();
			return;
		}
		int h;
		try {
			h = Integer.parseInt(height);
		} catch (NumberFormatException e) {
			Toast.makeText(this, R.string.invalid_resolution_not_saved, Toast.LENGTH_SHORT).show();
			return;
		}
		if (w <= 0 || h <= 0) {
			Toast.makeText(this, R.string.invalid_resolution_not_saved, Toast.LENGTH_SHORT).show();
			return;
		}
		String preset = width + " x " + height;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> set = preferences.getStringSet("ResolutionsPreset", null);
		if (set == null) {
			set = new HashSet<>(1);
		}
		if (set.add(preset)) {
			preferences.edit().putStringSet("ResolutionsPreset", set).apply();
			screenPresets.add(new Size(w, h));
			Toast.makeText(this, getString(R.string.saved, preset), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.not_saved_exists, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onTuneComplete(float[] values) {
		params.shader.values = values;
	}

	private static class ColorTextWatcher implements TextWatcher {
		private final EditText editText;
		private final ColorDrawable drawable;

		@SuppressLint("NewApi")
		ColorTextWatcher(EditText editText) {
			this.editText = editText;
			int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,
					editText.getResources().getDisplayMetrics());
			ColorDrawable colorDrawable = new ColorDrawable();
			colorDrawable.setBounds(0, 0, size, size);
			TextViewCompat.setCompoundDrawablesRelative(editText, null, null, colorDrawable, null);
			drawable = colorDrawable;
			editText.setFilters(new InputFilter[]{this::filter});
		}

		private CharSequence filter(CharSequence src, int ss, int se, Spanned dst, int ds, int de) {
			StringBuilder sb = new StringBuilder(se - ss);
			for (int i = ss; i < se; i++) {
				char c = src.charAt(i);
				if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
					sb.append(c);
				} else if (c >= 'a' && c <= 'f') {
					sb.append((char) (c - 32));
				}
			}
			return sb;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			int length = s.length();
			if (length > 6) {
				int st = Math.min(start + count, 6);
				int end = st + (length - 6);
				editText.getText().delete(st, end);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 0) return;
			try {
				int color = Integer.parseInt(s.toString(), 16);
				drawable.setColor(color | Color.BLACK);
			} catch (NumberFormatException e) {
				drawable.setColor(Color.BLACK);
				s.clear();
			}
		}
	}

	private static class ResolutionAutoFill implements TextWatcher, View.OnFocusChangeListener {
		private final EditText src;
		private final EditText dst;
		private final float aspect;

		public ResolutionAutoFill(EditText src, EditText dst, float aspect) {
			this.src = src;
			this.dst = dst;
			this.aspect = aspect;
			if (src.hasFocus())
				src.addTextChangedListener(this);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			try {
				int size = Integer.parseInt(src.getText().toString());
				if (size <= 0) return;
				int value = Math.round(size * aspect);
				dst.setText(String.valueOf(value));
			} catch (NumberFormatException ignored) {}
		}

		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				src.addTextChangedListener(this);
			} else {
				src.removeTextChangedListener(this);
			}
		}
	}
}
