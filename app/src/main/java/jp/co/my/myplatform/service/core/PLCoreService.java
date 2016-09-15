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
import jp.co.my.myplatform.service.app.PLAppStrategy;
import jp.co.my.myplatform.service.content.PLSetAlarmView;
import jp.co.my.myplatform.service.overlay.PLNavigationController;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;

public class PLCoreService extends Service {

	public static final String KEY_INTENT_FROM_BROADCAST = "KEY_INTENT_FROM_BROADCAST";
	public static final String KEY_CONTENT_CLASS_NAME = "KEY_CONTENT_CLASS_NAME";
	public static final String KEY_ACTION_SHOW = "KEY_ACTION_SHOW";

	// シングルトン
	private static Context sContext;
	private static PLVolleyHelper sVolleyHelper;
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
			sVolleyHelper = new PLVolleyHelper(this);
			sAppStrategy = new PLAppStrategy();
			sOverlayManager = new PLOverlayManager(this);
			sOverlayManager.addFrontOverlays();
			sNavigationController = new PLNavigationController();
		}

		if (intent != null) {
			actionIntent(intent);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		MYLogUtil.outputLog("onDestroy");

		sVolleyHelper.destroyRequest();
		sNavigationController.destroyNavigation();
		sOverlayManager.destroyOverlay();

		sVolleyHelper = null;
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

		String className = intent.getStringExtra(KEY_CONTENT_CLASS_NAME);
		if (className != null) {
			MYLogUtil.outputLog("indent className = " + className);
			sOverlayManager.addFrontOverlays();
			sNavigationController.displayNavigationIfNeeded();
			if (className.equals(PLNavigationController.class.getCanonicalName())) {
				// ナビゲーション表示のみ
			} else if (className.equals(PLSetAlarmView.class.getCanonicalName())) {
				PLSetAlarmView alarmView = PLCoreService.getNavigationController().pushView(PLSetAlarmView.class);
				alarmView.startAlarm();
			}
		}

		if (intent.getBooleanExtra(KEY_ACTION_SHOW, false)) {
			sOverlayManager.addFrontOverlays();
			sNavigationController.displayNavigationIfNeeded();
		}

		if (intent.getBooleanExtra(KEY_INTENT_FROM_BROADCAST, false)) {
			// PLBroadcastReceiver で設定した分を解除
			PLWakeLockManager.getInstance().decrementKeepCPU();
		}
	}

	private void showNotification() {
		// ボタン表示アクションつき通知
		Context context = getApplicationContext();
		Intent intent = new Intent(context, PLCoreService.class);
		intent.putExtra(PLCoreService.KEY_ACTION_SHOW, true);
		PendingIntent pendingIntent = PendingIntent.getService(context, 66, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentIntent(pendingIntent);
		NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
		manager.notify(1, builder.build());
		startForeground(1, builder.build());
	}

	public static PLVolleyHelper getVolleyHelper() {
		return sVolleyHelper;
	}

	public static PLAppStrategy getAppStrategy() {
		return sAppStrategy;
	}

	public static void setAppStrategy(PLAppStrategy appStrategy) {
		sAppStrategy = appStrategy;
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
