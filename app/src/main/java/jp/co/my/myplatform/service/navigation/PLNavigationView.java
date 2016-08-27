package jp.co.my.myplatform.service.navigation;

import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;

public class PLNavigationView extends LinearLayout {

	private ArrayList<PLPopoverView> popoverViews;

	public PLNavigationView() {
		super(PLOverlayManager.getInstance().getContext());

		popoverViews = new ArrayList<>();
	}

	public void viewWillDisappear() {
		for (PLPopoverView view : popoverViews) {
			removePopover(view);
		}
		popoverViews.clear();
	}

	protected boolean removeTopPopover() {
		int size = popoverViews.size();
		if (size == 0) {
			return false;
		}
		removePopover(popoverViews.get(size - 1));
		return true;
	}

	protected void removePopover(PLPopoverView view) {
		if (popoverViews.remove(view)) {
			getParentFrameLayout().removeView(view);
		} else {
			MYLogUtil.showErrorToast("存在しないviewのremovePopover");
		}
	}

	protected void addPopover(PLPopoverView view) {
		popoverViews.add(view);
		getParentFrameLayout().addView(view);
	}

	private FrameLayout getParentFrameLayout() {
		ViewParent parentView = getParent();
		return (FrameLayout) parentView;
	}
}
