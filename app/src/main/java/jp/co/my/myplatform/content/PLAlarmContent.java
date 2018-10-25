package jp.co.my.myplatform.content;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.co.my.common.util.MYCalendarUtil;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLBroadcastReceiver;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLWakeLockManager;
import jp.co.my.myplatform.overlay.PLFrontButtonOverlay;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.view.PLSelectTimeView;

import static jp.co.my.myplatform.core.PLCoreService.KEY_CANCEL_ALARM;

public class PLAlarmContent extends PLContentView {

	private static final String KEY_ALARM_TIME = "AlarmTime";
	private static final String KEY_SNOOZE_SEC = "SnoozeSec";

	// 画面終了後もキャンセル可能にするためにstatic
	private static Handler sAlarmHandler;

	private int mAlarmCount;
	private Button mStartButton;
	private Button mCancelButton;
	private RadioGroup mSnoozeRadio;
	private PLSelectTimeView mSelectTimeView;

	public PLAlarmContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_set_alarm, this);
		mStartButton = findViewById(R.id.set_alarm_button);
		mCancelButton = findViewById(R.id.cancel_alarm_button);
		mSnoozeRadio = findViewById(R.id.snooze_radio_group);
		mSelectTimeView = findViewById(R.id.time_select_view);

		setButtonEvent();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDefaultTimeIfNecessary();
	}

	public void startAlarm() {
		if (sAlarmHandler != null) {
			MYLogUtil.showErrorToast("すでにアラームが開始しています count=" +mAlarmCount);
			return;
		}
		MYLogUtil.outputLog("アラーム開始");
		mAlarmCount = 0;
		mCancelButton.setEnabled(true);
		PLWakeLockManager.getInstance().incrementKeepCPU();

		sAlarmHandler = new Handler();
		sAlarmHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAlarmCount++;
				Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator != null) {
					long milliseconds = 400;
					if (2 < mAlarmCount) {
						// 振動を強くする
						milliseconds *= (mAlarmCount + 1) / 2;
					}
					MYLogUtil.outputLog(" vibrato milliseconds=" +milliseconds +" count=" +mAlarmCount);
					vibrator.vibrate(milliseconds);
				}

				// 1度だけ画面点灯。バックライト点灯時間がスヌーズ秒より短い場合は消灯する。
				PLWakeLockManager.getInstance().incrementKeepScreen();
				PLWakeLockManager.getInstance().decrementKeepScreen();

				if (mAlarmCount < 7) {
					MYLogUtil.showToast("alarm count=" +mAlarmCount);
					SharedPreferences pref = MYLogUtil.getPreference();
					int snoozeSec = pref.getInt(KEY_SNOOZE_SEC, 10);
					int delayMills = snoozeSec * 1000;
					if (2 < mAlarmCount) {
						// 離席時用に間隔を伸ばす
						delayMills *= (mAlarmCount - 2) * 3;
					}
					sAlarmHandler.postDelayed(this, delayMills);
					return;
				}

				MYLogUtil.showToast("アラーム強制終了　回数=" +mAlarmCount);
				if (mCancelButton.getParent() != null) {
					// Viewが生存しているときのみ実行
					mCancelButton.setEnabled(false);
				}
				stopAlarm();
			}
		}, 1);
	}

	private void setButtonEvent() {
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sAlarmHandler != null) {
					// すでにアラームが鳴っている場合
					stopAlarm();
				}
				Calendar calendar = mSelectTimeView.getCurrentSelectCalendar();
				Long timeInMillis = calendar.getTimeInMillis();
				SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
				editor.putLong(KEY_ALARM_TIME, timeInMillis);
				int snoozeSec = getSelectSnoozeSec();
				editor.putInt(KEY_SNOOZE_SEC, snoozeSec);
				editor.commit();

				mCancelButton.setEnabled(true);
				updateFrontButtonText(calendar);
				String dateString = MYCalendarUtil.getDateTextFromCalendar(calendar);
				String timeString = mSelectTimeView.getSelectTimeString();
				MYLogUtil.showLongToast("schedule :" +dateString +"\nremaining:" +timeString +"ago\nschedule :" +snoozeSec +"/sec");
				showAlarmNotification(dateString);

				if (!mSelectTimeView.isZeroAll()) {
					PendingIntent alarmSender = createPendingIntent();
					AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
					if (alarmManager != null) {
						alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmSender);
						PLCoreService.getNavigationController().popView();
					}
				} else {
					startAlarm();
				}
			}
		});
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sAlarmHandler != null) {
					// すでにアラームが鳴っている場合
					mCancelButton.setEnabled(false);
					stopAlarm();
					return;
				}
				String[] titles = {"アラーム解除"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						PLAlarmContent.this.removeTopPopover();
						mSelectTimeView.resetAllTime();
						mCancelButton.setEnabled(false);
						cancelAlarm();
					}
				}).showPopover();
			}
		});
	}

	// 予約したアラーム解除
	private static void cancelAlarm() {
		MYLogUtil.outputLog("アラームキャンセル");
		// TODO: timerからコール時にインスタンスが破棄済みなためクラッシュ
		if (isExistPendingIntent()) {
			PendingIntent alarmSender = createPendingIntent();
			AlarmManager alarmManager = (AlarmManager) PLCoreService.getContext().getSystemService(Context.ALARM_SERVICE);
			if (alarmManager != null) {
				alarmManager.cancel(alarmSender);
				alarmSender.cancel();
			}
		}
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.remove(KEY_ALARM_TIME);
		editor.remove(KEY_SNOOZE_SEC);
		editor.commit();

		PLFrontButtonOverlay buttonView = PLCoreService.getOverlayManager().getOverlayView(PLFrontButtonOverlay.class);
		buttonView.clearText(PLAlarmContent.class);

		PLCoreService.getCoreService().showDefaultNotification();
		PLCoreService.getNavigationController().popView();

		if (sAlarmHandler == null) {
			// アラーム開始前なら通知
			Vibrator vibrator = (Vibrator) PLCoreService.getContext().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(400);
		}
	}

	// 開始したアラーム停止
	public static void stopAlarm() {
		cancelAlarm();
		if (sAlarmHandler != null) {
			PLWakeLockManager.getInstance().decrementKeepCPU();
			sAlarmHandler.removeCallbacksAndMessages(null);
			sAlarmHandler = null;
		}
	}

	private void setDefaultTimeIfNecessary() {
		if (sAlarmHandler != null) {
			mCancelButton.setEnabled(true);
			return;
		}

		SharedPreferences pref = MYLogUtil.getPreference();
		Long timeInMillis = pref.getLong(KEY_ALARM_TIME, 0);
		if (timeInMillis == 0) {
			mSelectTimeView.resetAllTime();
			mCancelButton.setEnabled(false);
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		mSelectTimeView.setAllProgressFromCalendar(calendar);
		mCancelButton.setEnabled(true);

		int snoozeSec = pref.getInt(KEY_SNOOZE_SEC, -1);
		if (0 < snoozeSec) {
			mSnoozeRadio.check(getIdOfSnoozeSec(snoozeSec));
		} else {
			MYLogUtil.showErrorToast("Snooze sec wasn't saved");
		}
	}

	private static Boolean isExistPendingIntent() {
		return (getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null);
	}

	private static PendingIntent createPendingIntent() {
		return getPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private static PendingIntent getPendingIntent(int flags) {
		Intent intent = new Intent(PLCoreService.getContext(), PLBroadcastReceiver.class);
		intent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, PLAlarmContent.class.getCanonicalName());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(PLCoreService.getContext(), 77, intent, flags);
		return pendingIntent;
	}

	private void updateFrontButtonText(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String text = format.format(calendar.getTime());

		PLFrontButtonOverlay buttonView = PLCoreService.getOverlayManager().getOverlayView(PLFrontButtonOverlay.class);
		buttonView.setText(this.getClass(), 8, text);
	}

	private void showAlarmNotification(String dateString) {
		Intent intent = new Intent(getContext(), PLCoreService.class);
		intent.putExtra(KEY_CANCEL_ALARM, true);
		PendingIntent pendingIntent = PendingIntent.getService(getContext(), 76, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		RemoteViews remote = new RemoteViews(getContext().getPackageName(), R.layout.notification_alarml);
		remote.setTextViewText(R.id.date_text, dateString);
		remote.setOnClickPendingIntent(R.id.notification_layout, pendingIntent);
		PLCoreService.getCoreService().showNotification(remote);
	}

	private int getSelectSnoozeSec() {
		switch (mSnoozeRadio.getCheckedRadioButtonId()) {
			case R.id.snooze_10sec_radio: {
				return 10;
			}
			case R.id.snooze_30sec_radio: {
				return 30;
			}
			case R.id.snooze_1min_radio: {
				return 60;
			}
			case R.id.snooze_5min_radio: {
				return 300;
			}
			case R.id.snooze_10min_radio: {
				return 600;
			}
			default:
				MYLogUtil.showErrorToast("スヌーズ未選択");
				return 0;
		}
	}

	private int getIdOfSnoozeSec(int snoozeSec) {
		switch (snoozeSec) {
			case 10:
				return R.id.snooze_10sec_radio;
			case 30:
				return R.id.snooze_30sec_radio;
			case 60:
				return R.id.snooze_1min_radio;
			case 300:
				return R.id.snooze_5min_radio;
			case 600:
				return R.id.snooze_10min_radio;
			default:
				return 0;
		}
	}
}
