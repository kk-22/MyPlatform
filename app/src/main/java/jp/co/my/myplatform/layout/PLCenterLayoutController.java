package jp.co.my.myplatform.layout;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

public class PLCenterLayoutController extends PLAbstractLayoutController {

	@Override
	public void controlLayout(View view, ViewGroup parentView) {
		// 中央に配置
		Point parentSize = new Point(parentView.getWidth(), parentView.getHeight());
		view.setTranslationX((parentSize.x - view.getWidth()) / 2);
		view.setTranslationY((parentSize.y - view.getHeight()) / 2);
	}
}
