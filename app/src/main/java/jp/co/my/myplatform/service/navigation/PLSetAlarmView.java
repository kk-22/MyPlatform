package jp.co.my.myplatform.service.navigation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.view.PLSelectTimeView;

public class PLSetAlarmView extends PLNavigationView {

	private static final String KEY_ALARM_TIME = "AlarmTime";

	private int mAlarmCount;
	private AlarmManager mAlarmManager;
	private PLSelectTimeView mSelectTimeView;

	public PLSetAlarmView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.navigation_set_alarm, this);
		mSelectTimeView = (PLSelectTimeView) findViewById(R.id.time_select_view);
		mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		mAlarmCount = 0;

		setButtonEvent();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDefaultTimeIfNecessary();
	}

	public void startAlarm() {
		Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(300);

		mAlarmCount++;
		MYLogUtil.showToast("alarm count=" +mAlarmCount);
		if (5 <= mAlarmCount) {
			MYLogUtil.showToast("アラーム強制終了　回数=" +mAlarmCount);
			cancelAlarm();
		}
	}

	private void setButtonEvent() {
		findViewById(R.id.set_alarm_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String timeString = mSelectTimeView.getSelectTimeString();
				MYLogUtil.showToast(timeString +"後にアラームセット");

				Calendar calendar = mSelectTimeView.getSelectTimeCalendar();
				Long timeInMillis = calendar.getTimeInMillis();
				SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
				editor.putLong(KEY_ALARM_TIME, timeInMillis);
				editor.commit();

				PendingIntent alarmSender = createPendingIntent();
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, 10000, alarmSender);

				mAlarmCount = 0;
			}
		});
		findViewById(R.id.cancel_alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isExistPendingIntent()) {
					 MYLogUtil.showToast("アラーム未登録");
					return;
				}
				MYLogUtil.showToast("アラーム解除");
				mSelectTimeView.resetAllTime();
				cancelAlarm();
			}
		});
	}

	private void cancelAlarm() {
		PendingIntent alarmSender = createPendingIntent();
		mAlarmManager.cancel(alarmSender);
		alarmSender.cancel();

		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.remove(KEY_ALARM_TIME);
		editor.commit();
	}

	private void setDefaultTimeIfNecessary() {
		if (!isExistPendingIntent()) {
			return;
		}
		SharedPreferences pref = MYLogUtil.getPreference();
		Long timeInMillis = pref.getLong(KEY_ALARM_TIME, 0);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		mSelectTimeView.setPrevCalendar(calendar);
	}

	private Boolean isExistPendingIntent() {
		return (getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null);
	}

	private PendingIntent createPendingIntent() {
		return getPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private PendingIntent getPendingIntent(int flags) {
		Intent intent = new Intent(getContext(), PLCoreService.class);
		intent.putExtra(PLCoreService.KEY_CLASS_NAME, getClass().getCanonicalName());
		PendingIntent pendingIntent = PendingIntent.getService(getContext(), 77, intent, flags);
		return pendingIntent;
	}
}
