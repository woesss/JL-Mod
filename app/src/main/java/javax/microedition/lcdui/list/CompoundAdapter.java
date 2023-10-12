/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2018 Nikita Shakarun
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

package javax.microedition.lcdui.list;

import android.database.DataSetObserver;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import java.util.ArrayList;

public abstract class CompoundAdapter implements Adapter {
	final ArrayList<DataSetObserver> observers = new ArrayList<>();
	private final ArrayList<CompoundItem> items;

	public CompoundAdapter(ArrayList<CompoundItem> items) {
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public CompoundItem getItem(int position) {
		return items.get(position);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	View getView(int position, View convertView, ViewGroup parent, int viewResourceID) {
		TextView textview;

		if (convertView instanceof TextView) {
			textview = (TextView) convertView;
		} else {
			textview = (TextView) LayoutInflater.from(parent.getContext()).inflate(viewResourceID, parent, false);
		}

		CompoundItem item = items.get(position);

		if (item.getImage() != null) {
			Paint.FontMetrics fm = textview.getPaint().getFontMetrics();
			float lineHeight = fm.leading + fm.bottom - fm.top;
			Drawable drawable = item.getDrawable(lineHeight);
			SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
			ImageSpan imageSpan = new ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BOTTOM);
			ssb.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.append(item.getString());
			textview.setText(ssb);
		} else {
			textview.setText(item.getString());
		}

		return textview;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		observers.remove(observer);
	}

	public void notifyDataSetChanged() {
		CompoundAdapterKt.notifyChangedSync(observers);
	}
}
