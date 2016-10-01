package jp.co.my.myplatform.service.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.activity.controller.PLMainActivity;
import jp.co.my.myplatform.service.overlay.PLNavigationController;

public class PLApplication extends Application {

	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();

		MYLogUtil.initLogUtil(sContext, false);
		FlowManager.init(new FlowConfig.Builder(this).build());
	}

	public static Context getContext() {
		return sContext;
	}

	public static void startCoreService() {
		MYLogUtil.outputLog("startCoreService");
		Intent intent = new Intent(sContext, PLCoreService.class);
		intent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, PLNavigationController.class.getCanonicalName());
		sContext.startService(intent);
	}

	public static void stopCoreService() {
		MYLogUtil.outputLog("stopCoreService");
		sContext.stopService(new Intent(sContext, PLCoreService.class));
	}

	public static void startMainActivity() {
		Intent intent = new Intent();
		intent.setClassName(sContext.getPackageName(), "jp.co.my.myplatform.activity.controller.PLMainActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PLMainActivity.KEY_DO_NOT_START_SERVICE, true);
		sContext.startActivity(intent);
	}

	public static String appRootPath() {
		return Environment.getExternalStorageDirectory() + "/MyPlatform/";
	}
}
