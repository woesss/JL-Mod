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

package org.microemu.android.asm;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BinaryPatcher {
	/** Copy bytes from source */
	public static final int SRC = 0b0000_0000;
	/** Copy bytes from patch */
	public static final int EXT = 0b0110_0000;
	/** Seek source position forward */
	public static final int FWD = 0b0010_0000;
	/** Seek source position backward */
	public static final int BWD = 0b0100_0000;
	/** Operator bits mask */
	private static final int MASK = EXT;

	private final ByteBuffer src;
	private final ByteBuffer diff;
	private final ByteArrayOutputStream out;
	private int prevLength;

	public BinaryPatcher(byte[] srcData, byte[] patch, int outSize) {
		src = ByteBuffer.wrap(srcData);
		diff = ByteBuffer.wrap(patch);
		out = new ByteArrayOutputStream(outSize);
	}

	public static byte[] patch(byte[] srcData, byte[] patch, int newSize) {
		return new BinaryPatcher(srcData, patch, newSize).patch();
	}

	private byte[] patch() {
		while (diff.hasRemaining()) {
			int command = diff.get();
			int length = readLength(command);
			switch (command & MASK) {
				case SRC:
					out.write(src.array(), src.position(), length);
					src.position(src.position() + length);
					break;
				case EXT:
					out.write(diff.array(), diff.position(), length);
					diff.position(diff.position() + length);
					break;
				case FWD:
					src.position(src.position() + length);
					break;
				case BWD: {
					src.position(src.position() - length);
					break;
				}
			}
		}
		return out.toByteArray();
	}

	private int readLength(int command) {
		int l = command & 0x1f;
		int shift = 5;
		int b = command;
		while ((b & 0x80) == 0) {
			b = diff.get();
			l |= (b & 0x7f) << shift;
			shift += 7;
		}
		if (l == 0) {
			return prevLength;
		}
		prevLength = l;
		return l;
	}
}
