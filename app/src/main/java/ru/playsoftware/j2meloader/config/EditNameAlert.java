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
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.Objects;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.databinding.DialogInputBinding;

public class EditNameAlert extends DialogFragment {
	private static final String TITLE = "title";
	private static final String NAME = "name";
	private static final String ID = "id";

	private Callback callback;
	private String title;
	private int id;
	private String name;
	private EditText editText;

	static EditNameAlert newInstance(String title, String name, int id) {
		EditNameAlert fragment = new EditNameAlert();
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		args.putString(NAME, name);
		args.putInt(ID, id);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof Callback) {
			callback = ((Callback) context);
		}
		final Bundle args = getArguments();
		if (args != null) {
			title = args.getString(TITLE);
			name = args.getString(NAME);
			id = args.getInt(ID);
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		TextInputLayout layout = DialogInputBinding.inflate(getLayoutInflater()).getRoot();
		editText = Objects.requireNonNull(layout.getEditText());
		editText.setFilters(new InputFilter[]{new FileNameInputFilter()});
		if (name != null) {
			editText.setText(name);
			editText.requestFocus();
			editText.setSelection(name.length());
		}
		setCancelable(false);
		return new AlertDialog.Builder(requireActivity())
				.setTitle(title)
				.setView(layout)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok, null)
				.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		AlertDialog dialog = (AlertDialog) requireDialog();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onClickOk());
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		editText = null;
	}

	void onClickOk() {
		EditText editText = this.editText;
		if (editText == null) {
			return;
		}
		String name = editText.getText().toString().trim();
		if (name.isEmpty()) {
			editText.setText(name);
			editText.requestFocus();
			Toast.makeText(requireActivity(), R.string.error_name, Toast.LENGTH_SHORT).show();
			return;
		}
		if (name.equals(this.name) || new File(Config.getProfilesDir(), name).exists()) {
			editText.requestFocus();
			editText.setSelection(name.length());
			final Toast toast = Toast.makeText(requireActivity(), R.string.not_saved_exists, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 50);
			toast.show();
			return;
		}
		if (callback != null) {
			callback.onNameChanged(id, name);
		}
		dismiss();
	}

	interface Callback {
		void onNameChanged(int id, String newName);
	}
}
