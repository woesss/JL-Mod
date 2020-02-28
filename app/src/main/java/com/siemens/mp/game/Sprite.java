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
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Sprite extends GraphicObject {
	private Bitmap sprite;
	private int frameHeight;
	private Rect dstBounds;
	private Rect frameBounds;
	private Rect collisionBounds;
	private int frame;
	private int posX;
	private int posY;

	public Sprite(byte[] pixels, int pixel_offset, int width, int height,
				  byte[] mask, int mask_offset, int numFrames) {
		if (pixels == null
				|| pixel_offset != 0
				|| mask_offset != 0
				|| width * height * numFrames / 8 != pixels.length
				|| (mask != null && pixels.length != mask.length)
				|| width % 8 != 0)
			throw new IllegalArgumentException();
		Image image = com.siemens.mp.ui.Image.createImageFromBitmap(pixels, mask, width, height * numFrames);
		init(image, null, numFrames);
	}

	public Sprite(ExtendedImage pixels, ExtendedImage mask, int numFrames) {
		if (pixels == null) throw new NullPointerException();
		init(pixels.getImage(), mask.getImage(), numFrames);
	}

	public Sprite(Image pixels, Image mask, int numFrames) {
		init(pixels, mask, numFrames);
	}

	private void init(Image pixels, Image mask, int numFrames) {
		if (pixels == null) {
			throw new NullPointerException();
		}
		int width = pixels.getWidth();
		int height = pixels.getHeight();
		sprite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(sprite);
		canvas.drawBitmap(pixels.getBitmap(), 0, 0, null);
		if (mask != null) {
			Paint paint = new Paint();
			float[] src = {
					0, 0, 0, 0, 0,
					0, 0, 0, 0, 0,
					0, 0, 0, 0, 0,
					1, 1, 1, 0, -1,
			};
			paint.setColorFilter(new ColorMatrixColorFilter(src));
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
			canvas.drawBitmap(mask.getBitmap(), 0, 0, paint);
		}
		frameHeight = height / numFrames;
		frameBounds = new Rect(0, 0, width, frameHeight);
		dstBounds = new Rect(frameBounds);
		collisionBounds = new Rect(dstBounds);
	}

	public int getFrame() {
		return frame;
	}

	public int getXPosition() {
		return posX;
	}

	public int getYPosition() {
		return posY;
	}

	public boolean isCollidingWith(Sprite other) {
		return Rect.intersects(collisionBounds, other.collisionBounds);
	}

	public boolean isCollidingWithPos(int xpos, int ypos) {
		return collisionBounds.contains(xpos, ypos);
	}

	public void setCollisionRectangle(int x, int y, int width, int height) {
		int dx = posX;
		int dy = posY;
		collisionBounds.set(dx + x, dy + y, dx + x + width, dy + y + height);
	}

	public void setFrame(int frameNumber) {
		frame = frameNumber;
		frameBounds.offsetTo(0, frameHeight * frameNumber);
	}

	public void setPosition(int x, int y) {
		collisionBounds.offset(x - posX, y - posY);
		posX = x;
		posY = y;
	}

	protected void paint(Graphics g, int x, int y) {
		Canvas canvas = g.getCanvas();
		dstBounds.offsetTo(x + posX, y + posY);
		canvas.drawBitmap(sprite, frameBounds, dstBounds, null);
	}
}
