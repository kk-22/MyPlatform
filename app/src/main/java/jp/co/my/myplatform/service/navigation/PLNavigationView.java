package jp.co.my.myplatform.service.navigation;

import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLNavigationView extends LinearLayout {

	private ArrayList<PLPopoverView> popoverViews;
	protected boolean isKeepCache;

	public PLNavigationView() {
		super(PLCoreService.getContext());

		popoverViews = new ArrayList<>();
		isKeepCache = false;
	}

	public void viewWillDisappear() {
		removeAllViews();
		popoverViews.clear();
	}

	public boolean removeTopPopover() {
		int size = popoverViews.size();
		if (size == 0) {
			return false;
		}
		removePopover(popoverViews.get(size - 1));
		return true;
	}

	public void removePopover(PLPopoverView view) {
		if (popoverViews.remove(view)) {
			getParentFrameLayout().removeView(view);
		} else {
			MYLogUtil.showErrorToast("存在しないviewのremovePopover");
		}
	}

	public void addPopover(PLPopoverView view) {
		popoverViews.add(view);
		getParentFrameLayout().addView(view);
		view.addedPopover(this);
	}

	private FrameLayout getParentFrameLayout() {
		ViewParent parentView = getParent();
		return (FrameLayout) parentView;
	}
}
