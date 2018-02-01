package jp.co.my.myplatform.layout;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

public class PLRelativeLayoutController extends PLAbstractLayoutController {

	private View mRelativeView;

	public PLRelativeLayoutController(View relativeView) {
		super();
		mRelativeView = relativeView;
	}

	@Override
	public void controlLayout(View view, ViewGroup parentView) {
		// 相対ビューの座標取得
		int[] locations = new int[2];
		mRelativeView.getLocationInWindow(locations);
		int relativePointX = locations[0];
		int relativePointY = locations[1];

		Point parentSize = new Point(parentView.getWidth(), parentView.getHeight());
		float viewSizeX = view.getWidth();
		float viewSizeY = view.getHeight();

		if (relativePointX + viewSizeX < parentSize.x) {
			// 画面に収まる場合は親のX座標と同じ
			view.setTranslationX(relativePointX);
		} else {
			// 画面右端に寄せる
			view.setTranslationX(parentSize.x - viewSizeX);
		}
		if (relativePointY + viewSizeY < parentSize.y) {
			view.setTranslationY(relativePointY);
		} else {
			// 収まるように上方向にずらす
			float tempY = parentSize.y - mRelativeView.getHeight() - viewSizeY;
			if (tempY < 0) {
				// 上方向にはみ出す分だけ高さを小さくする
				ViewGroup.LayoutParams params = view.getLayoutParams();
				params.height = (int)(viewSizeY + tempY);
				view.setLayoutParams(params);
				tempY = 0;
			}
			view.setTranslationY(tempY);
		}
	}
}
