package jp.co.my.myplatform.mysen.land;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.mysen.PLMSLandView;

abstract class PLMSAbstractCover {

	private ArrayList<View> mCoverViewArray;
	private ArrayList<PLMSLandView> mParentViewArray;

	public PLMSAbstractCover() {
		mCoverViewArray = new ArrayList<>();
		mParentViewArray = new ArrayList<>();
	}

	protected Context getContext() {
		return PLCoreService.getContext();
	}

	protected abstract View createCoverView();

	protected View getCoverView(int index, PLMSLandView currentLandView, ArrayList<PLMSLandView> landViews) {
		if (index < mCoverViewArray.size()) {
			return mCoverViewArray.get(index);
		} else {
			View view = createCoverView();
			mCoverViewArray.add(view);
			return view;
		}
	}

	public void showCoverViews(ArrayList<PLMSLandView> landViews) {
		if (landViews == null) {
			return;
		}
		int coverIndex = mParentViewArray.size();
		for (int i = 0; i < landViews.size(); i++) {
			PLMSLandView landView = landViews.get(i);
			showCoverView(coverIndex, landView, landViews);
			coverIndex++;
		}
	}

	public void showCoverView(PLMSLandView landView) {
		// 未使用 view が見つからなければ新規 view を作成するために index に初期値をセット
		int index = mCoverViewArray.size();
		for (int i = 0; i < mCoverViewArray.size(); i++) {
			View view = mCoverViewArray.get(i);
			if (view.getParent() == null) {
				index = i;
				break;
			}
		}
		showCoverView(index, landView, null);
	}

	private void showCoverView(int index, PLMSLandView landView, ArrayList<PLMSLandView> landViews) {
		View coverView = getCoverView(index, landView, landViews);
		landView.addView(coverView, new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mParentViewArray.add(landView);
	}

	public int hideCoverView(PLMSLandView targetView) {
		if (!mParentViewArray.remove(targetView)) {
			return -1;
		}
		int coverSize = mCoverViewArray.size();
		for (int i = 0; i < coverSize; i++) {
			View coverView = mCoverViewArray.get(i);
			if (coverView.getParent() == targetView) {
				targetView.removeView(coverView);
				return i;
			}
		}
		return -1;
	}

	public void hideAllCoverViews() {
		for (View coverView : mCoverViewArray) {
			MYViewUtil.removeFromSuperView(coverView);
		}
		mParentViewArray.clear();
	}

	public boolean isShowingCover(PLMSLandView landView) {
		return mParentViewArray.contains(landView);
	}

	// coverView の付け替え
	public void changeCover(PLMSLandView from, PLMSLandView to) {
		int index = hideCoverView(from);
		if (index == -1) {
			// 表示されていなかった
			return;
		}
		showCoverView(index, to, null);
	}

	// getter and setter
	protected ArrayList<PLMSLandView> getParentViewArray() {
		return mParentViewArray;
	}

	protected ArrayList<View> getCoverViewArray() {
		return mCoverViewArray;
	}
}
