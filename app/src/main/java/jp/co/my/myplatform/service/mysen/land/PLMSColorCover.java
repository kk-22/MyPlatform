package jp.co.my.myplatform.service.mysen.land;

import android.view.View;

public class PLMSColorCover extends PLMSAbstractCover {

	private int mCoverColor;

	public PLMSColorCover(int coverColor) {
		super();
		mCoverColor = coverColor;
	}

	@Override
	protected View createCoverView() {
		View view = new View(getContext());
		view.setBackgroundColor(mCoverColor);
		return view;
	}

	public void changeColor(int nextColor) {
		if (mCoverColor == nextColor) {
			return;
		}
		mCoverColor = nextColor;
		for (View view : getCoverViewArray()) {
			view.setBackgroundColor(nextColor);
		}
	}
}
