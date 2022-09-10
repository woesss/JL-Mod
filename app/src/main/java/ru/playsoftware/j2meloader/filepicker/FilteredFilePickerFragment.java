/*
 * Copyright 2018 Nikita Shakarun
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nononsenseapps.filepicker.FilePickerFragment;
import com.nononsenseapps.filepicker.LogicHandler;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.util.StoragePermissionContract;

public class FilteredFilePickerFragment extends FilePickerFragment {
	private static final List<String> extList = Arrays.asList(".jad", ".jar", ".kjx");
	private static final Stack<File> history = new Stack<>();
	private File mRequestedPath;
	private final ActivityResultLauncher<Boolean> permissionsLauncher = registerForActivityResult(
			new StoragePermissionContract(),
			this::onPermissionResult
	);

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (history.empty()) {
			history.push(Environment.getExternalStorageDirectory());
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
		return StoragePermissionContract.isGranted(requireContext());
	}

	@Override
	protected void handlePermission(@NonNull File path) {
		this.mRequestedPath = path;
		try {
			permissionsLauncher.launch(true);
		} catch (ActivityNotFoundException e) {
			permissionsLauncher.launch(false);
		}
	}

	private void onPermissionResult(boolean isGranted) {
		if (isGranted) {
			// Do refresh
			if (this.mRequestedPath != null) {
				refresh(this.mRequestedPath);
			}
		} else {
			Toast.makeText(getContext(), com.nononsenseapps.filepicker.R.string.nnf_permission_external_write_denied,
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
}