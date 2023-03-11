/*
 * Copyright 2015-2016 Nickolay Savchenko
 * Copyright 2017-2018 Nikita Shakarun
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

package ru.playsoftware.j2meloader.applist;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.Config;

class AppsListAdapter extends ListAdapter<AppItem, AppsListAdapter.AppViewHolder> implements Filterable {
	static final int LAYOUT_TYPE_LIST = 0;
	static final int LAYOUT_TYPE_GRID = 1;

	private final View.OnCreateContextMenuListener contextMenuListener;
	private List<AppItem> list = new ArrayList<>();
	private final AppFilter appFilter = new AppFilter();
	private CharSequence filterConstraint;
	private View emptyView;
	private int layout = LAYOUT_TYPE_LIST;

	AppsListAdapter(View.OnCreateContextMenuListener contextMenuListener) {
		super(new DiffUtil.ItemCallback<AppItem>() {
			@Override
			public boolean areItemsTheSame(@NonNull AppItem oldItem, @NonNull AppItem newItem) {
				return oldItem.getId() == newItem.getId();
			}

			@Override
			public boolean areContentsTheSame(@NonNull AppItem oldItem, @NonNull AppItem newItem) {
				return oldItem.getTitle().equals(newItem.getTitle()) &&
						oldItem.getVersion().equals(newItem.getVersion());
			}
		});
		this.contextMenuListener = contextMenuListener;
	}

	@Override
	public int getItemViewType(int position) {
		return layout;
	}

	@NonNull
	@Override
	public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view;
		AppViewHolder holder;
		if (viewType == 1) {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_grid_jar, parent, false);
			holder = new AppViewHolder(view);
		} else {
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_jar, parent, false);
			holder = new AppListViewHolder(view);
		}
		view.setOnClickListener(v -> {
			AppItem item = getCurrentList().get(holder.getLayoutPosition());
			Config.startApp(v.getContext(), item.getTitle(), item.getPathExt(), false);
		});
		view.setOnCreateContextMenuListener(contextMenuListener);
		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
		holder.onBind(getItem(position));
	}

	void setItems(List<AppItem> items) {
		list = items;
		appFilter.filter(filterConstraint);
	}

	@Override
	public Filter getFilter() {
		return appFilter;
	}

	void setEmptyView(View emptyView) {
		this.emptyView = emptyView;
	}

	void setLayout(int layout) {
		this.layout = layout;
	}

	static class AppViewHolder extends RecyclerView.ViewHolder {
		final ImageView icon;
		final TextView name;

		AppViewHolder(View itemView) {
			super(itemView);
			icon = itemView.findViewById(R.id.list_image);
			name = itemView.findViewById(R.id.list_title);
		}

		void onBind(AppItem item) {
			Drawable icon = Drawable.createFromPath(item.getImagePathExt());
			if (icon != null) {
				icon.setFilterBitmap(false);
				this.icon.setImageDrawable(icon);
			} else {
				this.icon.setImageResource(R.mipmap.ic_launcher);
			}
			name.setText(item.getTitle());
			itemView.setTag(item);
		}
	}

	static class AppListViewHolder extends AppViewHolder {
		final TextView author;
		final TextView version;

		AppListViewHolder(View view) {
			super(view);
			author = view.findViewById(R.id.list_author);
			version = view.findViewById(R.id.list_version);
		}

		@Override
		void onBind(AppItem item) {
			super.onBind(item);
			author.setText(item.getAuthor());
			version.setText(item.getVersion());
		}
	}

	class AppFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if (isEmpty(constraint)) {
				results.count = list.size();
				results.values = list;
			} else {
				ArrayList<AppItem> resultList = new ArrayList<>();
				for (AppItem item : list) {
					if (item.getTitle().toLowerCase().contains(constraint) ||
							item.getAuthor().toLowerCase().contains(constraint)) {
						resultList.add(item);
					}
				}
				results.count = resultList.size();
				results.values = resultList;
			}
			return results;
		}

		boolean isEmpty(CharSequence constraint) {
			return constraint == null || constraint.toString().trim().length() == 0;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filterConstraint = constraint;
			//noinspection unchecked
			submitList((List<AppItem>) results.values);
			if (results.count > 0) {
				emptyView.setVisibility(View.GONE);
			} else {
				if (list.isEmpty()) {
					((TextView) emptyView).setText(R.string.no_data_for_display);
				} else {
					String msg = emptyView.getResources().getString(R.string.msg_no_matches, constraint);
					((TextView) emptyView).setText(msg);
				}
				emptyView.setVisibility(View.VISIBLE);
			}
		}
	}
}
