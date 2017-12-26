package jp.co.my.myplatform.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jp.co.my.common.util.MYLogUtil;

public class PLBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// onReceive直後のスリープを防ぐ
		PLWakeLockManager.getInstance().incrementKeepCPU();

		MYLogUtil.outputLog("PLBroadcastReceiver onReceive");
		Intent serviceIntent = new Intent(context, PLCoreService.class);
		serviceIntent.putExtra(PLCoreService.KEY_INTENT_FROM_BROADCAST, true);
		serviceIntent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, intent.getStringExtra(PLCoreService.KEY_CONTENT_CLASS_NAME));
		context.startService(serviceIntent);
	}
}
