package jp.co.my.myplatform.service.core;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import jp.co.my.myplatform.R;

public class PLFirebaseMessagingService extends FirebaseMessagingService {

	@SuppressLint("WrongThread")
	@Override
	public void onMessageReceived(RemoteMessage message){
		Map dataMap = message.getData();
		String text = dataMap.get("data").toString();

		Intent intent = new Intent(this, PLCoreService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , intent,
				PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setAutoCancel(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
				.setContentIntent(pendingIntent);
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager != null) {
			notificationManager.notify(0 , notificationBuilder.build());
		}

		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(2000);
		}

		// 10秒間画面点灯
		PLWakeLockManager.getInstance().incrementKeepScreen();
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				PLWakeLockManager.getInstance().decrementKeepScreen();
			}
		}, 10000);
	}
}
