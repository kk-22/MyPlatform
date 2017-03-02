package jp.co.my.myplatform.service.mysen.Land;

import android.content.Context;
import android.view.View;

public class PLMSColorCover extends PLMSAbstractCover {

	private int mCoverColor;

	public PLMSColorCover(int coverColor) {
		super();
		mCoverColor = coverColor;
	}

	@Override
	protected View createCoverView(Context context) {
		View view = new View(context);
		view.setBackgroundColor(mCoverColor);
		return view;
	}
}
