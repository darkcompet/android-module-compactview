/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import tool.compet.appcompat.DkTextView;

/**
 * This extends compat-version and provided some optional below features:
 * - [Optional] corner-rounded view (default: true)
 * - [Optional] foreground with ripple animation
 */
public class DkCompactTextView extends DkTextView {
	protected Context context;

	// Rounded corner feature
	protected boolean isRoundCornerFeatureEnabled = true;
	protected TheCompactFeatureRoundCorner cmpRoundCorner;

	public DkCompactTextView(Context context) {
		super(context);
		init(context);
	}

	public DkCompactTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DkCompactTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (isRoundCornerFeatureEnabled()) {
			obtainRoundCornerComponent().onSizeChanged(w, h);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void draw(Canvas canvas) {
		if (isRoundCornerFeatureEnabled()) {
			obtainRoundCornerComponent().drawRoundedCorner(this, canvas);
		}
		super.draw(canvas);
	}

	public void setRoundCornerFeatureEnabled(boolean enable) {
		isRoundCornerFeatureEnabled = enable;
	}

	public boolean isRoundCornerFeatureEnabled() {
		return isRoundCornerFeatureEnabled;
	}

	/**
	 * Call this to obtain (prepare) rounded corner setting component (setting).
	 */
	public TheCompactFeatureRoundCorner obtainRoundCornerComponent() {
		if (cmpRoundCorner == null) {
			cmpRoundCorner = new TheCompactFeatureRoundCorner(context);
		}
		return cmpRoundCorner;
	}
}
