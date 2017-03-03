package jp.co.my.myplatform.service.mysen.Land;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.mysen.PLMSLandView;

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
		int coverIndex = mParentViewArray.size();
		for (int i = 0; i < landViews.size(); i++) {
			PLMSLandView landView = landViews.get(i);
			View coverView = getCoverView(coverIndex, landView, landViews);
			landView.addView(coverView, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

			mParentViewArray.add(landView);
			coverIndex++;
		}
	}

	public void hideCoverViews() {
		for (int i = 0; i < mParentViewArray.size(); i++) {
			View coverView = mCoverViewArray.get(i);
			MYViewUtil.removeFromSuperView(coverView);
		}
		mParentViewArray.clear();
	}

	public boolean isShowingCover(PLMSLandView landView) {
		return mParentViewArray.contains(landView);
	}

	// getter and setter
	protected ArrayList<PLMSLandView> getParentViewArray() {
		return mParentViewArray;
	}
}
