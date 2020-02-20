/*
 *  Copyright 2020 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.siemens.mp.game;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class TiledBackground extends GraphicObject {
	private Image pixels;
	private byte[][] map;
	private int widthInTiles;
	private int heightInTiles;
	private int posX;
	private int posY;

	public TiledBackground(byte[] tilePixels, byte[] tileMask, byte[] map,
						   int widthInTiles, int heightInTiles) {
		this(
				com.siemens.mp.ui.Image.createImageFromBitmap(tilePixels, tileMask, 8, tilePixels.length),
				null,
				map,
				widthInTiles,
				heightInTiles
		);
	}

	public TiledBackground(ExtendedImage tilePixels, ExtendedImage tileMask, byte[] map,
						   int widthInTiles, int heightInTiles) {
		this(tilePixels.getImage(), tileMask.getImage(), map, widthInTiles, heightInTiles);
	}

	public TiledBackground(Image tilePixels, Image tileMask, byte[] map, int widthInTiles, int heightInTiles) {
		this.map = new byte[heightInTiles][widthInTiles];
		this.heightInTiles = heightInTiles;
		this.widthInTiles = widthInTiles;

		pixels = Image.createImage(widthInTiles * 8, heightInTiles * 8);

		Canvas canvas = pixels.getCanvas();
		Bitmap bitmap = tilePixels.getBitmap();
		int idx = 0;
		Rect src = new Rect(0, 0, 8, 8);
		Rect dst = new Rect(0, 0, 8, 8);
		Paint paint = new Paint();
		for (int i = 0; i < heightInTiles; i++) {
			for (int j = 0; j < widthInTiles; j++) {
				dst.offsetTo(j * 8, i * 8);
				int tile = map[idx++] & 0xff;
				switch (tile) {
					case 0:
						break;
					case 1:
						paint.setColor(Color.WHITE);
						canvas.drawRect(dst, paint);
						break;
					case 2:
						paint.setColor(Color.BLACK);
						canvas.drawRect(dst, paint);
						break;
					default:
						src.offsetTo(0, (tile - 3) * 8);
						canvas.drawBitmap(bitmap, src, dst, null);
				}
			}
		}
	}

	public void setPositionInMap(int x, int y) {
		posX = x;
		posY = y;
	}

	protected void paint(Graphics g, int x, int y) {
		Canvas canvas = g.getCanvas();
		Bitmap bitmap = pixels.getBitmap();
		Rect src = new Rect(posX, posY, bitmap.getWidth(), bitmap.getHeight());
		Rect dst = new Rect(src);
		dst.offsetTo(x, y);
     	canvas.drawBitmap(bitmap, src, dst, null);
	}
}