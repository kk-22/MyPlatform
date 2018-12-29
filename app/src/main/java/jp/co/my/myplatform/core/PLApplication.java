package jp.co.my.myplatform.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.activity.PLMainActivity;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;

public class PLApplication extends Application {

	private static final String TWITTER_KEY = "dummy";
    private static final String TWITTER_SECRET = "dummy";

	private static Context sContext;

	@Override
	public void onCreate() {
		super.onCreate();

		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig));

		sContext = getApplicationContext();

		MYLogUtil.initLogUtil(sContext, PLDevelopmentUtil.isWriteLog());
		FlowManager.init(new FlowConfig.Builder(this).build());
	}

	public static Context getContext() {
		return sContext;
	}

	public static void startCoreService() {
		MYLogUtil.outputLog("startCoreService");
		Intent intent = new Intent(sContext, PLCoreService.class);
		intent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, PLNavigationOverlay.class.getCanonicalName());
		sContext.startService(intent);
	}

	public static void stopCoreService() {
		MYLogUtil.outputLog("stopCoreService");
		sContext.stopService(new Intent(sContext, PLCoreService.class));
	}

	public static void startMainActivity() {
		Intent intent = new Intent();
		intent.setClassName(sContext.getPackageName(), "jp.co.my.myplatform.activity.PLMainActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PLMainActivity.KEY_DO_NOT_START_SERVICE, true);
		sContext.startActivity(intent);
	}

	public static String appRootPath() {
		return Environment.getExternalStorageDirectory() + "/MyPlatform/";
	}
}
