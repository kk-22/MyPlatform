package jp.co.my.myplatform.service.mysen.Land;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import jp.co.my.myplatform.service.mysen.PLMSLandView;

import static android.view.View.VISIBLE;

public class PLLandCover {

	private View mCoverView;
	private PLMSLandView mLandView;
	private int mCoverColor;

	public PLLandCover(PLMSLandView landView, int coverColor) {
		mLandView = landView;
		mCoverColor = coverColor;
	}

	public void showCoverView() {
		if (mCoverView == null) {
			mCoverView = new View(mLandView.getContext());
			mCoverView.setBackgroundColor(mCoverColor);
			mLandView.addView(mCoverView, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		}
		mCoverView.setVisibility(VISIBLE);
	}

	public void hideCoverView() {
		if (mCoverView == null) {
			return;
		}
		mCoverView.setVisibility(View.GONE);
	}

	public boolean isShowingMoveArea() {
		return (mCoverView != null && mCoverView.getVisibility() == View.VISIBLE);
	}
}
