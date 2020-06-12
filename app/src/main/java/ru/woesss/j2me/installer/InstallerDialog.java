/*
 *  Copyright 2020 Yury Kharchenko
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

package ru.woesss.j2me.installer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.applist.AppItem;
import ru.playsoftware.j2meloader.appsdb.AppRepository;
import ru.playsoftware.j2meloader.config.Config;
import ru.woesss.j2me.jar.Descriptor;

public class InstallerDialog extends DialogFragment {
	private static final String ARG_PATH = "param1";
	private static final String ARG_URI = "param2";

	private TextView tvMessage;
	private TextView tvStatus;
	private ProgressBar progress;
	private AppRepository appRepository;
	private Button btnOk;
	private Button btnClose;
	private AppInstaller installer;
	private AlertDialog mDialog;

	public InstallerDialog() {
	}

	/**
	 * @param path source path of app.
	 * @param uri  original uri from intent.
	 * @return A new instance of fragment InstallerDialog.
	 */
	public static InstallerDialog newInstance(String path, Uri uri) {
		InstallerDialog fragment = new InstallerDialog();
		Bundle args = new Bundle();
		args.putString(ARG_PATH, path);
		args.putParcelable(ARG_URI, uri);
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		appRepository = new AppRepository(requireActivity().getApplication(), true);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		LayoutInflater inflater = requireActivity().getLayoutInflater();
		@SuppressLint("InflateParams")
		View view = inflater.inflate(R.layout.fragment_installer, null);
		tvMessage = view.findViewById(R.id.tvMidletInfo);
		tvStatus = view.findViewById(R.id.tvStatus);
		progress = view.findViewById(R.id.progress);
		btnOk = view.findViewById(R.id.btnOk);
		btnClose = view.findViewById(R.id.btnClose);
		mDialog = new AlertDialog.Builder(requireActivity(), getTheme())
				.setIcon(R.mipmap.ic_launcher)
				.setView(view)
				.setTitle("MIDlet installer")
				.create();
		return mDialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		Bundle args = requireArguments();
		String path = args.getString(ARG_PATH);
		Uri uri = args.getParcelable(ARG_URI);
		installApp(path, uri);
	}

	@Override
	public void onDestroy() {
		appRepository.close();
		super.onDestroy();
	}

	private void installApp(String path, Uri uri) {
		final FragmentActivity activity = requireActivity();
		installer = new AppInstaller(path, uri, activity.getApplication());
		btnClose.setOnClickListener(v -> {
			installer.deleteTemp();
			installer.clearCache();
			dismiss();
		});
		Single.create(installer::loadInfo)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new MidletInfoObserver());
	}

	private void hideProgress() {
		progress.setVisibility(View.INVISIBLE);
		tvStatus.setVisibility(View.INVISIBLE);
	}

	private void showProgress() {
		progress.setVisibility(View.VISIBLE);
		tvStatus.setVisibility(View.VISIBLE);
	}

	private void hideButtons() {
		btnOk.setVisibility(View.GONE);
		btnClose.setVisibility(View.GONE);
	}

	private void showButtons() {
		btnOk.setVisibility(View.VISIBLE);
		btnClose.setVisibility(View.VISIBLE);
	}

	private void convert(AppInstaller installer) {
		Descriptor nd = installer.getNewDescriptor();
		SpannableStringBuilder info = nd.getInfo(requireActivity());
		tvMessage.setText(info);
		tvStatus.setText(R.string.converting_wait);
		showProgress();
		hideButtons();
		Single.create(installer::install)
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new CompleteObserver(installer));

	}

	private void alertConfirm(SpannableStringBuilder message,
							  View.OnClickListener positive) {
		hideProgress();
		mDialog.setCancelable(false);
		mDialog.setCanceledOnTouchOutside(false);
		tvMessage.setText(message);
		btnOk.setOnClickListener(positive);
		showButtons();
	}

	private class MidletInfoObserver implements SingleObserver<Integer> {

		@Override
		public void onSubscribe(Disposable d) {
		}

		@Override
		public void onSuccess(Integer status) {
			Descriptor nd = installer.getNewDescriptor();
			mDialog.setTitle(nd.getName());
			if (status == AppInstaller.STATUS_NEW) {
				convert(installer);
				return;
			}
			String message;
			switch (status) {
				case AppInstaller.STATUS_OLDEST:
					String currVersion = installer.getOldDescriptor().getVersion();
					message = getString(R.string.reinstall_oldest, nd.getVersion(), currVersion);
					break;
				case AppInstaller.STATUS_EQUAL:
					message = getString(R.string.reinstall);
					break;
				case AppInstaller.STATUS_NEWEST:
					currVersion = installer.getOldDescriptor().getVersion();
					message = getString(R.string.reinstall_newest, nd.getVersion(), currVersion);
					break;
				case AppInstaller.STATUS_UNMATCHED:
					SpannableStringBuilder info = nd.getInfo(getActivity());
					info.append(getString(R.string.install_jar_non_matched_jad));
					alertConfirm(info, v -> installApp(installer.getJar(), null));
					return;
				default:
					throw new IllegalStateException("Unexpected value: " + status);
			}
			Drawable drawable = Drawable.createFromPath(installer.getIconPath());
			if (drawable != null) mDialog.setIcon(drawable);
			mDialog.setTitle(nd.getName());
			mDialog.setCancelable(false);
			mDialog.setCanceledOnTouchOutside(false);
			tvMessage.setText(message);
			btnOk.setOnClickListener(v -> convert(installer));
			hideProgress();
			showButtons();
		}

		@Override
		public void onError(Throwable e) {
			e.printStackTrace();
			hideProgress();
			if (!isAdded()) return;
			Toast.makeText(getActivity(), getString(R.string.error) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
			dismiss();
		}
	}

	private class CompleteObserver implements SingleObserver<AppItem> {
		private final AppInstaller installer;

		CompleteObserver(AppInstaller installer) {
			this.installer = installer;
		}

		@Override
		public void onSubscribe(Disposable d) {
		}

		@Override
		public void onSuccess(AppItem app) {
			appRepository.insert(app);
			installer.clearCache();
			installer.deleteTemp();
			hideProgress();
			if (!isAdded()) return;
			tvMessage.append(getString(R.string.install_done));
			Drawable drawable = Drawable.createFromPath(app.getImagePathExt());
			if (drawable != null) mDialog.setIcon(drawable);
			btnOk.setText(R.string.START_CMD);
			btnOk.setOnClickListener(v -> {
				Config.startApp(getActivity(), app.getTitle(), app.getPath(), false);
				dismiss();
			});
			btnClose.setText(R.string.close);
			showButtons();
		}

		@Override
		public void onError(Throwable e) {
			e.printStackTrace();
			hideProgress();
			if (!isAdded()) return;
			String message = e.getMessage();
			if (message == null) {
				return;
			}
			if (message.charAt(0) == '*') {
				Descriptor nd = installer.getManifest();
				SpannableStringBuilder info = nd.getInfo(getActivity());
				info.append(getString(R.string.install_jar_non_matched_jad));
				alertConfirm(info, v -> installApp(installer.getJar(), null));
			} else {
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			}
		}
	}
}
