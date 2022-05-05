/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tool.compet.compactview.databinding.DkDialogPleaseWaitBinding;

/**
 * You can use this to show or close waiting dialog, or extends this to customize behaviors.
 */
public abstract class DkPleaseWaitDialog extends DkCompactFragment<DkDialogPleaseWaitBinding> {
	// Indicate this dialog is dismissable for some actions as: back pressed...
	// Default value is false, so this dialog cannot be dismissed by user's cancel-action
	protected boolean cancelable = false;

	protected String message;
	protected int messageResId = View.NO_ID;
	protected int filterColor = Color.WHITE;

	@Override
	public int layoutResourceId() {
		return R.layout.dk_dialog_please_wait;
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
			this.cancelable = savedInstanceState.getBoolean("DkPleaseWaitDialog.cancelable", false);
			this.messageResId = savedInstanceState.getInt("DkPleaseWaitDialog.messageResId");
			this.message = savedInstanceState.getString("DkPleaseWaitDialog.message");
			this.filterColor = savedInstanceState.getInt("DkPleaseWaitDialog.filterColor");
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		this.storeInstanceState(outState);
	}


	@CallSuper
	protected void storeInstanceState(@NonNull Bundle outState) {
		outState.putBoolean("DkPleaseWaitDialog.cancelable", cancelable);
		outState.putInt("DkPleaseWaitDialog.messageResId", messageResId);
		outState.putString("DkPleaseWaitDialog.message", message);
		outState.putInt("DkPleaseWaitDialog.filterColor", filterColor);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Set message
		if (messageResId != View.NO_ID) {
			message = getString(messageResId);
		}
		setMessage(message);

		// Set color filter for progress
		setColorFilter(filterColor);
	}

	//
	// Get/Set region
	//

	public DkPleaseWaitDialog setCancellable(boolean cancelable) {
		this.cancelable = cancelable;
		return this;
	}

	public DkPleaseWaitDialog setMessage(int messageResId) {
		this.messageResId = messageResId;
		if (binder != null) {
			binder.tvMessage.setText(messageResId);
		}
		return this;
	}

	public DkPleaseWaitDialog setMessage(String message) {
		this.message = message;
		if (binder != null) {
			binder.tvMessage.setText(message);
		}
		return this;
	}

	/**
	 * @param color Set to null to turn off color filter
	 */
	public DkPleaseWaitDialog setColorFilter(int color) {
		this.filterColor = color;
		if (binder != null) {
			binder.pbLoading.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		}
		return this;
	}
}
