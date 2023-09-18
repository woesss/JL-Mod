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
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.LogicHandler;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.util.StoragePermissionHelper;

public class FilteredFilePickerFragment extends AbstractFilePickerFragment<File> {
	private static final List<String> extList = Arrays.asList(".jad", ".jar", ".kjx");
	private static final File ROOT_FILE = new File("/");

	private final Stack<File> history = new Stack<>();
	private final StoragePermissionHelper storagePermissionHelper = new StoragePermissionHelper(this, this::onPermissionResult);

	private File mRequestedPath;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (history.empty()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				history.push(Environment.getStorageDirectory());
			} else {
				history.push(Environment.getExternalStorageDirectory());
			}
		}

		requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (history.empty()) {
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

	private static String getExtension(@NonNull File file) {
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

	@NonNull
	@Override
	public File getRoot() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			return Environment.getStorageDirectory();
		} else {
			return ROOT_FILE;
		}
	}

	@NonNull
	@Override
	public Loader<List<File>> getLoader() {
		return new FileAsyncTaskLoader(requireContext(), mCurrentPath, this::isItemVisible);
	}

	@Override
	public boolean isDir(@NonNull final File path) {
		return path.isDirectory();
	}

	@NonNull
	@Override
	public String getName(@NonNull File path) {
		return path.getName();
	}

	@NonNull
	@Override
	public File getParent(@NonNull final File from) {
		if (from.getPath().equals(getRoot().getPath())) {
			// Already at root, we can't go higher
			return from;
		} else if (from.getParentFile() != null) {
			return from.getParentFile();
		} else {
			return from;
		}
	}

	@NonNull
	@Override
	public File getPath(@NonNull final String path) {
		return new File(path);
	}

	@NonNull
	@Override
	public String getFullPath(@NonNull final File path) {
		return path.getPath();
	}

	@Override
	public void onNewFolder(@NonNull final String name) {
		File folder = new File(mCurrentPath, name);

		if (folder.mkdir()) {
			refresh(folder);
		} else {
			Toast.makeText(getActivity(),
					com.nononsenseapps.filepicker.R.string.nnf_create_folder_error,
					Toast.LENGTH_SHORT)
					.show();
		}
	}

	private static class FileAsyncTaskLoader extends AsyncTaskLoader<List<File>> {
		private final FileFilter fileFilter;
		private final File path;

		public FileAsyncTaskLoader(Context context, File path, FileFilter fileFilter) {
			super(context);
			this.path = path;
			this.fileFilter = fileFilter;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		@Override
		public List<File> loadInBackground() {
			List<File> fileList = new ArrayList<>();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && path.equals(Environment.getStorageDirectory())) {
				StorageManager sm = ContextCompat.getSystemService(getContext(), StorageManager.class);
				if (sm != null) {
					for (StorageVolume volume : sm.getStorageVolumes()) {
						File volumeDirectory = volume.getDirectory();
						if (volumeDirectory != null) {
							String description = volume.getDescription(getContext());
							fileList.add(new VolumeFile(volumeDirectory.getPath(), description));
						}
					}
					if (fileList.size() > 0) {
						return fileList;
					}
				}
			}
			File[] files = path.listFiles(fileFilter);
			if (files != null) {
				Arrays.sort(files, this::compareFiles);
				fileList.addAll(Arrays.asList(files));
			}
			return fileList;
		}

		private int compareFiles(@NonNull File lhs, @NonNull File rhs) {
			if (lhs.isDirectory()) {
				if (!rhs.isDirectory()) {
					return -1;
				}
			} else if (rhs.isDirectory()) {
				return 1;
			}
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	}
}