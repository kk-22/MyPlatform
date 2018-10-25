package jp.co.my.myplatform.core;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Map;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLAlarmContent;
import jp.co.my.myplatform.content.PLPushNotificationSettingContent;

public class PLFirebaseMessagingService extends FirebaseMessagingService {

	private static final String FCM_SENDER_ID = "305287558819";

	@SuppressLint("WrongThread")
	@Override
	public void onMessageReceived(RemoteMessage message){
		Map dataMap = message.getData();
		String text = dataMap.get("data").toString();

		Intent intent = new Intent(this, PLCoreService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this,0 , intent,
				PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.added_favorite)
				.setAutoCancel(true)
				.setCategory(Notification.CATEGORY_SERVICE)
				.setContentTitle(text)
				.setVibrate(new long[0]) // Heads-upのために必要
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setVisibility(Notification.VISIBILITY_PUBLIC)
				.setContentIntent(pendingIntent);
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (notificationManager != null) {
			notificationManager.notify(0 , notificationBuilder.build());
		}

		if (PLPushNotificationSettingContent.shouldStartAlarmByPushNotification(text)) {
			// アラームを鳴らす
			Intent serviceIntent = new Intent(this, PLCoreService.class);
			serviceIntent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, PLAlarmContent.class.getCanonicalName());
			startService(serviceIntent);
		}
	}

	public static void outputToken() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String token = FirebaseInstanceId.getInstance().getToken(FCM_SENDER_ID, "FCM");
					MYLogUtil.outputLog("FCM token=" +token);
				} catch (IOException e) {
					MYLogUtil.showExceptionToast(e);
				}
			}
		}).start();
	}
}
