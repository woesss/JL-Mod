/*
 * Copyright 2012 Kulikov Dmitriy
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

package javax.microedition.lcdui;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;

import javax.microedition.lcdui.event.CommandActionEvent;
import javax.microedition.lcdui.event.SimpleEvent;
import javax.microedition.util.ContextHolder;

public abstract class Item {
	public static final int PLAIN = 0;
	public static final int HYPERLINK = 1;
	public static final int BUTTON = 2;

	public static final int LAYOUT_DEFAULT = 0;
	public static final int LAYOUT_LEFT = 1;
	public static final int LAYOUT_RIGHT = 2;
	public static final int LAYOUT_CENTER = 3;
	public static final int LAYOUT_TOP = 16;
	public static final int LAYOUT_BOTTOM = 32;
	public static final int LAYOUT_VCENTER = 48;
	public static final int LAYOUT_NEWLINE_BEFORE = 256;
	public static final int LAYOUT_NEWLINE_AFTER = 512;
	public static final int LAYOUT_SHRINK = 1024;
	public static final int LAYOUT_EXPAND = 2048;
	public static final int LAYOUT_VSHRINK = 4096;
	public static final int LAYOUT_VEXPAND = 8192;
	public static final int LAYOUT_2 = 16384;

	private static final int HORIZONTAL_GRAVITY_MASK = 3;
	private static final int VERTICAL_GRAVITY_MASK = 48;

	final ArrayList<Command> commands = new ArrayList<>();
	ItemCommandListener listener = null;
	int preferredWidth = -1;
	int preferredHeight = -1;
	int width;
	int height;

	private int layoutMode = LAYOUT_DEFAULT;
	private String label;
	private Screen owner;
	private LinearLayout layout;
	private View contentView;
	private TextView labelView;
	private Command defaultCommand;

	private final SimpleEvent msgSetContextMenuListener = new SimpleEvent() {
		@Override
		public void process() {
			if (layout == null) {
				return;
			}
			if (listener != null) {
				labelView.setOnCreateContextMenuListener(Item.this::onCreateContextMenu);
				contentView.setOnCreateContextMenuListener(Item.this::onCreateContextMenu);
			} else {
				labelView.setLongClickable(false);
				contentView.setLongClickable(false);
			}
		}
	};

	private final SimpleEvent msgSetLabel = new SimpleEvent() {
		@Override
		public void process() {
			if (labelView == null) {
				return;
			}
			String text = label;
			labelView.setText(text);
			labelView.setVisibility(text == null ? View.GONE : View.VISIBLE);
		}
	};

	public void addCommand(Command cmd) {
		if (cmd == null) {
			throw new NullPointerException();
		} else if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		if (!commands.contains(cmd)) {
			commands.add(cmd);
		}
	}

	public String getLabel() {
		return label;
	}

	public int getLayout() {
		return layoutMode;
	}

	public int getMinimumHeight() {
		return 0;
	}

	public int getMinimumWidth() {
		return 0;
	}

	public int getPreferredHeight() {
		return height;
	}

	public int getPreferredWidth() {
		return width;
	}

	public void notifyStateChanged() {
		if (owner instanceof Form form) {
			form.notifyItemStateChanged(this);
		} else {
			throw new IllegalStateException("Item is not owned by a Form");
		}
	}

	public void removeCommand(Command cmd) {
		commands.remove(cmd);
		if (defaultCommand == cmd) {
			defaultCommand = null;
		}
	}

	public void setDefaultCommand(Command cmd) {
		if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		defaultCommand = cmd;
		if (cmd == null) {
			return;
		}
		commands.remove(cmd);
		commands.add(0, cmd);
	}

	public void setItemCommandListener(ItemCommandListener listener) {
		if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		this.listener = listener;

		if (layout != null) {
			ViewHandler.postEvent(msgSetContextMenuListener);
		}
	}

	public void setLabel(String value) {
		if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		label = value;
		if (layout != null) {
			ViewHandler.postEvent(msgSetLabel);
		}
	}

	public void setLayout(int value) {
		if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		layoutMode = value;
	}

	public void setPreferredSize(int width, int height) {
		if (width < -1 || height < -1) {
			throw new IllegalArgumentException();
		} else if (owner instanceof Alert) {
			throw new IllegalStateException("Item is owned by Alert");
		}
		preferredWidth = width;
		preferredHeight = height;
		// FIXME: 18.11.2023 width and height MUST be computed from content
		// now we are reproducing the previous logic
		this.width = width == -1 ? 0 : width;
		this.height = height == -1 ? 0 : height;
	}

	void setOwner(Screen owner) {
		this.owner = owner;
		ViewHandler.postEvent(new SimpleEvent() {
			@Override
			public void process() {
				clearItemView();
			}
		});
	}

	Screen getOwner() {
		if (owner == null) {
			throw new IllegalStateException("call setOwnerForm() before calling getOwnerForm()");
		}

		return owner;
	}

	boolean hasOwner() {
		return owner != null;
	}

	/**
	 * Get the whole item
	 *
	 * @return LinearLayout with a label in the first row and some content in the second row
	 */
	View getItemView() {
		if (layout == null) {
			Context context = ContextHolder.getActivity();

			layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);

			labelView = new AppCompatTextView(context);
			labelView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
			labelView.setText(label);

			layout.addView(labelView, getLayoutParams());
			if (label == null) {
				labelView.setVisibility(View.GONE);
			}

			contentView = getItemContentView();
			layout.addView(contentView, getLayoutParams());

			msgSetContextMenuListener.run();
		}

		return layout;
	}

	private LinearLayout.LayoutParams getLayoutParams() {
		int hwrap = LayoutParams.MATCH_PARENT;
		int vwrap = LayoutParams.WRAP_CONTENT;
		int gravity = Gravity.LEFT;

		if (this instanceof ImageItem) {
			hwrap = LayoutParams.WRAP_CONTENT;
		}

		if ((layoutMode & LAYOUT_SHRINK) != 0) {
			hwrap = LayoutParams.WRAP_CONTENT;
		} else if ((layoutMode & LAYOUT_EXPAND) != 0) {
			hwrap = LayoutParams.MATCH_PARENT;
		}

		if ((layoutMode & LAYOUT_VSHRINK) != 0) {
			vwrap = LayoutParams.WRAP_CONTENT;
		} else if ((layoutMode & LAYOUT_VEXPAND) != 0) {
			vwrap = LayoutParams.MATCH_PARENT;
		}

		int horizontal = layoutMode & HORIZONTAL_GRAVITY_MASK;
		if (horizontal == LAYOUT_CENTER) {
			gravity = Gravity.CENTER_HORIZONTAL;
		} else if (horizontal == LAYOUT_RIGHT) {
			gravity = Gravity.RIGHT;
			hwrap = LayoutParams.WRAP_CONTENT;
		} else if (horizontal == LAYOUT_LEFT) {
			gravity = Gravity.LEFT;
			hwrap = LayoutParams.WRAP_CONTENT;
		}

		int vertical = layoutMode & VERTICAL_GRAVITY_MASK;
		if (vertical == LAYOUT_VCENTER) {
			gravity |= Gravity.CENTER_VERTICAL;
		} else if (vertical == LAYOUT_BOTTOM) {
			gravity |= Gravity.BOTTOM;
			vwrap = LayoutParams.WRAP_CONTENT;
		} else if (vertical == LAYOUT_TOP) {
			gravity |= Gravity.TOP;
			vwrap = LayoutParams.WRAP_CONTENT;
		}

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(hwrap, vwrap);
		layoutParams.gravity = gravity;
		return layoutParams;
	}

	void clearItemView() {
		layout = null;
		labelView = null;
		contentView = null;

		clearItemContentView();
	}

	/**
	 * Get the item content
	 */
	protected abstract View getItemContentView();

	protected abstract void clearItemContentView();

	void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		menu.clear();

		for (Command cmd : commands) {
			menu.add(hashCode(), cmd.hashCode(), cmd.getPriority(), cmd.getAndroidLabel());
		}
	}

	boolean contextMenuItemSelected(MenuItem item) {
		if (listener == null) {
			return false;
		}

		int id = item.getItemId();

		for (Command cmd : commands) {
			if (cmd.hashCode() == id) {
				if (owner != null) {
					Display.postEvent(CommandActionEvent.getInstance(listener, cmd, this));
				}
				return true;
			}
		}
		return false;
	}

	void fireDefaultCommandAction() {
		if (defaultCommand != null) {
			Display.postEvent(CommandActionEvent.getInstance(listener, defaultCommand, this));
		}
	}
}