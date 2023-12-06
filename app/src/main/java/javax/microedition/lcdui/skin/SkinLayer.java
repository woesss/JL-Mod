/*
 * Copyright 2023 Yury Kharchenko
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

package javax.microedition.lcdui.skin;

import android.graphics.Bitmap;
import android.graphics.RectF;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.graphics.CanvasWrapper;
import javax.microedition.lcdui.overlay.Overlay;

import ru.playsoftware.j2meloader.config.ProfileModel;

public class SkinLayer implements Overlay {
	private static SkinLayer instance;

	private final Bitmap image;
	private final ProfileModel settings;
	private final RectF clip = new RectF();
	private final RectF bounds = new RectF();
	private final RectF display = new RectF();

	public SkinLayer(Bitmap image, ProfileModel settings) {
		this.image = image;
		this.settings = settings;
		int imageHeight = image.getHeight();
		int imageWidth = image.getWidth();
		for (int t = 0; t < imageHeight; t++) {
			for (int l = 0; l < imageWidth; l++) {
				if (image.getPixel(l, t) != 0) {
					continue;
				}
				int r = l + 1;
				while (r < imageWidth && image.getPixel(r, t) == 0) {
					r++;
				}
				if (r - l < Math.min(imageWidth, imageHeight) / 2) {
					continue;
				}

				int b = t + 1;
				while (b < imageHeight && image.getPixel(l, b) == 0) {
					b++;
				}
				if (b - t < Math.min(imageWidth, imageHeight) / 2) {
					continue;
				}
				display.set(l, t, r, b);
				return;
			}
		}
	}

	public static SkinLayer getInstance() {
		return instance;
	}

	public static void init(Bitmap image, ProfileModel settings) {
		instance = new SkinLayer(image, settings);
	}

	@Override
	public void paint(CanvasWrapper g) {
		g.drawBackground(image, bounds, clip);
	}

	@Override
	public void setTarget(Canvas canvas) {

	}

	@Override
	public void resize(RectF screen, float left, float top, float right, float bottom) {
		bounds.set(left, top, right, bottom);
		if (display.isEmpty()) {
			int p = settings.screenPadding;
			clip.set(screen);
			clip.intersect(p, p, right - p, bottom - p);
		} else {
			clip.set(display);
			scale();
			clip.offset(left, top);
			screen.set(clip);
		}
	}

	private void scale() {
		float horizontalScale = bounds.width() / image.getWidth();
		clip.left = display.left * horizontalScale;
		clip.right = display.right * horizontalScale;
		float verticalScale = bounds.height() / image.getHeight();
		clip.top = display.top * verticalScale;
		clip.bottom = display.bottom * verticalScale;
	}

	@Override
	public boolean keyPressed(int keyCode) {
		return false;
	}

	@Override
	public boolean keyRepeated(int keyCode) {
		return false;
	}

	@Override
	public boolean keyReleased(int keyCode) {
		return false;
	}

	@Override
	public boolean pointerPressed(int pointer, float x, float y) {
		return false;
	}

	@Override
	public boolean pointerDragged(int pointer, float x, float y) {
		return false;
	}

	@Override
	public boolean pointerReleased(int pointer, float x, float y) {
		return false;
	}

	@Override
	public void show() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void cancel() {

	}

	public boolean hasDisplayFrame() {
		return !display.isEmpty();
	}
}
