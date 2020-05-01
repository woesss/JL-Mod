package com.nokia.mid.ui;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.*;

public class DirectUtilsTest {

	@Test
	public void createImage() throws Exception {
		try (InputStream is = getClass().getResourceAsStream("/res/mipmap-hdpi-v4/ic_launcher.png")) {
			assertNotNull(is);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int read;
			while ((read = is.read(buf)) != -1) {
				baos.write(buf, 0, read);
			}
			buf = baos.toByteArray();
			assertTrue(DirectUtils.createImage(buf, 0, buf.length).isMutable());
		}
	}
}