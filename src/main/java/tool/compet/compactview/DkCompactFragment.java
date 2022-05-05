/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelStoreOwner;

import tool.compet.appcompat.DkFragment;
import tool.compet.core.DkLogcats;
import tool.compet.core.DkUtils;
import tool.compet.floatingbar.DkSnackbar;
import tool.compet.floatingbar.DkToastbar;
import tool.compet.navigation.DkFragmentNavigator;
import tool.compet.navigation.DkNavigatorOwner;
import tool.compet.topic.DkTopicManager;
import tool.compet.topic.DkTopicProvider;
import tool.compet.topic.TheTopic;

/**
 * This is basic version which extends from compat-version. Provides more features as possible it can:
 * - [Optional] Navigator (we can forward, backward, dismiss... page easily)
 * - [Optional] Scoped topic (pass data between/under fragments, activities, app)
 * - [Optional] Message display (snack, toast...)
 *
 * @param <B> ViewDataBinding
 */
public abstract class DkCompactFragment<B extends ViewDataBinding>
	extends DkFragment<B>
	implements DkNavigatorOwner, DkTopicProvider {

	// Child navigator
	protected DkFragmentNavigator childNavigator;

	@Override // onViewCreated() -> onViewStateRestored() -> onStart()
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		if (childNavigator != null) {
			childNavigator.restoreInstanceState(savedInstanceState);
		}
		super.onViewStateRestored(savedInstanceState);
	}

	@Override // called before onDestroy()
	public void onSaveInstanceState(@NonNull Bundle outState) {
		if (childNavigator != null) {
			childNavigator.storeInstanceState(outState);
		}
		super.onSaveInstanceState(outState);
	}

	/**
	 * Called when user pressed to physical back button, this is normally passed from current activity.
	 * When this view got an event, this send signal to children first, if no child was found,
	 * then this will call `close()` on it to dismiss itself.
	 *
	 * @return true if this view or child of it has dismissed successfully, otherwise false.
	 */
	@Override
	public boolean onBackPressed() {
		if (childNavigator == null || childNavigator.childCount() == 0) {
			return this.close();
		}
		return childNavigator.handleOnBackPressed();
	}

	/**
	 * Open dialog via parent navigator.
	 */
	public boolean open(DkFragmentNavigator navigator) {
		return navigator.beginTransaction().add(this).commit();
	}

	/**
	 * Open dialog via parent navigator.
	 */
	public boolean open(DkFragmentNavigator navigator, int enterAnimRes, int exitAnimRes) {
		return navigator.beginTransaction().setAnims(enterAnimRes, exitAnimRes).add(this).commit();
	}

	/**
	 * Close this view by tell parent navigator remove this.
	 */
	@Override // from `DkFragment`
	public boolean close() {
		try {
			// Multiple times of calling `getParentNavigator()` maybe cause exception
			return getParentNavigator().beginTransaction().remove(this).commit();
		}
		catch (Exception e) {
			DkLogcats.error(this, e);
			return false;
		}
	}

	// region Navigator

	/**
	 * Must provide id of fragent container via `fragmentContainerId()`.
	 */
	@Override // from `DkNavigatorOwner`
	public DkFragmentNavigator getChildNavigator() {
		if (childNavigator == null) {
			int containerId = fragmentContainerId();

			if (containerId <= 0) {
				DkUtils.complainAt(this, "Must provide `fragmentContainerId()`");
			}

			childNavigator = new DkFragmentNavigator(containerId, getChildFragmentManager());
		}
		return childNavigator;
	}

	@Override // from `DkNavigatorOwner`
	public DkFragmentNavigator getParentNavigator() {
		Fragment parent = getParentFragment();
		DkFragmentNavigator parentNavigator = null;

		if (parent == null) {
			if (host instanceof DkNavigatorOwner) {
				parentNavigator = ((DkNavigatorOwner) host).getChildNavigator();
			}
		}
		else if (parent instanceof DkNavigatorOwner) {
			parentNavigator = ((DkNavigatorOwner) parent).getChildNavigator();
		}

		if (parentNavigator == null) {
			DkUtils.complainAt(this, "Must have a parent navigator own this fragment `%s`", getClass().getName());
		}

		return parentNavigator;
	}

	// endregion Navigator

	// region Scoped topic

	/**
	 * Obtain and Join to topic under `host` scope.
	 *
	 * @param topicType Topic type in the scope, for eg,. PromotionTopic.class,...
	 */
	@Override
	public <T extends TheTopic<?>> T topic(String topicId, Class<T> topicType) {
		return new DkTopicManager<>(host, topicId, topicType).registerClient(this, false);
	}

	/**
	 * Obtain and Join to topic inside given scope.
	 *
	 * @param topicType Topic type in the scope, for eg,. PromotionTopic.class,...
	 * @param scope Where to host the topic. For eg,. `app`, `host`, `this`,...
	 */
	@Override
	public <T extends TheTopic<?>> T topic(String topicId, Class<T> topicType, ViewModelStoreOwner scope) {
		return new DkTopicManager<>(scope, topicId, topicType).registerClient(this, false);
	}

	// endregion Scoped topic

	// region Utility

	public DkSnackbar snackbar() {
		return DkSnackbar.newIns(layout);
	}

	public DkToastbar toastbar() {
		return DkToastbar.newIns(layout);
	}

	// endregion Utility
}
