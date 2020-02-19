/*
 *  Nokia API for MicroEmulator
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

package com.nokia.mid.ui;

import javax.microedition.lcdui.Image;


public interface DirectGraphics {
	int FLIP_HORIZONTAL = 8192;
	int FLIP_VERTICAL = 16384;
	int ROTATE_90 = 90;
	int ROTATE_180 = 180;
	int ROTATE_270 = 270;
	int TYPE_BYTE_1_GRAY = 1;
	int TYPE_BYTE_1_GRAY_VERTICAL = -1;
	int TYPE_BYTE_2_GRAY = 2;
	int TYPE_BYTE_4_GRAY = 4;
	int TYPE_BYTE_8_GRAY = 8;
	int TYPE_BYTE_332_RGB = 332;
	int TYPE_USHORT_4444_ARGB = 4444;
	int TYPE_USHORT_444_RGB = 444;
	int TYPE_USHORT_555_RGB = 555;
	int TYPE_USHORT_1555_ARGB = 1555;
	int TYPE_USHORT_565_RGB = 565;
	int TYPE_INT_888_RGB = 888;
	int TYPE_INT_8888_ARGB = 8888;


	void drawImage(Image image, int x, int y, int anchor, int manipulation);

	void drawPixels(byte[] pixels, byte[] transparencyMask, int offset, int scanlength, int x, int y, int width,
					int height, int manipulation, int format);

	void drawPixels(int[] pixels, boolean transparency, int offset, int scanlength, int x, int y, int width,
					int height, int manipulation, int format);

	void drawPixels(short[] pixels, boolean transparency, int offset, int scanlength, int x, int y, int width,
					int height, int manipulation, int format);

	void drawPolygon(int[] xPoints, int xOffset, int[] yPoints, int yOffset, int nPoints, int argbColor);

	void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);

	void fillPolygon(int[] xPoints, int xOffset, int[] yPoints, int yOffset, int nPoints, int argbColor);

	void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);

	int getAlphaComponent();

	int getNativePixelFormat();

	void getPixels(byte[] pixels, byte[] transparencyMask, int offset, int scanlength, int x, int y, int width,
				   int height, int format);

	void getPixels(int[] pixels, int offset, int scanlength, int x, int y, int width,
				   int height, int format);

	void getPixels(short[] pixels, int offset, int scanlength, int x, int y, int width,
				   int height, int format);

	void setARGBColor(int i);

}