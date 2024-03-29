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

package ru.playsoftware.j2meloader.appsdb;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import ru.playsoftware.j2meloader.EmulatorApplication;
import ru.playsoftware.j2meloader.applist.AppItem;

@Database(entities = {AppItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

	public abstract AppItemDao appItemDao();

	static synchronized AppDatabase open(String path) {
		return Room.databaseBuilder(EmulatorApplication.getInstance(), AppDatabase.class, path).build();
	}
}
