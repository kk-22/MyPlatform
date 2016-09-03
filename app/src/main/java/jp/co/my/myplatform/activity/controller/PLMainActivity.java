package jp.co.my.myplatform.activity.controller;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
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
		startServiceIfEnable();
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

	public boolean canGetUsage(Context context) {
		AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
		int mode = appOpsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
				context.getPackageName());
		if (mode != AppOpsManager.MODE_DEFAULT) {
			return mode == AppOpsManager.MODE_ALLOWED;
		}
		// AppOpsの状態がデフォルトなら通常のpermissionチェックを行う。
		return context.checkPermission("android.permission.PACKAGE_USAGE_STATS",
				Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
	}

	private void startServiceIfEnable() {
		if (!canGetUsage(this)) {
			startActivity(new Intent("android.settings.USAGE_ACCESS_SETTINGS"));
		} else if (enablePermission()) {
			PLApplication.startCoreService();
		}
	}

	private void setButtonEvent() {
		findViewById(R.id.start_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startServiceIfEnable();
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
