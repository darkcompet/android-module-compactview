/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;

import tool.compet.core.DkConfig;

/**
 * Compact component for rounded corner feature.
 */
public class TheCompactFeatureRoundCorner {
	int roundColor;
	boolean isCircle;
	float roundStrokeWidth;
	float[] roundRadiusArr; // top-left, top-right, bottom-right, bottom-left
	Path clipRoundPath;
	Path roundPath;
	Paint roundPaint;

	TheCompactFeatureRoundCorner(Context context) {
		// Init with default values (user can change later if want)
		this.clipRoundPath = new Path();
		this.roundPath = new Path();
		final int roundColor = this.roundColor = DkConfig.colorAccent(context);
		final float roundRadius = 16 * DkConfig.density();

		final Paint roundPaint = this.roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		roundPaint.setStyle(Paint.Style.STROKE);
		roundPaint.setColor(roundColor);
		roundPaint.setStrokeWidth(roundStrokeWidth);

		this.roundRadiusArr = new float[] {
			roundRadius, roundRadius,
			roundRadius, roundRadius,
			roundRadius, roundRadius,
			roundRadius, roundRadius,
		};
	}

	void onSizeChanged(int w, int h) {
		// Clip round-path
		clipRoundPath.reset();
		if (isCircle) {
			clipRoundPath.addCircle(w >> 1, h >> 1, Math.min(w, h) >> 1, Path.Direction.CCW);
		}
		else {
			clipRoundPath.addRoundRect(new RectF(0, 0, w, h), roundRadiusArr, Path.Direction.CCW);
		}

		if (shouldDrawBorder()) {
			float m = DkConfig.density();
			roundPath.reset();
			roundPath.addRoundRect(new RectF(m, m, w - m, h - m), roundRadiusArr, Path.Direction.CCW);
		}
	}

	void drawRoundedCorner(View view, Canvas canvas) {
		//todo Buggy: setLayerType(LAYER_TYPE_SOFTWARE) makes redraw called repeatly !!!
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) { // api 17-
			view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		canvas.clipPath(clipRoundPath);
		if (shouldDrawBorder()) {
			canvas.drawPath(roundPath, roundPaint);
		}

		if (BuildConfig.DEBUG) {
			//todo if present, should fix? :)
//			DkLogcats.notice(this, "Buggy if this appears multiple times");
		}
	}

	// region Get/Set

	public int getRoundColor() {
		return roundColor;
	}

	public void setRoundColor(int roundColor) {
		this.roundColor = roundColor;
		this.roundPaint.setColor(roundColor);
	}

	public float getRoundStrokeWidth() {
		return roundStrokeWidth;
	}

	/**
	 * @param roundStrokeWidth In dp since we will multiply it with device density.
	 */
	public void setRoundStrokeWidth(float roundStrokeWidth) {
		this.roundStrokeWidth = roundStrokeWidth * DkConfig.density();
		this.roundPaint.setStrokeWidth(this.roundStrokeWidth);
	}

	public void setRoundRadius(float roundRadius) {
		float radius = roundRadius * DkConfig.density();
		this.roundRadiusArr = new float[] {
			radius, radius,
			radius, radius,
			radius, radius,
			radius, radius,
		};
	}

	public float[] getRoundRadiusArr() {
		return roundRadiusArr;
	}

	public void setRoundRadiusArr(float[] roundRadiusArr) {
		this.roundRadiusArr = roundRadiusArr;
	}

	public boolean isCircle() {
		return isCircle;
	}

	public void setCircle(boolean circle) {
		isCircle = circle;
	}

	// endregion Get/Set

	private boolean shouldDrawBorder() {
		return roundStrokeWidth > 0;
	}
}
