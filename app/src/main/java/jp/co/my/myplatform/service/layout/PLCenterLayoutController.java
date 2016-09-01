package jp.co.my.myplatform.service.layout;

import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;

public class PLCenterLayoutController extends PLAbstractLayoutController {

	@Override
	public void updateLayout(View view, ViewGroup parentView) {
		// 中央に配置
		Point displaySize = new Point(parentView.getWidth(), parentView.getHeight());
		view.setTranslationX((displaySize.x - view.getWidth()) / 2);
		view.setTranslationY((displaySize.y - view.getHeight()) / 2);
	}
}
