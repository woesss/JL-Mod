/*
 * Copyright 2021-2024 Yury Kharchenko
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

import static ru.playsoftware.j2meloader.util.Constants.PREF_APP_SORT;
import static ru.playsoftware.j2meloader.util.Constants.PREF_EMULATOR_DIR;

import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import java.util.List;

import ru.playsoftware.j2meloader.EmulatorApplication;
import ru.playsoftware.j2meloader.appsdb.AppRepository;
import ru.playsoftware.j2meloader.config.Config;

public class AppListModel extends ViewModel implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final AppRepository appRepository = new AppRepository();

	public AppListModel() {
		PreferenceManager.getDefaultSharedPreferences(EmulatorApplication.getInstance())
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onCleared() {
		PreferenceManager.getDefaultSharedPreferences(EmulatorApplication.getInstance())
				.unregisterOnSharedPreferenceChangeListener(this);
		appRepository.close();
	}

	public void setEmulatorDirectory(String emulatorDir) {
		appRepository.setDatabaseFile(emulatorDir + Config.APPS_DB_NAME);
	}

	MutableLiveData<List<AppItem>> getAppList() {
		return appRepository.getAppList();
	}

	public MutableLiveData<Throwable> getErrors() {
		return appRepository.getErrors();
	}

	void updateApp(AppItem item) {
		appRepository.update(item);
	}

	void deleteApp(AppItem item) {
		appRepository.delete(item);
	}

	void setAppListFilter(String filter) {
		appRepository.setFilter(filter);
	}

	public AppItem getApp(int id) {
		return appRepository.get(id);
	}

	public void addApp(AppItem app) {
		appRepository.insert(app);
	}

	public AppItem getApp(String name, String vendor) {
		return appRepository.get(name, vendor);
	}

	public String getAppFilter() {
		return appRepository.getFilter();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		if (PREF_APP_SORT.equals(key)) {
			appRepository.setSort(sp.getInt(PREF_APP_SORT, 0));
		} else if (PREF_EMULATOR_DIR.equals(key)) {
			String path = sp.getString(key, null);
			if (path != null) {
				appRepository.setDatabaseFile(path + Config.APPS_DB_NAME);
			}
		}
	}
}
