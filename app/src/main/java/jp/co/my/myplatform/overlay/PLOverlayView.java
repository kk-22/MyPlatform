package jp.co.my.myplatform.overlay;

import android.graphics.PixelFormat;
import android.view.WindowManager;
import android.widget.LinearLayout;

import jp.co.my.myplatform.core.PLCoreService;

public abstract class PLOverlayView extends LinearLayout {

	public PLOverlayView() {
		super(PLCoreService.getContext());
	}

	public void viewWillRemove() {
		// remove前に必要な処理をoverrideして実装
	}

	protected void updateLayout() {
		PLCoreService.getOverlayManager().updateOverlayLayout(this, getOverlayParams());
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
				WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,					// フォーカスされない
				PixelFormat.TRANSLUCENT											// ウィンドウの透明化
		);
	}

	protected WindowManager.LayoutParams getBaseParamsForNavigationView() {
		return new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL					// タッチイベントを拾わない
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN		// ステータスバーの上まで表示１
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS		// ステータスバーの上まで表示２
				,
				PixelFormat.TRANSLUCENT											//  ウィンドウの透明化
		);
	}
	protected WindowManager.LayoutParams getBaseParamsForFullView() {
		return new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
				WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS				// PLDragDropViewで画面外へフリック時に必要
						| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR	// FLAG_LAYOUT_IN_SCREENで必要
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,		// 画面全体に配置
				PixelFormat.TRANSLUCENT											//  ウィンドウの透明化
		);
	}
}
