package jp.co.my.myplatform.content;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
	private PLNavigationOverlay.BarType mBarType;
	private ArrayList<Button> mNavigationButtons;

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

	// 遷移先画面から戻ってきたときに呼ばれる
	public void viewWillComeBack(PLContentView from) {

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

	public boolean canGoBackContent() {
		return true;
	}

	public Button addNavigationButton(String title, OnClickListener onClickListener) {
		return addNavigationButton(title, true, onClickListener);
	}
	public Button addNavigationButton(String title, boolean enabled, OnClickListener onClickListener) {
		Button button = new Button(getContext());
		button.setText(title);
		button.setEnabled(enabled);
		button.setOnClickListener(onClickListener);

		if (mNavigationButtons == null) {
			mNavigationButtons = new ArrayList<>();
		}
		mNavigationButtons.add(button);

		if (isCurrentContentView()) {
			getNavigationController().getNavigationBar().showButton(button);
		}
		return button;
	}

	// getter and setter
	public ArrayList<Button> getNavigationButtons() {
		return mNavigationButtons;
	}

	public int getNavigationButtonVisibility() {
		PLNavigationOverlay navigationController = getNavigationController();
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
