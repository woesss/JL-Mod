/*
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

package ru.woesss.j2me.installer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.applist.AppItem;
import ru.playsoftware.j2meloader.applist.AppListModel;
import ru.playsoftware.j2meloader.appsdb.AppRepository;
import ru.playsoftware.j2meloader.config.Config;
import ru.playsoftware.j2meloader.databinding.FragmentInstallerBinding;
import ru.woesss.j2me.jar.Descriptor;

public class InstallerDialog extends DialogFragment {
	private static final String ARG_URI = "InstallerDialog.uri";
	private static final String ARG_ID = "InstallerDialog.id";
	private final CompositeDisposable compositeDisposable = new CompositeDisposable();

	private FragmentInstallerBinding binding;
	private Button btnOk;
	private Button btnClose;
	private Button btnRun;
	private AppRepository appRepository;
	private AppInstaller installer;
	private AlertDialog dialog;

	/**
	 * @param uri original uri from intent.
	 * @return A new instance of fragment InstallerDialog.
	 */
	public static InstallerDialog newInstance(Uri uri) {
		InstallerDialog fragment = new InstallerDialog();
		Bundle args = new Bundle();
		args.putParcelable(ARG_URI, uri);
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	public static InstallerDialog newInstance(int id) {
		InstallerDialog fragment = new InstallerDialog();
		Bundle args = new Bundle();
		args.putInt(ARG_ID, id);
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		AppListModel appListModel = new ViewModelProvider(requireActivity()).get(AppListModel.class);
		appRepository = appListModel.getAppRepository();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			dismissAllowingStateLoss();
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		binding = FragmentInstallerBinding.inflate(getLayoutInflater());
		dialog = new AlertDialog.Builder(requireActivity(), getTheme())
				.setIcon(R.mipmap.ic_launcher)
				.setView(binding.getRoot())
				.setTitle("MIDlet installer")
				.setMessage("")
				.setCancelable(false)
				.setPositiveButton(R.string.install, null)
				.setNegativeButton(android.R.string.cancel, null)
				.setNeutralButton(R.string.START_CMD, null)
				.create();
		return dialog;
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		binding = null;
	}

	@Override
	public void onDestroy() {
		compositeDisposable.dispose();
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (installer != null) {
			return;
		}
		btnOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		btnClose = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		btnRun = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
		hideButtons();
		Bundle args = requireArguments();
		Uri uri = args.getParcelable(ARG_URI);
		if (uri != null) {
			installApp(null, uri);
			return;
		}
		int id = args.getInt(ARG_ID);
		reinstallApp(id);
	}

	private void installApp(String path, Uri uri) {
		installer = new AppInstaller(path, uri, requireActivity().getApplication(), appRepository);
		btnClose.setOnClickListener(v -> {
			installer.deleteTemp();
			installer.clearCache();
			dismiss();
		});
		Disposable disposable = Single.create(installer::loadInfo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onProgress, this::onError);
		compositeDisposable.add(disposable);
	}

	private void reinstallApp(int id) {
		installer = new AppInstaller(id, requireActivity().getApplication(), appRepository);
		btnClose.setOnClickListener(v -> {
			installer.deleteTemp();
			installer.clearCache();
			dismiss();
		});
		Disposable disposable = Single.create(installer::loadInfo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onProgress, this::onError);
		compositeDisposable.add(disposable);
	}

	private void hideProgress() {
		binding.progress.setVisibility(View.GONE);
		binding.tvStatus.setVisibility(View.GONE);
	}

	private void showProgress() {
		binding.progress.setVisibility(View.VISIBLE);
		binding.tvStatus.setVisibility(View.VISIBLE);
	}

	private void hideButtons() {
		btnOk.setVisibility(View.GONE);
		btnClose.setVisibility(View.GONE);
		btnRun.setVisibility(View.GONE);
	}

	private void showButtons() {
		btnOk.setVisibility(View.VISIBLE);
		btnClose.setVisibility(View.VISIBLE);
	}

	private void convert() {
		Descriptor nd = installer.getNewDescriptor();
		SpannableStringBuilder info = nd.getInfo(requireActivity());
		dialog.setMessage(info);
		binding.tvStatus.setText(R.string.converting_wait);
		showProgress();
		hideButtons();
		Disposable disposable = Single.create(installer::install)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::onProgress, this::onError);
		compositeDisposable.add(disposable);
	}

	private void alertConfirm(SpannableStringBuilder message,
							  View.OnClickListener positive) {
		hideProgress();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(message);
		btnOk.setOnClickListener(positive);
		showButtons();
	}

	private void onProgress(@NonNull Integer status) {
		if (!isAdded()) {
			return;
		}
		if (status == AppInstaller.STATUS_SUCCESS) {
			binding.progress.setVisibility(View.GONE);
			binding.tvStatus.setText(getString(R.string.install_done));
			AppItem app = installer.getExistsApp();
			Drawable drawable = Drawable.createFromPath(app.getImagePathExt());
			if (drawable != null) dialog.setIcon(drawable);
			btnOk.setText(R.string.START_CMD);
			btnOk.setOnClickListener(v -> {
				Config.startApp(v.getContext(), app.getTitle(), app.getPathExt(), false);
				dismiss();
			});
			btnClose.setText(R.string.close);
			showButtons();
			return;
		}
		Descriptor nd = installer.getNewDescriptor();
		SpannableStringBuilder message;
		switch (status) {
			case AppInstaller.STATUS_NEW -> {
				if (installer.getJar() != null) {
					convert();
					return;
				}
				message = nd.getInfo(requireActivity());
			}
			case AppInstaller.STATUS_OLDEST -> message = new SpannableStringBuilder(getString(
					R.string.reinstall_older,
					nd.getVersion(),
					installer.getCurrentVersion()));
			case AppInstaller.STATUS_EQUAL -> {
				message = new SpannableStringBuilder(getString(R.string.reinstall));
				AppItem app = installer.getExistsApp();
				btnRun.setVisibility(View.VISIBLE);
				btnRun.setOnClickListener(v -> {
					installer.clearCache();
					installer.deleteTemp();
					Config.startApp(v.getContext(), app.getTitle(), app.getPathExt(), false);
					dismiss();
				});
			}
			case AppInstaller.STATUS_NEWEST -> message = new SpannableStringBuilder(getString(
					R.string.reinstall_newest,
					nd.getVersion(),
					installer.getCurrentVersion()));
			case AppInstaller.STATUS_UNMATCHED -> {
				SpannableStringBuilder info = installer.getManifest().getInfo(requireActivity());
				info.append(getString(R.string.install_jar_non_matched_jad));
				alertConfirm(info, v -> installApp(installer.getJar(), null));
				return;
			}
			case AppInstaller.STATUS_SAME -> {
				installer.clearCache();
				installer.deleteTemp();
				AppItem app = installer.getExistsApp();
				Config.startApp(getContext(), app.getTitle(), app.getPathExt(), false);
				dismiss();
				return;
			}
			default -> throw new IllegalStateException("Unexpected value: " + status);
		}
		if (installer.getJar() == null) {
			message.append('\n').append(getString(R.string.warn_install_from_net));
		}
		Drawable drawable = Drawable.createFromPath(installer.getIconPath());
		if (drawable != null) dialog.setIcon(drawable);
		dialog.setTitle(nd.getName());
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(message);
		btnOk.setOnClickListener(v -> convert());
		hideProgress();
		showButtons();
	}

	private void onError(Throwable e) {
		e.printStackTrace();
		installer.clearCache();
		installer.deleteTemp();
		if (!isAdded()) return;
		hideProgress();
		Toast.makeText(requireActivity(), getString(R.string.error) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
		dismissAllowingStateLoss();
	}
}
