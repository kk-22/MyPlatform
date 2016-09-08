package jp.co.my.myplatform.service.content;

import android.app.ActivityManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLDebugView extends PLContentView {

	private TextView mMemoryText;

	public PLDebugView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_debug, this);
		mMemoryText = (TextView) findViewById(R.id.memory_text);

		updateMemoryText();
		setClickEvent();
	}

	private void updateMemoryText() {
		String packageName = getContext().getPackageName();
		ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (ActivityManager.RunningServiceInfo serviceInfo : services) {
			if (!serviceInfo.service.getPackageName().equals(packageName)) {
				continue;
			}
			MYLogUtil.outputLog("app:service: pkg = " + serviceInfo.service.getPackageName() + " pid = " + serviceInfo.pid);

			int[] processIds = {serviceInfo.pid};
			android.os.Debug.MemoryInfo[] memories = activityManager.getProcessMemoryInfo(processIds);
			for (android.os.Debug.MemoryInfo info : memories) {
				int totalPss = info.getTotalPss();
				mMemoryText.setText((totalPss / 1000) +"MB");
			}
		}
	}

	private void setClickEvent() {
		findViewById(R.id.memory_reload_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateMemoryText();
			}
		});
		findViewById(R.id.gc_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.gc();
				updateMemoryText();
			}
		});
	}
}
