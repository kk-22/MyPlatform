package jp.co.my.myplatform.service.mysen.Land;

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
}