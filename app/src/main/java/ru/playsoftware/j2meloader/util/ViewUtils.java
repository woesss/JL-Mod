/*
 *  Copyright 2023 Yury Kharchenko
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

package ru.playsoftware.j2meloader.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

public class ViewUtils {

	public static int measureListViewWidth(ListAdapter adapter, ViewGroup parent,
												 Context context, int maxAllowedWidth) {
		// Menus don't tend to be long, so this is more reasonable than it looks.
		int maxWidth = 0;
		View itemView = null;
		int itemType = 0;

		final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}

			if (parent == null) {
				parent = new FrameLayout(context);
			}

			itemView = adapter.getView(i, itemView, parent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);

			final int itemWidth = itemView.getMeasuredWidth();
			if (itemWidth >= maxAllowedWidth) {
				return maxAllowedWidth;
			} else if (itemWidth > maxWidth) {
				maxWidth = itemWidth;
			}
		}

		return maxWidth;
	}
}
