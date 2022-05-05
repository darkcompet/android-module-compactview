/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;

import tool.compet.compactview.databinding.DkConfirmDialogHorizonalActionsBinding;
import tool.compet.compactview.databinding.DkConfirmDialogVerticalActionsBinding;
import tool.compet.core.DkConfig;
import tool.compet.core.TheDialogFragment;
import tool.compet.animation.DkAnimationConfiguration;
import tool.compet.animation.DkLookupTableInterpolator;
import tool.compet.graphics.DkDrawables;
import tool.compet.core.DkRunner;
import tool.compet.core.DkRunner2;
import tool.compet.view.DkViews;

/**
 * By default,
 * - Title, subtitle, message, buttons are gone
 * - Auto dismiss dialog when click to buttons or outside dialog
 * - Default open, close animation
 */
public class DkAlertDialog
	extends DkCompactFragment<ViewDataBinding>
	implements TheDialogFragment, View.OnClickListener, TheConfirmDialog {

	// Indicate this dialog is dismissable for some actions as: back pressed...
	// Default value is true, so this dialog can dismissed by user's cancel-action
	protected boolean cancelable = true;

	private static final int NORMAL = Color.parseColor("#333333");
	private static final int ASK = Color.parseColor("#009b8b");
	private static final int ERROR = Color.parseColor("#ff0000");
	private static final int WARNING = Color.parseColor("#ff9500");
	private static final int INFO = Color.parseColor("#493ebb");
	private static final int SUCCESS = Color.parseColor("#00bb4d");

	public static final int LAYOUT_TYPE_HORIZONTAL_ACTIONS = 0;
	public static final int LAYOUT_TYPE_VERTICAL_ACTIONS = 1;
	protected int layoutType = LAYOUT_TYPE_VERTICAL_ACTIONS;

	// Click listener for action-buttons
	private DkRunner onCancel;
	private DkRunner onReset;
	private DkRunner onOk;

	/**
	 * Dialog content.
	 */

	protected ViewGroup vContent;
	private Integer backgroundColor;
	private Drawable backgroundDrawable;

	/**
	 * Header
	 */

	protected View vHeader;
	protected TextView vTitle;
	protected int iconResId; // store in instance state
	protected int titleTextResId; // store in instance state
	protected CharSequence title; // store in instance state
	protected int subTitleTextResId; // store in instance state
	protected int headerBackgroundColor = Color.TRANSPARENT; // store in instance state
	protected int titleTextColor = Color.BLACK; // store in instance state

	/**
	 * Body
	 */

	protected ViewGroup vBody;
	protected int bodyLayoutResId; // store in instance state
	protected float widthPercent = 0.85f; // store in instance state
	protected float heightPercent; // store in instance state
	protected boolean dimensionRatioBasedOnWidth = true; // store in instance state
	protected float widthRatio; // store in instance state
	protected float heightRatio; // store in instance state
	// Content: message
	protected TextView vMessage;
	protected int messageTextResId; // store in instance state
	protected String message; // store in instance state
	protected Integer messageBackgroundColor; // store in instance state

	/**
	 * Footer
	 */

	protected TextView vCancel;
	protected TextView vReset;
	protected TextView vOk;
	protected int cancelTextResId; // store in instance state
	protected int resetTextResId; // store in instance state
	protected int okTextResId; // store in instance state

	/**
	 * Setting
	 */

	protected boolean isDismissOnClickButton = true;
	protected boolean isDismissOnTouchOutside = true;
	protected boolean isFullScreen;

	/**
	 * Animation
	 */

	// Zoom-in (bigger) and then Zomm-out (smaller)
	public static final int ANIM_ZOOM_IN_OUT = 1;
	// Like as spring mocks a ball which is pulling down
	public static final int ANIM_SWIPE_DOWN = 2;

	private ValueAnimator animator;
	private boolean enableEnterAnimation = true; // whether has animation when show dialog
	private boolean enableExitAnimation; // whether has animation when dismiss dialog
	private int enterAnimationType = ANIM_ZOOM_IN_OUT;
	private int exitAnimationType = -1;

	private Interpolator animInterpolator;
	private Interpolator defaultEnterAnimInterpolator;
	private Interpolator exitAnimInterpolator;

	private DkRunner2<ValueAnimator, View> animUpdater;

	@Override
	public int layoutResourceId() {
		if (this.layoutType == LAYOUT_TYPE_VERTICAL_ACTIONS) {
			return R.layout.dk_confirm_dialog_vertical_actions;
		}
		return R.layout.dk_confirm_dialog_horizonal_actions;
	}

	// By default, dialog will not support container for fragment-transaction
	// To enable it, subclass must re-define this function
	@Override
	public int fragmentContainerId() {
		return View.NO_ID;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.restoreInstanceState(savedInstanceState);
	}

	@CallSuper
	protected void restoreInstanceState(@Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			this.cancelable = savedInstanceState.getBoolean("DkConfirmDialog.cancelable", false);

			this.backgroundColor = savedInstanceState.getInt("DkConfirmDialog.backgroundColor");

			this.iconResId = savedInstanceState.getInt("DkConfirmDialog.iconResId");
			this.titleTextResId = savedInstanceState.getInt("DkConfirmDialog.titleTextResId");
			this.title = savedInstanceState.getCharSequence("DkConfirmDialog.title");
			this.subTitleTextResId = savedInstanceState.getInt("DkConfirmDialog.subTitleTextResId");
			this.headerBackgroundColor = savedInstanceState.getInt("DkConfirmDialog.headerBackgroundColor");

			this.bodyLayoutResId = savedInstanceState.getInt("DkConfirmDialog.bodyLayoutResId");
			this.widthPercent = savedInstanceState.getFloat("DkConfirmDialog.widthWeight");
			this.heightPercent = savedInstanceState.getFloat("DkConfirmDialog.heightWeight");

			this.messageTextResId = savedInstanceState.getInt("DkConfirmDialog.messageTextResId");
			this.message = savedInstanceState.getString("DkConfirmDialog.message");
			this.messageBackgroundColor = savedInstanceState.getInt("DkConfirmDialog.messageBackgroundColor");

			this.cancelTextResId = savedInstanceState.getInt("DkConfirmDialog.cancelTextResId");
			this.resetTextResId = savedInstanceState.getInt("DkConfirmDialog.resetTextResId");
			this.okTextResId = savedInstanceState.getInt("DkConfirmDialog.okTextResId");

			this.isDismissOnClickButton = savedInstanceState.getBoolean("DkConfirmDialog.isDismissOnClickButton");
			this.isDismissOnTouchOutside = savedInstanceState.getBoolean("DkConfirmDialog.isDismissOnTouchOutside");
			this.isFullScreen = savedInstanceState.getBoolean("DkConfirmDialog.isFullScreen");

			final NonConfigState ncs = obtainOwnViewModel(NonConfigState.class.getName(), NonConfigState.class);
			this.backgroundDrawable = ncs.backgroundDrawable;
			this.onCancel = ncs.onCancel;
			this.onReset = ncs.onReset;
			this.onOk = ncs.onOk;
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		this.storeInstanceState(outState);
	}

	@CallSuper
	protected void storeInstanceState(@NonNull Bundle outState) {
		outState.putBoolean("DkConfirmDialog.cancelable", cancelable);

		if (this.backgroundColor != null) {
			outState.putInt("DkConfirmDialog.backgroundColor", this.backgroundColor);
		}

		outState.putInt("DkConfirmDialog.iconResId", this.iconResId);
		outState.putInt("DkConfirmDialog.titleTextResId", this.titleTextResId);
		outState.putCharSequence("DkConfirmDialog.title", this.title);
		outState.putInt("DkConfirmDialog.subTitleTextResId", this.subTitleTextResId);
		outState.putInt("DkConfirmDialog.headerBackgroundColor", this.headerBackgroundColor);

		if (this.bodyLayoutResId > 0) {
			outState.putInt("DkConfirmDialog.bodyLayoutResId", this.bodyLayoutResId);
		}
		if (this.widthPercent > 0) {
			outState.putFloat("DkConfirmDialog.widthWeight", this.widthPercent);
		}
		if (this.heightPercent > 0) {
			outState.putFloat("DkConfirmDialog.heightWeight", this.heightPercent);
		}

		if (this.messageTextResId > 0) {
			outState.putInt("DkConfirmDialog.messageTextResId", this.messageTextResId);
		}
		if (this.message != null) {
			outState.putString("DkConfirmDialog.message", this.message);
		}
		if (this.messageBackgroundColor != null) {
			outState.putInt("DkConfirmDialog.messageBackgroundColor", this.messageBackgroundColor);
		}

		outState.putInt("DkConfirmDialog.cancelTextResId", this.cancelTextResId);
		outState.putInt("DkConfirmDialog.resetTextResId", this.resetTextResId);
		outState.putInt("DkConfirmDialog.okTextResId", this.okTextResId);

		outState.putBoolean("DkConfirmDialog.isDismissOnClickButton", this.isDismissOnClickButton);
		outState.putBoolean("DkConfirmDialog.isDismissOnTouchOutside", this.isDismissOnTouchOutside);
		outState.putBoolean("DkConfirmDialog.isFullScreen", this.isFullScreen);

		final NonConfigState ncs = obtainOwnViewModel(NonConfigState.class.getName(), NonConfigState.class);
		ncs.backgroundDrawable = this.backgroundDrawable;
		ncs.onCancel = this.onCancel;
		ncs.onReset = this.onReset;
		ncs.onOk = this.onOk;
	}

	@Override // onViewCreated() -> onViewStateRestored() -> onStart()
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		this.restoreInstanceState(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		// layout = background + foreground
		// foreground = innner-padding + content (= header + content + footer)
		// header = title + subtitle
		// content = custom-view || message
		// footer = buttons
		final View layout = super.onCreateView(inflater, container, savedInstanceState);

		// We have to init views since we have different layouts
		if (this.binder instanceof DkConfirmDialogHorizonalActionsBinding) {
			final DkConfirmDialogHorizonalActionsBinding binder = (DkConfirmDialogHorizonalActionsBinding) this.binder;
			this.vContent = binder.dkBackground;
			this.vBody = binder.dkBody;

			this.vHeader = binder.dkHeader;
			this.vTitle = binder.dkTitle;
			this.vMessage = binder.dkMessage;
			this.vCancel = binder.dkCancel;
			this.vReset = binder.dkReset;
			this.vOk = binder.dkOk;
		}
		else if (this.binder instanceof DkConfirmDialogVerticalActionsBinding) {
			final DkConfirmDialogVerticalActionsBinding binder = (DkConfirmDialogVerticalActionsBinding) this.binder;
			this.vContent = binder.dkBackground;
			this.vBody = binder.dkBody;

			this.vHeader = binder.dkHeader;
			this.vTitle = binder.dkTitle;
			this.vMessage = binder.dkMessage;
			this.vCancel = binder.dkCancel;
			this.vReset = binder.dkReset;
			this.vOk = binder.dkOk;
		}

		return layout;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		onSetupLayout(view);

		if (this.enableEnterAnimation) {
			showEnterAnimation();
		}
	}

	@Override // from View.OnClickListener interface
	public void onClick(View view) {
		// Perform callback
		final int viewId = view.getId();

		if (viewId == R.id.dk_cancel) {
			onCancelButtonClick(view);
		}
		else if (viewId == R.id.dk_reset) {
			onResetButtonClick(view);
		}
		else if (viewId == R.id.dk_ok) {
			onOkButtonClick(view);
		}

		// Dismiss (close) the dialog
		if (this.isDismissOnClickButton) {
			this.close();
		}
	}

	/**
	 * By default, this try to perform cancel-callback.
	 * Subclass can override to customize click event.
	 */
	protected void onCancelButtonClick(View button) {
	}

	/**
	 * By default, this try to perform reset-callback.
	 * Subclass can override to customize click event.
	 */
	protected void onResetButtonClick(View button) {
	}

	/**
	 * By default, this try to perform ok-callback.
	 * Subclass can override to customize click event.
	 */
	protected void onOkButtonClick(View button) {
	}

	/**
	 * Subclass can override to customize layout setting.
	 */
	@SuppressLint("ClickableViewAccessibility")
	protected void onSetupLayout(View view) {
		view.setOnTouchListener((v, event) -> {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					return true;
				case MotionEvent.ACTION_UP: {
					if (! DkViews.isInsideView(event, this.vContent)) {
						onClickOutside();
					}
					break;
				}
			}
			return false;
		});

		// Dialog content (rounded corner view)
		decorContent();

		// Header
		decorHeader();
		decorIcon();
		decorTitle();

		// Body
		decorBodyView();

		// Footer
		this.vCancel.setOnClickListener(this);
		decorCancelButton();

		this.vReset.setOnClickListener(this);
		decorResetButton();

		this.vOk.setOnClickListener(this);
		decorOkButton();

		// Background (dialog) dimension
		ViewGroup.LayoutParams bkgLayoutParams = this.vContent.getLayoutParams();
		final int[] dimensions = DkConfig.displaySize();

		if (this.isFullScreen) {
			bkgLayoutParams.width = bkgLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
		}
		else {
			if (this.widthPercent != 0) {
				bkgLayoutParams.width = (int) (dimensions[0] * this.widthPercent);
			}
			if (this.heightPercent != 0) {
				bkgLayoutParams.height = (int) (dimensions[1] * this.heightPercent);
			}
			if (this.widthRatio != 0 && this.heightRatio != 0) {
				if (this.dimensionRatioBasedOnWidth) {
					bkgLayoutParams.height = (int) (bkgLayoutParams.width * this.heightRatio / this.widthRatio);
				}
				else {
					bkgLayoutParams.width = (int) (bkgLayoutParams.height * this.widthRatio / this.heightRatio);
				}
			}
		}
		this.vContent.setLayoutParams(bkgLayoutParams);
	}

	/**
	 * By default, this check `isDismissOnTouchOutside` flag to dismiss dialog.
	 * Subclass can override to customize click event.
	 */
	protected void onClickOutside() {
		if (this.isDismissOnTouchOutside) {
			this.close();
		}
	}

	// region Get/Set

	public DkAlertDialog setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		if (vContent != null) {
			decorContent();
		}
		return this;
	}

	public DkAlertDialog setIcon(int iconResId) {
		this.iconResId = iconResId;
		if (vTitle != null) {
			decorIcon();
		}
		return this;
	}

	public DkAlertDialog setTitle(int titleResId) {
		this.titleTextResId = titleResId;
		if (vTitle != null) {
			decorTitle();
		}
		return this;
	}

	public DkAlertDialog setTitle(CharSequence title) {
		this.title = title;
		if (vTitle != null) {
			decorTitle();
		}
		return this;
	}

	public DkAlertDialog setTitleTextColor(int titleTextColor) {
		this.titleTextColor = titleTextColor;
		if (vTitle != null) {
			vTitle.setTextColor(titleTextColor);
		}
		return this;
	}

	public DkAlertDialog setMessage(int messageResId) {
		this.messageTextResId = messageResId;
		if (vMessage != null) {
			decorBodyView();
		}
		return this;
	}

	public DkAlertDialog setMessage(String message) {
		this.message = message;
		if (vMessage != null) {
			decorBodyView();
		}
		return this;
	}

	public DkAlertDialog setBodyView(int layoutResId) {
		this.bodyLayoutResId = layoutResId;
		if (vBody != null) {
			decorBodyView();
		}
		return this;
	}

	public DkAlertDialog setCancelButton(int textRes, DkRunner onCancel) {
		this.onCancel = onCancel;
		return setCancelButton(textRes);
	}

	public DkAlertDialog setCancelButton(int textResId) {
		this.cancelTextResId = textResId;
		if (vCancel != null) {
			decorCancelButton();
		}
		return this;
	}

	public DkAlertDialog setResetButton(int textResId, DkRunner onReset) {
		this.onReset = onReset;
		return this.setResetButton(textResId);
	}

	public DkAlertDialog setResetButton(int textResId) {
		this.resetTextResId = textResId;
		if (vReset != null) {
			decorResetButton();
		}
		return this;
	}

	public DkAlertDialog setOkButton(int textRes, DkRunner onOk) {
		this.onOk = onOk;
		return setOkButton(textRes);
	}

	public DkAlertDialog setOkButton(int textResId) {
		this.okTextResId = textResId;
		if (vOk != null) {
			decorOkButton();
		}
		return this;
	}

	public DkAlertDialog setDismissOnTouchOutside(boolean isDismissOnTouchOutside) {
		this.isDismissOnTouchOutside = isDismissOnTouchOutside;
		return this;
	}

	public DkAlertDialog setDismissOnClickButton(boolean dismissOnClickButton) {
		this.isDismissOnClickButton = dismissOnClickButton;
		return this;
	}

	public DkAlertDialog setFullScreen(boolean isFullScreen) {
		this.isFullScreen = isFullScreen;
		return this;
	}

	/**
	 * Set percent of width, height based on device size.
	 * @param widthPercent Percent based on device width.
	 */
	public DkAlertDialog setWidthPercent(float widthPercent) {
		this.widthPercent = widthPercent;
		return this;
	}

	/**
	 * Set percent of width, height based on device size.
	 * @param heightPercent Percent based on device height.
	 */
	public DkAlertDialog setHeightPercent(float heightPercent) {
		this.heightPercent = heightPercent;
		return this;
	}


	/**
	 * Set percent of width, height based on device size.
	 * @param widthPercent Percent based on device width.
	 * @param heightPercent Percent based on device height.
	 */
	public DkAlertDialog setDimensionPercent(float widthPercent, float heightPercent) {
		this.widthPercent = widthPercent;
		this.heightPercent = heightPercent;
		return this;
	}

	/**
	 * Set ratio between width and height.
	 * @param widthRatio Ratio of width.
	 * @param heightRatio Ratio of height.
	 * @param basedOnWidth Base when calculate rate, true (based on width), false (based on height).
	 */
	public DkAlertDialog setDimensionRatio(float widthRatio, float heightRatio, boolean basedOnWidth) {
		this.widthRatio = widthRatio;
		this.heightRatio = heightRatio;
		this.dimensionRatioBasedOnWidth = basedOnWidth;
		return this;
	}

	/**
	 * This dialog provides some layout type, for eg,. vertical layout, horizontal layout...
	 */
	public DkAlertDialog setLayoutType(int layoutType) {
		this.layoutType = layoutType;
		return this;
	}

	public DkAlertDialog asSuccess() {
		return asColor(SUCCESS);
	}

	public DkAlertDialog asError() {
		return asColor(ERROR);
	}

	public DkAlertDialog asWarning() {
		return asColor(WARNING);
	}

	public DkAlertDialog asAsk() {
		return asColor(ASK);
	}

	public DkAlertDialog asInfo() {
		return asColor(INFO);
	}

	public DkAlertDialog asColor(int color) {
		return setTitleTextColor(color);
	}

	public DkAlertDialog setHeaderBackgroundColor(int color) {
		this.headerBackgroundColor = color;
		if (this.vHeader != null) {
			this.vHeader.setBackgroundColor(color);
		}
		return this;
	}

	public DkAlertDialog setMessageBackgroundColor(int messageBackgroundColor) {
		this.messageBackgroundColor = messageBackgroundColor;
		if (this.vMessage != null) {
			this.vMessage.setBackgroundColor(messageBackgroundColor);
		}
		return this;
	}

	public void setEnableEnterAnimation(boolean enableEnterAnimation) {
		this.enableEnterAnimation = enableEnterAnimation;
	}

	public void setEnableExitAnimation(boolean enableExitAnimation) {
		this.enableExitAnimation = enableExitAnimation;
	}

	// endregion Get/Set

	// region Private

	private void decorContent() {
		if (this.vContent != null) {
			if (this.backgroundColor != null) {
				this.vContent.setBackgroundColor(this.backgroundColor);
			}
			if (this.backgroundDrawable != null) {
				ViewCompat.setBackground(this.vContent, this.backgroundDrawable);
			}
		}
	}

	private void decorHeader() {
		if (this.vHeader != null) {
			this.vHeader.setBackgroundColor(this.headerBackgroundColor);
		}
	}

	private void decorIcon() {
		if (this.vTitle != null) {
			if (this.iconResId > 0) {
				if (this.layoutType == LAYOUT_TYPE_VERTICAL_ACTIONS) {
					Drawable left = DkDrawables.loadDrawable(context, this.iconResId);
					this.vTitle.setCompoundDrawables(left, null, null, null);
				}
				else if (layoutType == LAYOUT_TYPE_HORIZONTAL_ACTIONS) {
					Drawable left = DkDrawables.loadDrawable(context, this.iconResId);
					this.vTitle.setCompoundDrawables(left, null, null, null);
				}

				if (this.vTitle.getVisibility() != View.VISIBLE) {
					this.vTitle.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void decorTitle() {
		if (this.vTitle != null) {
			if (this.titleTextResId > 0) {
				this.title = this.context.getString(this.titleTextResId);
			}
			if (this.title != null) {
				DkViews.setTextSize(this.vTitle, 1.25f * this.vReset.getTextSize());
				this.vTitle.setText(this.title);
				this.vTitle.setTextColor(this.titleTextColor);
				this.vTitle.setVisibility(View.VISIBLE);
			}
			else {
				this.vTitle.setVisibility(View.GONE);
			}
		}
	}

	private void decorBodyView() {
		if (this.vMessage != null && this.vBody != null) {
			if (this.messageTextResId > 0 || this.message != null) {
				if (this.messageBackgroundColor != null) {
					this.vMessage.setBackgroundColor(this.messageBackgroundColor);
				}
				if (this.messageTextResId > 0) {
					this.message = this.context.getString(this.messageTextResId);
				}
				DkViews.setTextSize(this.vMessage, 1.125f * this.vReset.getTextSize());
				this.vMessage.setMovementMethod(new ScrollingMovementMethod());
				this.vMessage.setText(this.message);
				this.vMessage.setVisibility(View.VISIBLE);
			}
			else if (this.bodyLayoutResId > 0) {
				this.vBody.removeAllViews();
				this.vBody.addView(View.inflate(this.context, this.bodyLayoutResId, null));
			}
		}
	}

	private void decorCancelButton() {
		if (this.vCancel != null) {
			if (this.cancelTextResId > 0) {
				this.vCancel.setVisibility(View.VISIBLE);
				this.vCancel.setText(this.cancelTextResId);
			}
			else {
				this.vCancel.setVisibility(View.GONE);
			}
		}
	}

	private void decorResetButton() {
		if (this.vReset != null) {
			if (this.resetTextResId > 0) {
				this.vReset.setVisibility(View.VISIBLE);
				this.vReset.setText(resetTextResId);
			}
			else {
				this.vReset.setVisibility(View.GONE);
			}
		}
	}

	private void decorOkButton() {
		if (this.vOk != null) {
			if (okTextResId > 0) {
				this.vOk.setVisibility(View.VISIBLE);
				this.vOk.setText(this.okTextResId);
			}
			else {
				this.vOk.setVisibility(View.GONE);
			}
		}
	}

	private void showEnterAnimation() {
		if (this.vContent != null) {
			// Jump to end state to complete last animation
			if (this.animator == null) {
				this.animator = ValueAnimator.ofFloat(0.85f, 1f);
			}
			else {
				this.animator.end();
				this.animator.removeAllUpdateListeners();
				this.animator.removeAllListeners();
			}

			this.animUpdater = acquireAnimationUpdater();
			this.animInterpolator = acquireEnterAnimationInterpolator();

			this.animator.setDuration(150);
			this.animator.setInterpolator(this.animInterpolator);
			this.animator.addUpdateListener(anim -> {
				this.animUpdater.run(anim, this.vContent);
			});
			this.animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//					super.onAnimationEnd(animation);
					//					onShowAnimationEnd(dialog);
				}
			});
			this.animator.start();
		}
	}

	private void showExitAnimation() {
		if (animator != null) {
			animUpdater = acquireAnimationUpdater();
			animInterpolator = acquireEnterAnimationInterpolator();

			animator.removeAllListeners();
			animator.removeAllUpdateListeners();
			animator.setDuration(DkAnimationConfiguration.ANIM_LARGE_COLLAPSE_DURATION);
			animator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//						super.onAnimationEnd(animation);
					//						onDismissAnimationEnd(dialog);
				}
			});
			animator.reverse();
		}
	}

	private Interpolator acquireEnterAnimationInterpolator() {
		if (enterAnimationType == ANIM_ZOOM_IN_OUT) {
			if (defaultEnterAnimInterpolator == null) {
				defaultEnterAnimInterpolator = PathInterpolatorCompat.create(
					0.72f, 1.32f,
					0.90f, 1.33f);
			}
		}
		else if (enterAnimationType == ANIM_SWIPE_DOWN) {
			if (defaultEnterAnimInterpolator == null) {
				defaultEnterAnimInterpolator = new DkLookupTableInterpolator(null); // DkInterpolatorProvider.easeElasticOut()
			}
		}
		else {
			throw new RuntimeException("Invalid animType");
		}
		return (animInterpolator = defaultEnterAnimInterpolator);
	}

	private static float[] enterAnimationlookupTable = {
		1f
	};

	private static float[] exitAnimationlookupTable = {
		1f
	};

	private DkRunner2<ValueAnimator, View> acquireAnimationUpdater() {
		if (animUpdater == null) {
			switch (enterAnimationType) {
				case ANIM_ZOOM_IN_OUT: {
					animUpdater = (va, view) -> {
						float t = va.getAnimatedFraction();
						float scaleFactor = (float) va.getAnimatedValue();

						view.setScaleX(scaleFactor);
						view.setScaleY(scaleFactor);
					};
					break;
				}
				case ANIM_SWIPE_DOWN: {
					animUpdater = (va, view) -> {
						view.setY((va.getAnimatedFraction() - 1) * view.getHeight() / 2);
					};
					break;
				}
				default: {
					throw new RuntimeException("Invalid animType");
				}
			}
		}
		return animUpdater;
	}

	// endregion Private

	// onCreate() -> onCreateDialog() -> onCreateView()

	// onViewCreated() -> onViewStateRestored() -> onStart()

	//	@Override
	//	public void onStart() {
	//		if (BuildConfig.DEBUG) {
	//			DkLogs.info(this, "onStart");
	//		}
	//		super.onStart();
	//
	//		// At this time, window is displayed, so we can set size of the dialog
	//		Dialog dialog = getDialog();
	//
	//		if (dialog != null) {
	//			Window window = dialog.getWindow();
	//
	//			if (window != null) {
	//				window.setLayout(MATCH_PARENT, MATCH_PARENT);
	//				window.setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
	//
	//				if (requestInputMethod()) {
	//					window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	//				}
	//			}
	//		}
	//	}


	// onSaveInstanceState() -> onDestroy()

	public DkAlertDialog setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
		return this;
	}

	public boolean isCancelable() {
		return cancelable;
	}

	private static class NonConfigState extends ViewModel {
		private Drawable backgroundDrawable;

		private DkRunner onCancel;
		private DkRunner onReset;
		private DkRunner onOk;
	}
}
