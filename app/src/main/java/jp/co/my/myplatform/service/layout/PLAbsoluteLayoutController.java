package jp.co.my.myplatform.service.layout;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

public class PLAbsoluteLayoutController extends PLAbstractLayoutController {

	int mWidth, mHeight;
	Point mPoint;

	public PLAbsoluteLayoutController(int width, int height, Point point) {
		super();
		mWidth = width;
		mHeight = height;
		mPoint = point;
	}

	@Override
	public void controlLayout(View view, ViewGroup parentView) {
		view.setTranslationX(mPoint.x);
		view.setTranslationY(mPoint.y);
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = mWidth;
		params.height = mHeight;
		view.setLayoutParams(params);
	}
}
