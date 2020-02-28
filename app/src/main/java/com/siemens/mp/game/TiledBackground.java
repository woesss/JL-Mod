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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import javax.microedition.lcdui.Image;

public class TiledBackground extends GraphicObject {
	private static final String TAG = TiledBackground.class.getName();
	private Bitmap pixels;
	private byte[][] map;
	private int widthInTiles;
	private int heightInTiles;
	private int posX;
	private int posY;
	private Rect frame = new Rect(0, 0, 8, 8);
	private Rect dst = new Rect(0, 0, 8, 8);
	private Paint paint = new Paint();

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

	public TiledBackground(Image tilePixels, Image tileMask, byte[] map,
						   int widthInTiles, int heightInTiles) {
		paint.setStyle(Paint.Style.FILL);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		this.map = new byte[heightInTiles][widthInTiles];
		this.heightInTiles = heightInTiles;
		this.widthInTiles = widthInTiles;

		int idx = 0;
		for (int i = 0; i < heightInTiles; i++) {
			byte[] row = this.map[i];
			for (int j = 0; j < widthInTiles; j++) {
				row[j] = map[idx++];
			}
		}
		pixels = tilePixels.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
	}

	public void setPositionInMap(int x, int y) {
		posX = x;
		posY = y;
	}

	protected void paint(Canvas c, int x, int y) {
		final Rect clip = c.getClipBounds();
		clip.left = Math.max(clip.left, x);
		clip.top = Math.max(clip.top, y);
		final int save = c.save();
		c.clipRect(clip);
		final int left = x - (posX % 8);
		final int top = y - (posY % 8);
		dst.offsetTo(left, top);
		try {
			for (int ty = posY / 8, tyLen = clip.height() / 8 + 1 + ty; ty < tyLen; ty++) {
				byte[] row = this.map[ty % heightInTiles];
				for (int tx = posX / 8, txLen = clip.width() / 8 + 1 + tx; tx < txLen; tx++) {
					final byte tile = row[tx % widthInTiles];
					switch (tile) {
						case 0:
							paint.setColor(0);
							c.drawRect(dst, paint);
							break;
						case 1:
							paint.setColor(Color.WHITE);
							c.drawRect(dst, paint);
							break;
						case 2:
							paint.setColor(Color.BLACK);
							c.drawRect(dst, paint);
							break;
						default:
							frame.offsetTo(0, (tile - 3) * 8);
							c.drawBitmap(pixels, frame, dst, null);
					}
					dst.offset(8, 0);
				}
				dst.left = left;
				dst.top += 8;
				dst.right = left + 8;
				dst.bottom += 8;
			}
		} catch (Throwable t) {
			Log.e(TAG, "paint: ", t);
		} finally {
			c.restoreToCount(save);
		}
	}
}