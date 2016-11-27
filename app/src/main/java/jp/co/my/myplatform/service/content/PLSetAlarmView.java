package jp.co.my.myplatform.service.content;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLBroadcastReceiver;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLWakeLockManager;
import jp.co.my.myplatform.service.overlay.PLFrontButtonView;
import jp.co.my.myplatform.service.popover.PLListPopover;
import jp.co.my.myplatform.service.view.PLSelectTimeView;

public class PLSetAlarmView extends PLContentView {

	private static final String KEY_ALARM_TIME = "AlarmTime";
	private static final String KEY_SNOOZE_SEC = "SnoozeSec";

	private static Timer sTimer;

	private int mAlarmCount;
	private Button mStartButton;
	private Button mCancelButton;
	private RadioGroup mSnoozeRadio;
	private AlarmManager mAlarmManager;
	private PLSelectTimeView mSelectTimeView;

	public PLSetAlarmView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_set_alarm, this);
		mStartButton = (Button) findViewById(R.id.set_alarm_button);
		mCancelButton = (Button) findViewById(R.id.cancel_alarm_button);
		mSnoozeRadio = (RadioGroup) findViewById(R.id.snooze_radio_group);
		mSelectTimeView = (PLSelectTimeView) findViewById(R.id.time_select_view);
		mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

		setButtonEvent();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDefaultTimeIfNecessary();
	}

	public void startAlarm() {
		if (sTimer != null) {
			MYLogUtil.showErrorToast("すでにアラームが開始しています count=" +mAlarmCount);
			return;
		}
		mAlarmCount = 0;
		PLWakeLockManager.getInstance().incrementKeepScreen();
		SharedPreferences pref = MYLogUtil.getPreference();
		int snoozeSec = pref.getInt(KEY_SNOOZE_SEC, 1);

		sTimer = new Timer(true);
		sTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mAlarmCount++;
				MYLogUtil.showToast("alarm count=" +mAlarmCount);
				Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(400);

				if (7 <= mAlarmCount) {
					MYLogUtil.showToast("アラーム強制終了　回数=" +mAlarmCount);
					stopAlarm();
				}
			}
		}, 1, snoozeSec * 1000);
	}

	private void setButtonEvent() {
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sTimer != null) {
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
				String timeString = mSelectTimeView.getSelectTimeString();
				MYLogUtil.showToast(timeString +"後にアラーム\n" +snoozeSec + "秒毎に通知");

				if (!mSelectTimeView.isZeroAll()) {
					PendingIntent alarmSender = createPendingIntent();
					mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, alarmSender);
					PLCoreService.getNavigationController().goBackView();
				} else {
					startAlarm();
				}
			}
		});
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sTimer != null) {
					// すでにアラームが鳴っている場合
					stopAlarm();
					PLCoreService.getNavigationController().goBackView();
					return;
				}
				String[] titles = {"アラーム解除"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						PLSetAlarmView.this.removeTopPopover();
						mSelectTimeView.resetAllTime();
						cancelAlarm();
						PLCoreService.getNavigationController().goBackView();
					}
				}).showPopover();
			}
		});
	}

	// 予約したアラーム解除
	private void cancelAlarm() {
		MYLogUtil.showToast("アラームキャンセル");
		mCancelButton.setEnabled(false);

		if (isExistPendingIntent()) {
			PendingIntent alarmSender = createPendingIntent();
			mAlarmManager.cancel(alarmSender);
			alarmSender.cancel();
		}
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.remove(KEY_ALARM_TIME);
		editor.remove(KEY_SNOOZE_SEC);
		editor.commit();

		PLFrontButtonView buttonView = PLCoreService.getOverlayManager().getOverlayView(PLFrontButtonView.class);
		buttonView.clearText(this.getClass());
	}

	// 開始したアラーム停止
	private void stopAlarm() {
		cancelAlarm();
		PLWakeLockManager.getInstance().decrementKeepScreen();
		sTimer.cancel();
		sTimer = null;
	}

	private void setDefaultTimeIfNecessary() {
		SharedPreferences pref = MYLogUtil.getPreference();
		Long timeInMillis = pref.getLong(KEY_ALARM_TIME, 0);
		if (timeInMillis == 0) {
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

	private Boolean isExistPendingIntent() {
		return (getPendingIntent(PendingIntent.FLAG_NO_CREATE) != null);
	}

	private PendingIntent createPendingIntent() {
		return getPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT);
	}

	private PendingIntent getPendingIntent(int flags) {
		Intent intent = new Intent(getContext(), PLBroadcastReceiver.class);
		intent.putExtra(PLCoreService.KEY_CONTENT_CLASS_NAME, getClass().getCanonicalName());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 77, intent, flags);
		return pendingIntent;
	}

	private void updateFrontButtonText(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		String text = format.format(calendar.getTime());

		PLFrontButtonView buttonView = PLCoreService.getOverlayManager().getOverlayView(PLFrontButtonView.class);
		buttonView.setText(this.getClass(), 8, text);
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
