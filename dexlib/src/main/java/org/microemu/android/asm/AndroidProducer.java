/**
 * MicroEmulator
 * Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
 * Copyright (C) 2017-2018 Nikita Shakarun
 * Copyright (C) 2021-2024 Yury Kharchenko
 * <p>
 * It is licensed under the following two licenses as alternatives:
 * 1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 * 2. Apache License (the "AL") Version 2.0
 * <p>
 * You may not use this file except in compliance with at least one of
 * the above two licenses.
 * <p>
 * You may obtain a copy of the LGPL at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 * <p>
 * You may obtain a copy of the AL at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LGPL or the AL for the specific language governing permissions and
 * limitations.
 *
 * @version $Id$
 */

package org.microemu.android.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.DataInputStream;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;

public class AndroidProducer {
	private static final Map<Integer, Integer> patches = initPatchFixes();

	public static byte[] instrument(byte[] classData, String classFileName, long crc)
			throws IllegalArgumentException {
		Integer patch = patches.get((int) crc);
		if (patch != null) {
			classData = patchClass(classData, patch);
		}
		ClassReader cr = new ClassReader(classData);
		if (!cr.getClassName().equals(classFileName.substring(0, classFileName.length() - 6))) {
			throw new IllegalArgumentException("Class name does not match path");
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassVisitor cv = new AndroidClassVisitor(cw);
		cr.accept(cv, ClassReader.SKIP_DEBUG);

		return cw.toByteArray();
	}

	private static byte[] patchClass(byte[] classData, int patch) {
		try (DataInputStream dis = new DataInputStream(AndroidProducer.class.getResourceAsStream("/assets/dexer/patches.bin"))) {
			dis.skipBytes(patch);
			int len = dis.readUnsignedShort();
			int newSize = dis.readShort() + classData.length;
			byte[] patchData = new byte[len - 2];
			dis.readFully(patchData);
			return BinaryPatcher.patch(classData, patchData, newSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classData;
	}

	public static Map<Integer, Integer> initPatchFixes() {
		Map<Integer, Integer> map = new HashMap<>();
		try (DataInputStream dis = new DataInputStream(AndroidProducer.class.getResourceAsStream("/assets/dexer/patches.bin"))) {
			int pos = 0;
			//noinspection InfiniteLoopStatement
			while (true) {
				int key = dis.readInt();
				pos += 4;
				map.put(key, pos);
				int len = dis.readUnsignedShort();
				dis.skipBytes(len);
				pos += len + 2;
			}
		} catch (EOFException ignored) {
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return map;
	}
}
