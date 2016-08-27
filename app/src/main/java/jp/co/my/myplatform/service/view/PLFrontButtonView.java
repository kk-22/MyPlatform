package jp.co.my.myplatform.service.view;

import android.content.ClipData;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.overlay.PLDragDropView;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;
import jp.co.my.myplatform.service.overlay.PLOverlayView;

public class PLFrontButtonView extends PLOverlayView {

	protected Button mButton;

	public PLFrontButtonView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_front_button, this);

		mButton = (Button) findViewById(R.id.button);
		mButton.setLayoutParams(createMatchParams());
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLOverlayManager.getInstance().displayNavigationView(null);
			}
		});
		mButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// ドラッグ&ドロップで受け渡しするデータ(使わないのでダミー)
				ClipData tmpData = ClipData.newPlainText("dummy", "dummy");
				View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
				v.startDrag(tmpData, shadow, v, 0);

				PLOverlayManager overlayManager = PLOverlayManager.getInstance();
				if (overlayManager.getOverlayView(PLDragDropView.class) == null) {
					overlayManager.addOverlayView(new PLDragDropView(PLFrontButtonView.this));
				}
				return true;
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		WindowManager.LayoutParams params = getBaseParamsForButtonView();
		params.gravity = Gravity.TOP | Gravity.RIGHT;
		params.verticalMargin = 0.4f;
		params.alpha = 0.5f;
		return params;
	}
}