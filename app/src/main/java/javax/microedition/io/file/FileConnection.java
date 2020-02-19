/**
 * MicroEmulator
 * Copyright (C) 2006-2007 Bartek Teodorczyk <barteo@barteo.net>
 * Copyright (C) 2006-2007 Vlad Skarzhevskyy
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
package javax.microedition.io.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.microedition.io.StreamConnection;

public interface FileConnection extends StreamConnection {

	boolean isOpen();

	@Override
	InputStream openInputStream() throws IOException;

	@Override
	DataInputStream openDataInputStream() throws IOException;

	@Override
	OutputStream openOutputStream() throws IOException;

	@Override
	DataOutputStream openDataOutputStream() throws IOException;

	OutputStream openOutputStream(long byteOffset) throws IOException;

	long totalSize();

	long availableSize();

	long usedSize();

	long directorySize(boolean includeSubDirs) throws IOException;

	long fileSize() throws IOException;

	boolean canRead();

	boolean canWrite();

	boolean isHidden();

	void setReadable(boolean readable) throws IOException;

	void setWritable(boolean writable) throws IOException;

	void setHidden(boolean hidden) throws IOException;

	Enumeration list() throws IOException;

	Enumeration list(String filter, boolean includeHidden) throws IOException;

	void create() throws IOException;

	void mkdir() throws IOException;

	boolean exists();

	boolean isDirectory();

	void delete() throws IOException;

	void rename(String newName) throws IOException;

	void truncate(long byteOffset) throws IOException;

	void setFileConnection(String s) throws IOException;

	String getName();

	String getPath();

	String getURL();

	long lastModified();
}
