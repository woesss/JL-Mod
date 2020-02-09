/**
 * MicroEmulator
 * Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
 * Copyright (C) 2017-2018 Nikita Shakarun
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

import android.util.Log;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ru.playsoftware.j2meloader.util.ZipFileCompat;

public class AndroidProducer {
	private static final String TAG = AndroidProducer.class.getName();

	private static byte[] instrument(final InputStream classFile, String classFileName)
			throws IllegalArgumentException, IOException {
		ClassReader cr = new ClassReader(classFile);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassVisitor cv = new AndroidClassVisitor(cw);
		if (!cr.getClassName().equals(classFileName)) {
			throw new IllegalArgumentException("Class name does not match path");
		}
		cr.accept(cv, ClassReader.SKIP_DEBUG);

		return cw.toByteArray();
	}

	public static void processJar(File src, File dst) throws IOException {
		try (ZipFileCompat zip = new ZipFileCompat(src);
			 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dst))) {
			ZipEntry zipEntry;
			while ((zipEntry = zip.getNextEntry()) != null) {
				String name = zipEntry.getName();
				if (name == null || name.endsWith("/") || !name.toLowerCase().endsWith(".class")) {
					continue;
				}
				try (InputStream zis = zip.getInputStream(zipEntry)) {
					byte[] outBuffer = instrument(zis, name.substring(0, name.length() - 6));
					zos.putNextEntry(new ZipEntry(name));
					zos.write(outBuffer);
				} catch (Exception e) {
					Log.w(TAG, "Error patching class: " + name, e);
				}
			}
		}
	}
}
