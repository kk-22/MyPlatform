package jp.co.my.myplatform.service.layout;

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
	public void updateLayout(View view, ViewGroup parentView) {
		Point displaySize = new Point(parentView.getWidth(), parentView.getHeight());
		float subSizeX = view.getWidth();
		float subSizeY = view.getHeight();

		// 親ビューの座標取得
		int[] locations = new int[2];
		mRelativeView.getLocationInWindow(locations);
		int parentPointX = locations[0];
		int parentPointY = locations[1];

		if (parentPointX + subSizeX < displaySize.x) {
			// 画面に収まる場合は親のX座標と同じ
			view.setTranslationX(parentPointX);
		} else {
			// 画面右端に寄せる
			view.setTranslationX(displaySize.x - subSizeX);
		}
		if (parentPointY + subSizeY < displaySize.y) {
			view.setTranslationY(parentPointY);
		} else {
			// 収まるように上方向にずらす
			view.setTranslationY(displaySize.y - mRelativeView.getHeight() - subSizeY);
		}
	}
}
