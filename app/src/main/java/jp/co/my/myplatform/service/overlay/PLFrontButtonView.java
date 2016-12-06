package jp.co.my.myplatform.service.overlay;

import android.content.ClipData;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLFrontButtonView extends PLOverlayView {

	protected Button mButton;
	protected Class mTextClass;

	public PLFrontButtonView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_front_button, this);

		mButton = (Button) findViewById(R.id.front_button);
		mButton.setLayoutParams(createMatchParams());
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().displayNavigationIfNeeded();
			}
		});
		mButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// ドラッグ&ドロップで受け渡しするデータ(使わないのでダミー)
				ClipData tmpData = ClipData.newPlainText("dummy", "dummy");
				View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
				v.startDrag(tmpData, shadow, v, 0);

				PLOverlayManager overlayManager = PLCoreService.getOverlayManager();
				if (overlayManager.getOverlayView(PLDragDropView.class) == null) {
					overlayManager.addOverlayView(new PLDragDropView(PLFrontButtonView.this));
				}
				return true;
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		SharedPreferences pref = MYLogUtil.getPreference();
		Float horizontal = pref.getFloat(PLDragDropView.KEY_HORIZONTAL_MARGIN, 0);
		Float vertical = pref.getFloat(PLDragDropView.KEY_VERTICAL_MARGIN, 0);

		WindowManager.LayoutParams params = getBaseParamsForButtonView();
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.horizontalMargin = horizontal;
		params.verticalMargin = vertical;
		params.alpha = 0.5f;
		return params;
	}

	public void setText(Class klass, int fontSize, String text) {
		mTextClass = klass;
		mButton.setTextSize(fontSize);
		mButton.setText(text);
	}

	public boolean clearText(Class klass) {
		if (!klass.equals(mTextClass)) {
			return false;
		}
		mTextClass = null;
		mButton.setText("");
		return true;
	}
}
