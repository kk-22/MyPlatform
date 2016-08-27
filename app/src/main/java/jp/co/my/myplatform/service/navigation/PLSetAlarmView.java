package jp.co.my.myplatform.service.navigation;

import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.view.PLSelectTimeView;

public class PLSetAlarmView extends PLNavigationView {

	private PLSelectTimeView mSelectTimeView;
//	private SUAlarmTimer mTimer;

	public PLSetAlarmView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.navigation_set_alarm, this);
		mSelectTimeView = (PLSelectTimeView) findViewById(R.id.time_select_view);
//		mSelectTimeView.setPrevCalendar(mTimer.getStartCalendar());

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.set_alarm_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String timeString = mSelectTimeView.getSelectTimeString();
				MYLogUtil.showToast(timeString +"後にアラームセット");
//				Calendar calendar = mSelectTimeView.getSelectTimeCalendar();
//				mTimer.timerSchedule(calendar);
//				PLOverlayManager overlayManager = PLOverlayManager.getInstance();
//				overlayManager.removeModalView();
//				SUDeviceSetting.cpuWakeLockIncrement();
			}
		});
		findViewById(R.id.cancel_alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectTimeView.resetAllTime();
//				PLOverlayManager overlayManager = PLOverlayManager.getInstance();
//				overlayManager.removeModalView();
//				overlayManager.removeOverlayView(SULockView.class);
//				if (mTimer.timerCancel()) {
//					MYLogUtil.showToast("タイマーキャンセルしました");
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							// synchronizedにより処理が止まらないように非同期実行
//							SUDeviceSetting.cpuWakeLockDecrement();
//						}
//					}).start();
//				}
			}
		});
	}
}
