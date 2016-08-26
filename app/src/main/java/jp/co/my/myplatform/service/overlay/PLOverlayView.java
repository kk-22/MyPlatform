package jp.co.my.myplatform.service.overlay;

import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.LinearLayout;

public abstract class PLOverlayView extends LinearLayout {

	public PLOverlayView() {
		super(PLOverlayManager.getInstance().getContext());
	}

	public void viewWillRemove() {
		// remove前に必要な処理をoverrideして実装
	}

	abstract public WindowManager.LayoutParams getOverlayParams();

	protected LayoutParams createMatchParams() {
		return new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
	}

	protected WindowManager.LayoutParams getBaseParamsForButtonView() {
		return new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,					// フォーカスされない
				PixelFormat.TRANSLUCENT											// ウィンドウの透明化
		);
	}

	protected WindowManager.LayoutParams getBaseParamsForDataView() {
		return new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,		// タッチイベントを拾わない
				PixelFormat.TRANSLUCENT									// ウィンドウの透明化
		);
	}

	protected WindowManager.LayoutParams getBaseParamsForFullView() {
		return new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL					// タッチイベントを拾わない
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,		// 画面全体に配置
				PixelFormat.TRANSLUCENT											//  ウィンドウの透明化
		);
	}
}
