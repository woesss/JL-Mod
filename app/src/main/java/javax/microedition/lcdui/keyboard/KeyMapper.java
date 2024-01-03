/*
 * Copyright 2018 Nikita Shakarun
 * Copyright 2021-2023 Yury Kharchenko
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

package javax.microedition.lcdui.keyboard;

import static javax.microedition.lcdui.Canvas.*;

import android.util.SparseIntArray;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.collection.SparseArrayCompat;

import java.util.List;

import ru.playsoftware.j2meloader.config.ProfileModel;

public class KeyMapper {
	public static final int KEY_OPTIONS_MENU = 0;
	public static final int SE_KEY_SPECIAL_GAMING_A = -13;
	public static final int SE_KEY_SPECIAL_GAMING_B = -14;

	private static final int DEFAULT_LAYOUT = 0;
	private static final int SIEMENS_LAYOUT = 1;
	private static final int MOTOROLA_LAYOUT = 2;
	private static final int CUSTOM_LAYOUT = 3;

	private static final int SIEMENS_KEY_UP = -59;
	private static final int SIEMENS_KEY_DOWN = -60;
	private static final int SIEMENS_KEY_LEFT = -61;
	private static final int SIEMENS_KEY_RIGHT = -62;
	private static final int SIEMENS_KEY_SOFT_LEFT = -1;
	private static final int SIEMENS_KEY_SOFT_RIGHT = -4;
	private static final int MOTOROLA_KEY_UP = -1;
	private static final int MOTOROLA_KEY_DOWN = -6;
	private static final int MOTOROLA_KEY_LEFT = -2;
	private static final int MOTOROLA_KEY_RIGHT = -5;
	private static final int MOTOROLA_KEY_FIRE = -20;
	private static final int MOTOROLA_KEY_SOFT_LEFT = -21;
	private static final int MOTOROLA_KEY_SOFT_RIGHT = -22;

	private static final SparseArrayCompat<String> keyCodeToKeyName = new SparseArrayCompat<>();
	private static final SparseIntArray keyCodeToCustom = new SparseIntArray();
	private static final SparseIntArray keyCodeToGameAction = new SparseIntArray();
	private static final SparseIntArray gameActionToKeyCode = new SparseIntArray();
	private static SparseIntArray androidToMIDP;
	private static int layoutType;

	static {
		mapGameAction(KEY_NUM2, UP);
		mapGameAction(KEY_NUM4, LEFT);
		mapGameAction(KEY_NUM5, FIRE);
		mapGameAction(KEY_NUM6, RIGHT);
		mapGameAction(KEY_NUM7, GAME_A);
		mapGameAction(KEY_NUM8, DOWN);
		mapGameAction(KEY_NUM9, GAME_B);
		mapGameAction(KEY_STAR, GAME_C);
		mapGameAction(KEY_POUND, GAME_D);
		mapKey(KEY_UP, UP, "UP");
		mapKey(KEY_DOWN, DOWN, "DOWN");
		mapKey(KEY_LEFT, LEFT, "LEFT");
		mapKey(KEY_RIGHT, RIGHT, "RIGHT");
		mapKey(KEY_FIRE, FIRE, "SELECT");
		mapKeyName(KEY_SOFT_LEFT, "SOFT1");
		mapKeyName(KEY_SOFT_RIGHT, "SOFT2");
		mapKeyName(KEY_CLEAR, "CLEAR");
		mapKeyName(KEY_SEND, "SEND");
		mapKeyName(KEY_END, "END");
	}

	private static void remapKeys(ProfileModel params) {
		if (layoutType == SIEMENS_LAYOUT) {
			keyCodeToCustom.put(KEY_LEFT, SIEMENS_KEY_LEFT);
			keyCodeToCustom.put(KEY_RIGHT, SIEMENS_KEY_RIGHT);
			keyCodeToCustom.put(KEY_UP, SIEMENS_KEY_UP);
			keyCodeToCustom.put(KEY_DOWN, SIEMENS_KEY_DOWN);
			keyCodeToCustom.put(KEY_SOFT_LEFT, SIEMENS_KEY_SOFT_LEFT);
			keyCodeToCustom.put(KEY_SOFT_RIGHT, SIEMENS_KEY_SOFT_RIGHT);

			mapKey(SIEMENS_KEY_UP, UP, "UP");
			mapKey(SIEMENS_KEY_DOWN, DOWN, "DOWN");
			mapKey(SIEMENS_KEY_LEFT, LEFT, "LEFT");
			mapKey(SIEMENS_KEY_RIGHT, RIGHT, "RIGHT");
			mapKeyName(SIEMENS_KEY_SOFT_LEFT, "SOFT1");
			mapKeyName(SIEMENS_KEY_SOFT_RIGHT, "SOFT2");
		} else if (layoutType == MOTOROLA_LAYOUT) {
			keyCodeToCustom.put(KEY_UP, MOTOROLA_KEY_UP);
			keyCodeToCustom.put(KEY_DOWN, MOTOROLA_KEY_DOWN);
			keyCodeToCustom.put(KEY_LEFT, MOTOROLA_KEY_LEFT);
			keyCodeToCustom.put(KEY_RIGHT, MOTOROLA_KEY_RIGHT);
			keyCodeToCustom.put(KEY_FIRE, MOTOROLA_KEY_FIRE);
			keyCodeToCustom.put(KEY_SOFT_LEFT, MOTOROLA_KEY_SOFT_LEFT);
			keyCodeToCustom.put(KEY_SOFT_RIGHT, MOTOROLA_KEY_SOFT_RIGHT);

			mapKey(MOTOROLA_KEY_UP, UP, "UP");
			mapKey(MOTOROLA_KEY_DOWN, DOWN, "DOWN");
			mapKey(MOTOROLA_KEY_LEFT, LEFT, "LEFT");
			mapKey(MOTOROLA_KEY_RIGHT, RIGHT, "RIGHT");
			mapKey(MOTOROLA_KEY_FIRE, FIRE, "SELECT");
			mapKeyName(MOTOROLA_KEY_SOFT_LEFT, "SOFT1");
			mapKeyName(MOTOROLA_KEY_SOFT_RIGHT, "SOFT2");
		} else if (layoutType == CUSTOM_LAYOUT) {
			List<KeyModel> list = params.customKeys;
			if (list != null) {
				for (KeyModel keyModel : list) {
					if (keyModel.defaultKeyCode == 0) {
						continue;
					}
					if (keyModel.customKeyCode != 0) {
						keyCodeToCustom.put(keyModel.defaultKeyCode, keyModel.customKeyCode);
						mapKey(keyModel.customKeyCode, keyModel.gameAction, keyModel.keyName);
					} else {
						mapKey(keyModel.defaultKeyCode, keyModel.gameAction, keyModel.keyName);
					}
				}
			}
		}
	}

	private static void mapGameAction(int keyCode, int gameAction) {
		keyCodeToGameAction.put(keyCode, gameAction);
		gameActionToKeyCode.put(gameAction, keyCode);
	}

	private static void mapKeyName(int keyCode, String keyName) {
		keyCodeToKeyName.put(keyCode, keyName);
	}

	private static void mapKey(int keyCode, int gameAction, String keyName) {
		if (keyName != null) {
			keyCodeToKeyName.put(keyCode, keyName);
		}
		if (gameAction != 0) {
			keyCodeToGameAction.put(keyCode, gameAction);
			gameActionToKeyCode.put(gameAction, keyCode);
		}
	}

	public static int convertAndroidKeyCode(int keyCode, KeyEvent event) {
		if (!event.isShiftPressed()) {
			int map = androidToMIDP.get(keyCode, 0);
			if (map != 0) {
				return map;
			}
		}
		// TODO: 27.06.2021 ignored ascent char combination
		return event.getUnicodeChar() & KeyCharacterMap.COMBINING_ACCENT_MASK;
	}

	public static int convertKeyCode(int keyCode) {
		if (layoutType == DEFAULT_LAYOUT) {
			return keyCode;
		}
		return keyCodeToCustom.get(keyCode, keyCode);
	}

	public static void setKeyMapping(ProfileModel params) {
		layoutType = params.keyCodesLayout;
		SparseIntArray map = getDefaultKeyMap();
		SparseIntArray customKeyMap = params.keyMappings;
		if (customKeyMap != null) {
			for (int i = 0, size = customKeyMap.size(); i < size; i++) {
				map.put(customKeyMap.keyAt(i), customKeyMap.valueAt(i));
			}
		}
		androidToMIDP = map;
		remapKeys(params);
	}

	public static int getKeyCode(int gameAction) {
		return gameActionToKeyCode.get(gameAction, Integer.MAX_VALUE);
	}

	public static int getGameAction(int keyCode) {
		return keyCodeToGameAction.get(keyCode, 0);
	}

	public static String getKeyName(int keyCode) {
		String name = keyCodeToKeyName.get(keyCode);
		if (name == null && Character.isValidCodePoint(keyCode)) {
			name = new String(Character.toChars(keyCode));
		}
		return name;
	}

	public static SparseIntArray getDefaultKeyMap() {
		SparseIntArray map = new SparseIntArray();
		map.append(KeyEvent.KEYCODE_SOFT_LEFT, KEY_SOFT_LEFT);
		map.append(KeyEvent.KEYCODE_SOFT_RIGHT, KEY_SOFT_RIGHT);
		map.append(KeyEvent.KEYCODE_BACK, KEY_OPTIONS_MENU);
		map.append(KeyEvent.KEYCODE_CALL, KEY_SEND);
		map.append(KeyEvent.KEYCODE_ENDCALL, KEY_END);
		map.append(KeyEvent.KEYCODE_0, KEY_NUM0);
		map.append(KeyEvent.KEYCODE_1, KEY_NUM1);
		map.append(KeyEvent.KEYCODE_2, KEY_NUM2);
		map.append(KeyEvent.KEYCODE_3, KEY_NUM3);
		map.append(KeyEvent.KEYCODE_4, KEY_NUM4);
		map.append(KeyEvent.KEYCODE_5, KEY_NUM5);
		map.append(KeyEvent.KEYCODE_6, KEY_NUM6);
		map.append(KeyEvent.KEYCODE_7, KEY_NUM7);
		map.append(KeyEvent.KEYCODE_8, KEY_NUM8);
		map.append(KeyEvent.KEYCODE_9, KEY_NUM9);
		map.append(KeyEvent.KEYCODE_STAR, KEY_STAR);
		map.append(KeyEvent.KEYCODE_POUND, KEY_POUND);
		map.append(KeyEvent.KEYCODE_DPAD_UP, KEY_UP);
		map.append(KeyEvent.KEYCODE_DPAD_DOWN, KEY_DOWN);
		map.append(KeyEvent.KEYCODE_DPAD_LEFT, KEY_LEFT);
		map.append(KeyEvent.KEYCODE_DPAD_RIGHT, KEY_RIGHT);
		map.append(KeyEvent.KEYCODE_ENTER, KEY_FIRE);
		map.append(KeyEvent.KEYCODE_DEL, KEY_CLEAR);
		return map;
	}
}
