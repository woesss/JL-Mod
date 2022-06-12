/*
 *  Siemens API for MicroEmulator
 *  Copyright (C) 2003 Markus Heberling <markus@heberling.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 */

package com.siemens.mp.ui;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.nio.IntBuffer;

public class Image extends com.siemens.mp.misc.NativeMem {
	public static final int COLOR_BMP_8BIT = 5;

	protected Image() {
	}

	public Image(int imageWidth, int imageHeight) {}

	public Image(Image image) {}

	public Image(byte[] bytes, int imageWidth, int imageHeight) {}

	public Image(byte[] bytes, int imageWidth, int imageHeight, boolean transparent) {}

	public Image(String name, boolean doScale) throws IOException {}

	public Image(byte[] imageData) {}

	public Image(byte[] bytes, int imageWidth, int imageHeight, int BitmapType) throws IOException {}

	public int getHeight() {
		return 0;
	}

	public int getWidth() {
		return 0;
	}

	public static javax.microedition.lcdui.Image createImageWithScaling(String name)
			throws IOException {
		return null;
	}

	public static javax.microedition.lcdui.Image createImageWithoutScaling(String name)
			throws IOException {
		return javax.microedition.lcdui.Image.createImage(name);
	}

	public static javax.microedition.lcdui.Image createImageFromBitmap(byte[] imageData, int imageWidth, int imageHeight) {
		return createImageFromBitmap(imageData, null, imageWidth, imageHeight);
	}

	public static javax.microedition.lcdui.Image createImageFromBitmap(byte[] imageData, byte[] alpha, int imageWidth, int imageHeight) {
		if (imageData == null) return null;

		if (imageWidth < 8) imageWidth = 8;

		int pixLen = imageWidth * imageHeight;
		int[] pixres = new int[pixLen];
		int idx = 0;
		for (int i = 0; i < imageData.length; i++) {
			int c = imageData[i] & 0xff;
			int a = alpha == null ? 0xff : alpha[i] & 0xff;
			for (int j = 7; j >= 0; j--) {
				int cb = c >> j;
				int ab = a >> j;
				pixres[idx++] = Color.BLACK * (ab & 1) | 0xffffff * (1 - (cb & 1));
			}
		}
		javax.microedition.lcdui.Image rgbImage = javax.microedition.lcdui.Image.createRGBImage(pixres, imageWidth, imageHeight, true);
		rgbImage.setBlackWhiteAlpha(alpha != null);
		return rgbImage;
	}

	public static javax.microedition.lcdui.Image createRGBImage(byte[] imageData,
																int imageWidth,
																int imageHeight,
																int BitmapType)
			throws IOException {
		if (imageWidth <= 0 || imageHeight <= 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (BitmapType != COLOR_BMP_8BIT) {
			throw new IOException("BitmapType = " + BitmapType);
		}
		int[] pixels = new int[imageWidth * imageHeight];
		for (int i = 0; i < pixels.length; i++) {
			int c = imageData[i] & 0xff;
			if (c == 0xc0) {
				pixels[i] = 0;
				continue;
			}
			int r = (((c >> 5) & 0b111) * 255) / 7;
			int g = (c >> 2 & 0b111) * 255 / 7;
			int b = (c & 0b11) * 255 / 3;
			pixels[i] = 0xff000000 | r << 16 | g << 8 | b;
		}
		return javax.microedition.lcdui.Image.createRGBImage(pixels, imageWidth, imageHeight, true);
	}

	public static javax.microedition.lcdui.Image createTransparentImageFromBitmap(byte[] imageData, int imageWidth, int imageHeight) {
		if (imageData == null) return null;

		if (imageWidth < 4) imageWidth = 4;

		int[] pixres = new int[imageHeight * imageWidth];
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth / 4; x++) {
				for (int b = 7; b >= 0; b -= 2) {
					int c = doAlpha(imageData, y * imageWidth / 4 + x, b);
					pixres[x * 4 + 3 - b / 2 + y * imageWidth] = c;
				}
			}
		}
		javax.microedition.lcdui.Image image = javax.microedition.lcdui.Image.createRGBImage(pixres, imageWidth, imageHeight, true);
		image.setBlackWhiteAlpha(true);
		return image;
	}

	private static boolean isBitSet(byte b, int pos) {
		return ((b & (byte) (1 << pos)) != 0);
	}

	private static int doAlpha(byte[] pix, int pos, int shift) {
		int p;
		int a;
		if (isBitSet(pix[pos], shift))
			p = 0;
		else
			p = 0x00FFFFFF;
		if (isBitSet(pix[pos], shift) || isBitSet(pix[pos], shift - 1))
			a = 0xFF000000;
		else
			a = 0;
		return p | a;
	}

	public static void mirrorImageHorizontally(javax.microedition.lcdui.Image image) {
		Bitmap bitmap = image.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] tmp = new int[width * height];
		IntBuffer buffer = IntBuffer.wrap(tmp);
		bitmap.copyPixelsToBuffer(buffer);
		for (int i = 0; i < height; ) {
			for (int f = i * width, s = ++i * width; f < s; f++, s--) {
				int c = tmp[f];
				tmp[f] = tmp[s];
				tmp[s] = c;
			}
		}
		buffer.rewind();
		bitmap.copyPixelsFromBuffer(buffer);
	}

	public static void mirrorImageVertically(javax.microedition.lcdui.Image image) {}

	protected static void setNativeImage(javax.microedition.lcdui.Image img, Image simg) {}

	public static Image getNativeImage(javax.microedition.lcdui.Image img) {
		return null;
	}
}
