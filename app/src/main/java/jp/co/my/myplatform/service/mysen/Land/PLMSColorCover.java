package jp.co.my.myplatform.service.mysen.Land;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static android.view.View.VISIBLE;

public class PLMSColorCover implements PLMSCoverInterface {

	private View mCoverView;
	private ViewGroup mTargetView;
	private int mCoverColor;

	public PLMSColorCover(ViewGroup targetView, int coverColor) {
		mTargetView = targetView;
		mCoverColor = coverColor;
	}

	@Override
	public void showCoverView() {
		if (mCoverView == null) {
			mCoverView = new View(mTargetView.getContext());
			mCoverView.setBackgroundColor(mCoverColor);
			mTargetView.addView(mCoverView, new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		}
		mCoverView.setVisibility(VISIBLE);
	}

	@Override
	public void hideCoverView() {
		if (mCoverView == null) {
			return;
		}
		mCoverView.setVisibility(View.GONE);
	}

	@Override
	public boolean isShowingCover() {
		return (mCoverView != null && mCoverView.getVisibility() == View.VISIBLE);
	}
}
