/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.compactview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelStoreOwner;

import tool.compet.appcompat.DkActivity;
import tool.compet.core.DkUtils;
import tool.compet.floatingbar.DkSnackbar;
import tool.compet.floatingbar.DkToastbar;
import tool.compet.navigation.DkFragmentNavigator;
import tool.compet.navigation.DkNavigatorOwner;
import tool.compet.topic.DkTopicManager;
import tool.compet.topic.DkTopicProvider;
import tool.compet.topic.TheTopic;

/**
 * This is compact version which extends from compat-version. Provides more features as possible it can:
 * - [Optional] Navigator (we can forward, backward, dismiss... page easily)
 * - [Optional] Scoped topic (pass data between/under fragments, activities, app)
 * - [Optional] Message display (snack, toast...)
 *
 * @param <B> ViewDataBinding
 */
public abstract class DkCompactActivity<B extends ViewDataBinding>
	extends DkActivity<B>
	implements DkNavigatorOwner, DkTopicProvider {

	// Child navigator
	protected DkFragmentNavigator childNavigator;

	@Override // onPostCreate() -> onRestoreInstanceState() -> onStart()
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		if (childNavigator != null) {
			childNavigator.restoreInstanceState(savedInstanceState);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override // maybe called before onStop() or onDestroy()
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (childNavigator != null) {
			childNavigator.storeInstanceState(outState);
		}
		super.onSaveInstanceState(outState);
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

			childNavigator = new DkFragmentNavigator(containerId, getSupportFragmentManager());
		}

		return childNavigator;
	}

	@Override // from `DkNavigatorOwner`
	public DkFragmentNavigator getParentNavigator() {
		throw new RuntimeException("By default, activity does not provide parent navigator");
	}

	// endregion Navigator

	// region Scoped topic

	/**
	 * Obtain and Join to topic inside `this` scope.
	 *
	 * @param topicId Unique id of this topic under the `host`.
	 * @param topicType Topic type in the scope, for eg,. PromotionTopic.class,...
	 */
	@Override
	public <T extends TheTopic<?>> T topic(String topicId, Class<T> topicType) {
		return new DkTopicManager<>(this, topicId, topicType).registerClient(this, false);
	}

	/**
	 * Obtain and Join to topic inside given scope.
	 *
	 * @param topicId Unique id of this topic under the `host`.
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
