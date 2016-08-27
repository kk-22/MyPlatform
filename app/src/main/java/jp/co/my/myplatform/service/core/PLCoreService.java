package jp.co.my.myplatform.service.core;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.activity.controller.PLMainActivity;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;

public class PLCoreService extends Service {

	private boolean mIsRunning;					// 多重起動対策

	@Override
	public void onCreate() {
		MYLogUtil.outputLog("onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mIsRunning) {
			MYLogUtil.showToast("既にServiceが開始しています");
			return START_STICKY;
		}
		mIsRunning = true;
		MYLogUtil.outputLog("onStartCommand");

		showNotification();
		PLOverlayManager.init(this);
		PLOverlayManager.getInstance().displayNavigationView(null);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		MYLogUtil.outputLog("onDestroy");
		PLServiceController.getInstance().destroyCoreService();
		mIsRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		MYLogUtil.showToast("onBind");
		return null;
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
}
