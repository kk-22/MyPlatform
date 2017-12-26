package jp.co.my.myplatform.overlay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Switch;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLDeviceSetting;

public class PLLockView extends PLOverlayView {

	public PLLockView() {
		super();

		View view = LayoutInflater.from(getContext()).inflate(R.layout.overlay_lock_view, this);
		final Switch switch1 = (Switch) view.findViewById(R.id.switch1);
		final Switch switch2 = (Switch) view.findViewById(R.id.switch2);
		view.findViewById(R.id.open_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!switch1.isChecked() || !switch2.isChecked()) {
					switch1.setChecked(false);
					switch2.setChecked(false);
					return;
				}
				PLCoreService.getOverlayManager().removeOverlayView(PLLockView.this);
			}
		});

		// ディスプレイを暗くする
		PLDeviceSetting.setMinScreenBrightness();
	}

	@Override
	public void viewWillRemove() {
		// 画面ロック時に変えた設定を元に戻す
		PLDeviceSetting.revertScreenBrightness();
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		WindowManager.LayoutParams params = getBaseParamsForFullView();
		return params;
	}
}