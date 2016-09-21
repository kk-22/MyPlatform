package jp.co.my.myplatform.service.content;

import android.app.ActivityManager;
import android.content.Context;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLWakeLockManager;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.popover.PLListPopover;

public class PLDebugView extends PLContentView {

	private TextView mMemoryText;

	public PLDebugView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_debug, this);
		mMemoryText = (TextView) findViewById(R.id.memory_text);

		updateWakeUpText();
		updateMemoryText();
		setClickEvent();

		findViewById(R.id.delete_db_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] titles = {"データベース消去"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						PLDebugView.this.removeTopPopover();
						PLCoreService.getNavigationController().pushView(PLAppListView.class);
						getContext().deleteDatabase(PLDatabase.NAME + ".db");
						MYLogUtil.outputLog("Delete DB. Please restart app");
					}
				}).showPopover();
			}
		});
	}

	private void updateWakeUpText() {
		PLWakeLockManager manager = PLWakeLockManager.getInstance();
		PowerManager.WakeLock wakeLock = manager.getWakeLock();
		String isHoldStr = "null";
		if (wakeLock != null) {
			isHoldStr = String.valueOf(manager.getWakeLock().isHeld());
		}
		((TextView) findViewById(R.id.is_hold_text)).setText(isHoldStr);
		((TextView) findViewById(R.id.cpu_count_text)).setText(Integer.toString(manager.getKeepScreenCount()));
		((TextView) findViewById(R.id.screen_count_text)).setText(Integer.toString(manager.getKeepCPUCount()));
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
