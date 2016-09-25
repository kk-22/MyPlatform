package jp.co.my.myplatform.service.content;

import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLContentView extends FrameLayout implements View.OnKeyListener {

	private ArrayList<PLPopoverView> mPopoverViews;
	private boolean mIsKeepCache;

	public PLContentView() {
		super(PLCoreService.getContext());
		setFocusable(true);
		setFocusableInTouchMode(true);

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

	public void removePopover(PLPopoverView popoverView) {
		if (mPopoverViews.remove(popoverView)) {
			popoverView.popoverWillRemove();
			removeView(popoverView);
		} else {
			MYLogUtil.showErrorToast("存在しないviewのremovePopover");
		}
	}

	public void addPopover(PLPopoverView popoverView) {
		mPopoverViews.add(popoverView);
		addView(popoverView);
		popoverView.addedPopover(this);
	}

	public boolean isKeepCache() {
		return mIsKeepCache;
	}

	public void setKeepCache(boolean keepCache) {
		mIsKeepCache = keepCache;
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			return onBackKey();
		}
		return false;
	}

	public boolean onBackKey() {
		return false;
	}
}
