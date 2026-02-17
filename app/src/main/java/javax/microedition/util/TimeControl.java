/*
 * Copyright 2024 Yury Kharchenko
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

package javax.microedition.util;

public class TimeControl {
	private static volatile float speed = 1.0f;

	public static void setSpeed(float speed) {
		if (speed <= 0) {
			throw new IllegalArgumentException("Speed must be positive");
		}
		TimeControl.speed = speed;
	}

	public static float getSpeed() {
		return speed;
	}

	public static void sleep(long millis) throws InterruptedException {
		float s = speed;
		if (s == 1.0f) {
			Thread.sleep(millis);
			return;
		}
		long newMillis = (long) (millis / s);
		if (newMillis < 0) newMillis = 0;
		Thread.sleep(newMillis);
	}
}
