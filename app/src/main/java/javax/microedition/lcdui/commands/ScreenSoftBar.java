/*
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
package javax.microedition.lcdui.commands;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.Arrays;
import java.util.List;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Screen;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.databinding.SoftButtonBarBinding;

public class ScreenSoftBar extends AbstractSoftKeysBar {
	private final SoftButtonBarBinding binding;

	public ScreenSoftBar(Screen target, ViewGroup root) {
		super(target);
		binding = SoftButtonBarBinding.inflate(LayoutInflater.from(root.getContext()), root, true);
		binding.softLeft.setOnClickListener(this::onClick);
		binding.softMiddle.setOnClickListener(this::onClick);
		binding.softRight.setOnClickListener(this::onClick);
		notifyChanged();
	}

	private void onClick(View button) {
		Object tag = button.getTag();
		if (tag == null) {
			showMenu();
		} else {
			target.fireCommandAction((Command) tag);
		}
	}

	@Override
	protected void onCommandsChanged() {
		List<Command> list = this.commands;
		list.clear();
		Command[] commands = target.getCommands();
		int size = commands.length;
		if (size == 0) {
			binding.softLeft.setTag(null);
			binding.softMiddle.setTag(null);
			binding.softRight.setTag(null);
			binding.softLeft.setText("");
			binding.softMiddle.setText("");
			binding.softRight.setText("");
			binding.softBar.setVisibility(View.GONE);
			return;
		}
		Arrays.sort(commands);
		list.addAll(Arrays.asList(commands));
		switch (size) {
			case 1:
				Command c = list.get(0);
				binding.softLeft.setText(c.getAndroidLabel());
				binding.softLeft.setTag(c);

				binding.softMiddle.setVisibility(View.INVISIBLE);
				binding.softMiddle.setText("");
				binding.softMiddle.setTag(null);

				binding.softRight.setVisibility(View.INVISIBLE);
				binding.softRight.setText("");
				binding.softRight.setTag(null);
				break;
			case 2:
				c = list.get(0);
				binding.softLeft.setText(c.getAndroidLabel());
				binding.softLeft.setTag(c);

				binding.softMiddle.setVisibility(View.INVISIBLE);
				binding.softMiddle.setText("");
				binding.softMiddle.setTag(null);

				binding.softRight.setVisibility(View.VISIBLE);
				c = list.get(1);
				binding.softRight.setText(c.getAndroidLabel());
				binding.softRight.setTag(c);
				break;
			case 3:
				c = list.get(0);
				binding.softLeft.setText(c.getAndroidLabel());
				binding.softLeft.setTag(c);

				binding.softMiddle.setVisibility(View.VISIBLE);
				c = list.get(1);
				binding.softMiddle.setText(c.getAndroidLabel());
				binding.softMiddle.setTag(c);

				binding.softRight.setVisibility(View.VISIBLE);
				c = list.get(2);
				binding.softRight.setText(c.getAndroidLabel());
				binding.softRight.setTag(c);
				break;
			default:
				c = list.get(0);
				binding.softLeft.setText(c.getAndroidLabel());
				binding.softLeft.setTag(c);

				binding.softMiddle.setVisibility(View.VISIBLE);
				c = list.get(1);
				binding.softMiddle.setText(c.getAndroidLabel());
				binding.softMiddle.setTag(c);

				binding.softRight.setVisibility(View.VISIBLE);
				binding.softRight.setText(R.string.cmd_menu);
				binding.softRight.setTag(null);
		}
		binding.softBar.setVisibility(View.VISIBLE);
	}

	public void showMenu() {
		PopupWindow popup = prepareMenu(2);
		int y = binding.softRight.getHeight();
		View rootView = binding.softRight.getRootView();
		popup.setWidth(Math.min(rootView.getWidth(), rootView.getHeight()) / 2);
		popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		popup.showAtLocation(rootView, Gravity.RIGHT | Gravity.BOTTOM, 0, y);
	}
}
