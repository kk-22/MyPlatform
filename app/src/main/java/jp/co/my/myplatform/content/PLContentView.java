package jp.co.my.myplatform.content;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;
import jp.co.my.myplatform.popover.PLPopoverView;
import jp.co.my.myplatform.view.PLSavePositionListView;

import static jp.co.my.myplatform.core.PLCoreService.getNavigationController;

public class PLContentView extends FrameLayout implements View.OnKeyListener {

	private ArrayList<PLPopoverView> mPopoverViews;
	private ArrayList<WeakReference<PLSavePositionListView>> mListViews;
	private ViewGroup mNavigationBar;
	private PLNavigationOverlay.BarType mBarType;

	public PLContentView() {
		super(PLCoreService.getContext());
		setFocusable(true);
		setFocusableInTouchMode(true);

		mBarType = PLNavigationOverlay.BarType.BOTTOM;
		mPopoverViews = new ArrayList<>();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mListViews != null) {
			// PLSavePositionListViewクラスの同メソッドでは効かないためここで実行
			for (WeakReference<PLSavePositionListView> listView : mListViews) {
				listView.get().loadPosition();
			}
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mListViews != null) {
			for (WeakReference<PLSavePositionListView> listView : mListViews) {
				listView.get().savePosition();
			}
		}
	}

	public void addListView(PLSavePositionListView listView) {
		if (mListViews == null) {
			mListViews = new ArrayList<>();
		}
		mListViews.add(new WeakReference<>(listView));
	}

	public void viewWillDisappear() {
		if (mListViews != null) {
			// 解放するListViewのスクロール位置は保存しない
			mListViews.clear();
		}
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

	public boolean isCurrentContentView() {
		return equals(getNavigationController().getCurrentView());
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

	// getter and setter
	public ViewGroup getNavigationBar() {
		return mNavigationBar;
	}

	public void setNavigationBar(ViewGroup navigationBar) {
		mNavigationBar = navigationBar;
		if (isCurrentContentView()) {
			getNavigationController().putNavigationBar(mNavigationBar);
		}
	}

	public int getNavigationButtonVisibility() {
		PLNavigationOverlay navigationController = PLCoreService.getNavigationController();
		if (navigationController != null && navigationController.isHalf()) {
			// 戻るボタンを表示できるように常に表示
			return VISIBLE;
		}
		return GONE;
	}

	public ArrayList<PLPopoverView> getPopoverViews() {
		return mPopoverViews;
	}

	public PLNavigationOverlay.BarType getBarType() {
		return mBarType;
	}

	public void setBarType(PLNavigationOverlay.BarType barType) {
		mBarType = barType;
	}

	public int getStatusBarVisibility() {
		return View.VISIBLE;
	}
}
