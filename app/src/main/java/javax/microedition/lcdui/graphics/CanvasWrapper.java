/*
 * Copyright 2020-2024 Yury Kharchenko
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

package javax.microedition.lcdui.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import javax.microedition.lcdui.Image;
import javax.microedition.util.ContextHolder;

import ru.playsoftware.j2meloader.R;

public class CanvasWrapper {
	private final Paint drawPaint = new Paint();
	private final Paint fillPaint = new Paint();
	private final Paint textPaint = new Paint();
	private final Paint imgPaint = new Paint();
	private final float textSize;
	private final boolean filterBitmap;

	private float textAscent;
	private float textCenterOffset;
	private float textHeight;
	private Canvas canvas;

	public CanvasWrapper(boolean filterBitmap) {
		this.filterBitmap = filterBitmap;
		imgPaint.setFilterBitmap(filterBitmap);
		drawPaint.setStyle(Paint.Style.STROKE);
		fillPaint.setStyle(Paint.Style.FILL);

		// init text paint
		Context context = ContextHolder.getAppContext();
		textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.roboto_regular));
		textSize = context.getResources().getDimension(R.dimen._22sp);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textAscent = textPaint.ascent();
		float descent = textPaint.descent();
		textHeight = descent - textAscent;
		textCenterOffset = ((descent + textAscent) / 2);
	}

	public void bind(Canvas canvas) {
		this.canvas = canvas;
	}

	public void clear(int color) {
		canvas.drawColor(color, PorterDuff.Mode.SRC);
	}

	public void drawArc(RectF oval, int startAngle, int sweepAngle) {
		canvas.drawArc(oval, startAngle, sweepAngle, false, drawPaint);
	}

	public void fillArc(RectF oval, int startAngle, int sweepAngle) {
		canvas.drawArc(oval, startAngle, sweepAngle, false, fillPaint);
	}

	public void drawRoundRect(RectF rect, int rx, int ry) {
		canvas.drawRoundRect(rect, rx, ry, drawPaint);
	}

	public void fillRoundRect(RectF rect, int rx, int ry) {
		canvas.drawRoundRect(rect, rx, ry, fillPaint);
	}

	public void drawString(String text, float x, float y) {
		canvas.drawText(text, x, y - textCenterOffset, textPaint);
	}

	public void drawImage(Image image, RectF dst) {
		Bitmap bitmap = image.getBitmap();
		bitmap.prepareToDraw();
		canvas.drawBitmap(bitmap, image.getBounds(), dst, imgPaint);
	}

	public void fillRect(RectF rect) {
		canvas.drawRect(rect, fillPaint);
	}

	public void drawRect(RectF rect) {
		canvas.drawRect(rect, drawPaint);
	}

	public void setDrawColor(int color) {
		drawPaint.setColor(color);
	}

	public void setFillColor(int color) {
		fillPaint.setColor(color);
	}

	public void setTextColor(int color) {
		textPaint.setColor(color);
	}

	public void drawBackgroundedText(String text) {
		float width = textPaint.measureText(text);
		canvas.drawRect(0, 0, width, textHeight, fillPaint);
		canvas.drawText(text, width / 2.0f, -textAscent, textPaint);
	}

	public float getTextHeight() {
		return textHeight;
	}

	public void setTextAlign(Paint.Align align) {
		textPaint.setTextAlign(align);
	}

	public void setTextScale(float scale) {
		textPaint.setTextSize(textSize * scale);
		textAscent = textPaint.ascent();
		float descent = textPaint.descent();
		textHeight = descent - textAscent;
		textCenterOffset = ((descent + textAscent) / 2);
	}

	public void drawBackground(Bitmap bitmap, RectF dst, RectF exclude) {
		int save = canvas.save();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			canvas.clipOutRect(exclude);
		} else {
			canvas.clipRect(exclude, Region.Op.DIFFERENCE);
		}
		imgPaint.setFilterBitmap(true);
		canvas.drawBitmap(bitmap, null, dst, imgPaint);
		imgPaint.setFilterBitmap(filterBitmap);
		canvas.restoreToCount(save);
	}
}
