package jp.co.my.myplatform.service.overlay;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLDragDropView extends PLOverlayView {
	public static final String KEY_HORIZONTAL_MARGIN = "KEY_HORIZONTAL_MARGIN";
	public static final String KEY_VERTICAL_MARGIN = "KEY_VERTICAL_MARGIN";

	PLOverlayView mMoveView;

	public PLDragDropView(PLOverlayView view) {
		super();
		mMoveView = view;

		setLayoutParams(createMatchParams());
		setBackgroundColor(Color.parseColor("#334444FF"));
		setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(View v, DragEvent event) {
				switch (event.getAction())	{
					case DragEvent.ACTION_DRAG_STARTED:
					case DragEvent.ACTION_DRAG_ENTERED:
					case DragEvent.ACTION_DRAG_LOCATION:
					case DragEvent.ACTION_DRAG_EXITED:
						return true;
					case DragEvent.ACTION_DROP: {
						finishDragEvent(event);
						return true;
					}
					default:
						break;
				}
				return false;
			}
		});
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ドラッグ失敗時の解除用
				PLOverlayManager overlayManager = PLCoreService.getOverlayManager();
				overlayManager.removeOverlayView(PLDragDropView.this);
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		return getBaseParamsForFullView();
	}

	private void finishDragEvent(DragEvent event) {
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
