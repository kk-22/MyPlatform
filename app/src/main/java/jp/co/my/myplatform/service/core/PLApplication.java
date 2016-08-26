package jp.co.my.myplatform.service.core;

import android.app.Application;
import android.content.Context;

import jp.co.my.common.util.MYLogUtil;

public class PLApplication extends Application {

	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();

		MYLogUtil.initLogUtil(sContext, false);
	}

	public static Context getContext() {
		return sContext;
	}
}
