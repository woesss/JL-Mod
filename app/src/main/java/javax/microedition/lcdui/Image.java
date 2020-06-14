/*
 * Copyright 2012 Kulikov Dmitriy
 * Copyright 2017-2018 Nikita Shakarun
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

package javax.microedition.lcdui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.game.Sprite;
import javax.microedition.util.ContextHolder;

public class Image {

	private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() >> 2); // 1/4 heap max
	private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(CACHE_SIZE) {
		@Override
		protected int sizeOf(String key, Bitmap value) {
			return value.getByteCount();
		}
	};

	private Bitmap mBitmap;
	private Canvas canvas;
	private Graphics mGraphics;
	private int save;
	private Rect mBounds;
	private boolean isBlackWhiteAlpha;

	public Image(Bitmap bitmap) {
		if (bitmap == null) {
			throw new NullPointerException();
		}
		this.mBitmap = bitmap;
		mBounds = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public Canvas getCanvas() {
		if (canvas == null) {
			canvas = new Canvas(mBitmap);
			save = canvas.save();
		}

		return canvas;
	}

	public static Image createImage(int width, int height) {
		return createImage(width, height, Color.WHITE);
	}

	@NonNull
	public static Image createImage(int width, int height, int argb) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		if (argb != 0) bitmap.eraseColor(argb);
		return new Image(bitmap);
	}

	public static Image createImage(String resname) throws IOException {
		synchronized (CACHE) {
			Bitmap b = CACHE.get(resname);
			if (b != null) {
				return new Image(b);
			}
			InputStream stream = ContextHolder.getResourceAsStream(null, resname);
			if (stream == null) {
				throw new IOException("Can't read image: " + resname);
			}
			b = BitmapFactory.decodeStream(stream);
			if (b == null) {
				throw new IOException("Can't decode image: " + resname);
			}
			CACHE.put(resname, b);
			return new Image(b);
		}
	}

	public static Image createImage(InputStream stream) {
		return new Image(BitmapFactory.decodeStream(stream));
	}

	public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
		return new Image(BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength));
	}

	public static Image createImage(Image image, int x, int y, int width, int height, int transform) {
		return new Image(Bitmap.createBitmap(image.mBitmap, x, y, width, height, Sprite.transformMatrix(transform, width / 2f, height / 2f), false));
	}

	public static Image createImage(Image image) {
		return new Image(Bitmap.createBitmap(image.mBitmap));
	}

	public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
		if (!processAlpha) {
			final int length = width * height;
			int[] tmp = new int[length];
			for (int i = 0; i < length; i++) {
				tmp[i] = rgb[i] | 0xFF000000;
			}
			rgb = tmp;
		}
		return new Image(Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888));
	}

	public Graphics getGraphics() {
		return new Graphics(this);
	}

	public boolean isMutable() {
		return mBitmap.isMutable();
	}

	public int getWidth() {
		return mBounds.right;
	}

	public int getHeight() {
		return mBounds.bottom;
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		mBitmap.getPixels(rgbData, offset, scanlength, x, y, width, height);
	}

	void copyTo(Image dst) {
		dst.getCanvas().drawBitmap(mBitmap, mBounds, mBounds, null);
	}

	void copyTo(Image dst, int x, int y) {
		Rect r = new Rect(x, y, x + mBounds.right, y + mBounds.bottom);
		dst.getCanvas().drawBitmap(mBitmap, mBounds, r, null);
	}

	Graphics getSingleGraphics() {
		if (mGraphics == null) {
			mGraphics = getGraphics();
		}
		return mGraphics;
	}

	void resetCanvas() {
		getCanvas();
		try {
			canvas.restoreToCount(save);
		} catch (Exception e) {
			canvas.restoreToCount(1);
		}
		save = canvas.save();
	}

	void setSize(int width, int height) {
		mBounds.right = width;
		mBounds.bottom = height;
		getCanvas().clipRect(mBounds);
	}

	public Rect getBounds() {
		return mBounds;
	}

	public boolean isBlackWhiteAlpha() {
		return isBlackWhiteAlpha;
	}

	public void setBlackWhiteAlpha(boolean blackWhiteAlpha) {
		isBlackWhiteAlpha = blackWhiteAlpha;
	}
}
