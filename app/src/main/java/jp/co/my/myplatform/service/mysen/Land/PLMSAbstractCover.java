package jp.co.my.myplatform.service.mysen.Land;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.myplatform.service.mysen.PLMSLandView;

abstract class PLMSAbstractCover {

	private ArrayList<View> mCoverViewArray;
	private ArrayList<PLMSLandView> mParentViewArray;

	public PLMSAbstractCover() {
		mCoverViewArray = new ArrayList<>();
		mParentViewArray = new ArrayList<>();
	}

	protected abstract View createCoverView(Context context);

	public void showCoverViews(ArrayList<PLMSLandView> landViews) {
		int numberOfViews = mCoverViewArray.size();
		int coverIndex = mParentViewArray.size();
		for (int i = 0; i < landViews.size(); i++) {
			PLMSLandView landView = landViews.get(i);
			View coverView;
			if (coverIndex < numberOfViews) {
				coverView = mCoverViewArray.get(coverIndex);
			} else {
				coverView = createCoverView(landView.getContext());
				mCoverViewArray.add(coverView);
			}
			landView.addView(coverView, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

			mParentViewArray.add(landView);
			coverIndex++;
		}
	}

	public void hideCoverViews() {
		for (int i = 0; i < mParentViewArray.size(); i++) {
			View coverView = mCoverViewArray.get(i);
			ViewGroup parentView = (ViewGroup) coverView.getParent();
			parentView.removeView(coverView);
		}
		mParentViewArray.clear();
	}

	public boolean isShowingCover(PLMSLandView landView) {
		return mParentViewArray.contains(landView);
	}
}
