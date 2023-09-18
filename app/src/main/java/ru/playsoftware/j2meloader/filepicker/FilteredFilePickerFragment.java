/*
 * Copyright 2018-2020 Nikita Shakarun
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

package ru.playsoftware.j2meloader.filepicker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nononsenseapps.filepicker.FilePickerFragment;
import com.nononsenseapps.filepicker.LogicHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.util.StoragePermissionHelper;

public class FilteredFilePickerFragment extends FilePickerFragment {
	private static final List<String> extList = Arrays.asList(".jad", ".jar", ".kjx");
	private static final Stack<File> history = new Stack<>();
	private final List<File> roots = new ArrayList<>();
	private File mRequestedPath;
	private final StoragePermissionHelper storagePermissionHelper = new StoragePermissionHelper(this, this::onPermissionResult);

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			StorageManager sm = ContextCompat.getSystemService(requireContext(), StorageManager.class);
			if (sm != null) {
				for (StorageVolume volume : sm.getStorageVolumes()) {
					roots.add(volume.getDirectory());
				}
			}
		}
		if (history.empty()) {
			history.push(roots.isEmpty() ? Environment.getExternalStorageDirectory() : getRoot());
		}

		requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (isBackTop()) {
					setEnabled(false);
					requireActivity().onBackPressed();
				} else {
					goBack();
				}
			}
		});
	}

	@NonNull
	@Override
	public File getParent(@NonNull File from) {
		if (roots.contains(from)) {
			return getRoot();
		}
		return super.getParent(from);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater li = getLayoutInflater();
		switch (viewType) {
			case LogicHandler.VIEWTYPE_HEADER:
				return new HeaderViewHolder(li.inflate(R.layout.listitem_dir, parent, false));
			case LogicHandler.VIEWTYPE_CHECKABLE:
				return new CheckableViewHolder(li.inflate(R.layout.listitem_checkable, parent, false));
			case LogicHandler.VIEWTYPE_DIR:
			default:
				return new DirViewHolder(li.inflate(R.layout.listitem_dir, parent, false));
		}
	}

	@Override
	protected boolean hasPermission(@NonNull File path) {
		return StoragePermissionHelper.isGranted(requireContext());
	}

	@Override
	protected void handlePermission(@NonNull File path) {
		this.mRequestedPath = path;
		storagePermissionHelper.launch(requireContext());
	}

	private void onPermissionResult(boolean granted) {
		if (granted) {
			// Do refresh
			if (this.mRequestedPath != null) {
				refresh(this.mRequestedPath);
			}
		} else {
			Toast.makeText(getContext(),
					com.nononsenseapps.filepicker.R.string.nnf_permission_external_write_denied,
					Toast.LENGTH_SHORT).show();
			// Treat this as a cancel press
			if (mListener != null) {
				mListener.onCancelled();
			}
		}
	}

	private String getExtension(@NonNull File file) {
		String name = file.getName();
		int i = name.lastIndexOf('.');
		if (i < 0) {
			return null;
		} else {
			return name.substring(i).toLowerCase();
		}
	}

	@Override
	protected boolean isItemVisible(final File file) {
		if (isDir(file)) {
			return true;
		}
		if (mode != MODE_FILE && mode != MODE_FILE_AND_DIR) {
			return false;
		}
		String ext = getExtension(file);
		return ext != null && extList.contains(ext);
	}

	@Override
	public void goToDir(@NonNull File file) {
		history.add(mCurrentPath);
		super.goToDir(file);
	}

	@Override
	protected void refresh(@NonNull File nextPath) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && compareFiles(nextPath, getRoot()) == 0) {
			StorageManager sm = ContextCompat.getSystemService(requireContext(), StorageManager.class);
			if (sm != null) {
				if (hasPermission(nextPath)) {
					mCurrentPath = nextPath;
					isLoading = true;
					List<File> files = new ArrayList<>();
					for (StorageVolume volume : sm.getStorageVolumes()) {
						files.add(volume.getDirectory());
					}
					mFiles = files;
					isLoading = false;
					mCheckedItems.clear();
					mCheckedVisibleViewHolders.clear();
					mAdapter.replaceAll(files);
					if (mCurrentDirView != null) {
						mCurrentDirView.setText(getFullPath(mCurrentPath));
					}
					// Stop loading now to avoid a refresh clearing the user's selections
					LoaderManager.getInstance(this).destroyLoader(0);
				} else {
					handlePermission(nextPath);
				}
			}
			return;
		}
		super.refresh(nextPath);
	}

	@NonNull
	@Override
	public String getName(@NonNull File path) {
		if (roots.contains(path) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			StorageManager sm = ContextCompat.getSystemService(requireContext(), StorageManager.class);
			if (sm != null) {
				StorageVolume volume = sm.getStorageVolume(path);
				if (volume != null) {
					return volume.getDescription(requireContext());
				}
			}
		}
		return super.getName(path);
	}

	private boolean isBackTop() {
		return history.empty();
	}

	private void goBack() {
		File last = history.pop();
		super.goToDir(last);
	}

	@Override
	public void onBindHeaderViewHolder(@NonNull HeaderViewHolder viewHolder) {
		viewHolder.itemView.setEnabled(!getRoot().equals(mCurrentPath));
		super.onBindHeaderViewHolder(viewHolder);
	}

	@NonNull
	@Override
	public Uri toUri(@NonNull File file) {
		return Uri.fromFile(file);
	}
}