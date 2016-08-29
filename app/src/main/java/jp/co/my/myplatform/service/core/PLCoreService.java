package jp.co.my.myplatform.service.core;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.activity.controller.PLMainActivity;
import jp.co.my.myplatform.service.app.PLAppStrategy;
import jp.co.my.myplatform.service.app.PLYurudoraApp;
import jp.co.my.myplatform.service.navigation.PLNavigationController;
import jp.co.my.myplatform.service.navigation.PLSetAlarmView;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;

public class PLCoreService extends Service {

	public static final String KEY_CLASS_NAME = "KEY_CLASS_NAME";

	// シングルトン
	private static Context sContext;
	private static PLAppStrategy sAppStrategy;
	private static PLOverlayManager sOverlayManager;
	private static PLNavigationController sNavigationController;

	private boolean mIsRunning;					// 多重起動対策

	@Override
	public void onCreate() {
		MYLogUtil.outputLog("onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MYLogUtil.outputLog("onStartCommand");
		if (!mIsRunning) {
			mIsRunning = true;
			showNotification();

			sContext = this;
			sAppStrategy = new PLYurudoraApp();
			sOverlayManager = new PLOverlayManager(this);
			sNavigationController = new PLNavigationController();

			sOverlayManager.initFrontOverlays();
			sNavigationController.displayNavigationIfNeeded();
		}

		actionIntent(intent);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		MYLogUtil.outputLog("onDestroy");

		sNavigationController.destroyNavigation();
		sOverlayManager.destroyOverlay();

		sNavigationController = null;
		sOverlayManager = null;
		sAppStrategy = null;
		sContext = null;

		mIsRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		MYLogUtil.showToast("onBind");
		return null;
	}

	private void actionIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		String className = intent.getStringExtra(KEY_CLASS_NAME);
		if (className != null) {
			MYLogUtil.outputLog("indent className = " + className);

			if (className.equals(PLSetAlarmView.class.getCanonicalName())) {
				PLSetAlarmView alarmView = PLCoreService.getNavigationController().pushView(PLSetAlarmView.class);
				alarmView.startAlarm();
			}
		}
	}

	private void showNotification() {
		//終了フラグ付きActivity起動通知表示
		Intent intent = new Intent(this.getBaseContext(), PLMainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentIntent(contentIntent);
		NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
		manager.notify(1, builder.build());
		startForeground(1, builder.build());
	}

	public static PLAppStrategy getAppStrategy() {
		return sAppStrategy;
	}

	public static PLOverlayManager getOverlayManager() {
		return sOverlayManager;
	}

	public static PLNavigationController getNavigationController() {
		return sNavigationController;
	}

	public static Context getContext() {
		return sContext;
	}
}
