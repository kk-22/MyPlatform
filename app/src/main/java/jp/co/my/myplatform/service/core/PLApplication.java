package jp.co.my.myplatform.service.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import jp.co.my.common.util.MYLogUtil;

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
		sContext.startService(new Intent(sContext, PLCoreService.class));
	}

	public static void stopCoreService() {
		MYLogUtil.outputLog("stopCoreService");
		sContext.stopService(new Intent(sContext, PLCoreService.class));
	}
}
