package jp.co.my.myplatform.activity.controller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLDeviceSetting;

public class PLMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setButtonEvent();

		if (enablePermission()) {
			PLApplication.startCoreService();
		}
	}

	private boolean enablePermission() {
		if (Build.VERSION.SDK_INT < 23) {
			return true;
		}

		String settingAction = null;
		if (!Settings.canDrawOverlays(this)) {
			settingAction = Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
		} else if (!Settings.System.canWrite(this)) {
			settingAction = Settings.ACTION_MANAGE_WRITE_SETTINGS;
		}

		if (settingAction== null) {
			// パーミッション設定済み
			return true;
		}
		// パーミッション設定画面表示
		Intent intent = new Intent(settingAction,
				Uri.parse("package:" + getPackageName()));
		startActivityForResult(intent, 1234);
		return false;
	}

	private void setButtonEvent() {
		findViewById(R.id.start_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.startCoreService();
			}
		});
		findViewById(R.id.stop_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.stopCoreService();

				PLDeviceSetting.revertAllSetting();
			}
		});
	}
}
