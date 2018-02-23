package jp.co.my.myplatform.overlay;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.core.PLCoreService;

public class PLPointSelectView extends PLOverlayView {
	public static final String KEY_HORIZONTAL_MARGIN = "KEY_HORIZONTAL_MARGIN";
	public static final String KEY_VERTICAL_MARGIN = "KEY_VERTICAL_MARGIN";

	PLOverlayView mMoveView;

	public PLPointSelectView(PLOverlayView view) {
		super();
		mMoveView = view;
		setLayoutParams(createMatchParams());
		setBackgroundColor(Color.parseColor("#334444FF"));

		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						break;
					case MotionEvent.ACTION_UP:
						performClick();
						didClick(event);
						break;
					default:
						break;
				}
				return false;
			}
		});
		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// キャンセル用
				PLOverlayManager overlayManager = PLCoreService.getOverlayManager();
				overlayManager.removeOverlayView(PLPointSelectView.this);
				return true;
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		// 座標にずれが発生しないように座標選択対象のオーバーレイと同じparamsを返す
		WindowManager.LayoutParams params = getBaseParamsForButtonView();
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		return params;
	}

	private void didClick(MotionEvent event) {
		float positionX = event.getX() - mMoveView.getWidth() / 2;
		float positionY = event.getY() - mMoveView.getHeight() / 2;
		Point screenSize = MYViewUtil.getDisplaySize(getContext());
		WindowManager.LayoutParams params = mMoveView.getOverlayParams();
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.horizontalMargin = positionX / screenSize.x;
		params.verticalMargin = positionY / screenSize.y;

		PLOverlayManager overlayManager = PLCoreService.getOverlayManager();
		overlayManager.updateOverlayLayout(mMoveView, params);
		overlayManager.removeOverlayView(this);

		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putFloat(KEY_HORIZONTAL_MARGIN, params.horizontalMargin);
		editor.putFloat(KEY_VERTICAL_MARGIN, params.verticalMargin);
		editor.commit();
	}
}
