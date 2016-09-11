package jp.co.my.myplatform.service.content;

import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLContentView extends LinearLayout {

	private ArrayList<PLPopoverView> mPopoverViews;
	private boolean mIsKeepCache;

	public PLContentView() {
		super(PLCoreService.getContext());

		mPopoverViews = new ArrayList<>();
		mIsKeepCache = false;
	}

	public void viewWillDisappear() {
		removeAllViews();
		mPopoverViews.clear();
	}

	public boolean removeTopPopover() {
		int size = mPopoverViews.size();
		if (size == 0) {
			return false;
		}
		removePopover(mPopoverViews.get(size - 1));
		return true;
	}

	public void removePopover(PLPopoverView view) {
		if (mPopoverViews.remove(view)) {
			view.popoverWillRemove();
			getParentFrameLayout().removeView(view);
		} else {
			MYLogUtil.showErrorToast("存在しないviewのremovePopover");
		}
	}

	public void addPopover(PLPopoverView view) {
		mPopoverViews.add(view);
		getParentFrameLayout().addView(view);
		view.addedPopover(this);
	}

	private FrameLayout getParentFrameLayout() {
		ViewParent parentView = getParent();
		return (FrameLayout) parentView;
	}

	public boolean isKeepCache() {
		return mIsKeepCache;
	}

	public void setKeepCache(boolean keepCache) {
		mIsKeepCache = keepCache;
	}
}
