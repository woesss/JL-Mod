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

package javax.microedition.media.control;

import javax.microedition.media.Control;

public interface ToneControl extends Control {
	byte VERSION = -2;

	byte TEMPO = -3;

	byte RESOLUTION = -4;

	byte BLOCK_START = -5;

	byte BLOCK_END = -6;

	byte PLAY_BLOCK = -7;

	byte SET_VOLUME = -8;

	byte REPEAT = -9;

	byte C4 = 60;

	byte SILENCE = -1;

	void setSequence(byte[] sequence);
}
