package jp.co.my.myplatform.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

import jp.co.my.common.util.MYCalendarUtil;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLFirebaseMessagingService;
import jp.co.my.myplatform.popover.PLTextFieldPopover;
import jp.co.my.myplatform.view.PLSelectTimeView;

public class PLPushNotificationSettingContent extends PLContentView {

	private static final String KEY_PUSH_ALARM_PERIOD = "KEY_PUSH_ALARM_PERIOD";
	private static final String KEY_PUSH_ALARM_HIT_WORD = "KEY_PUSH_ALARM_HIT_WORD";

	private PLSelectTimeView mSelectTimeView;
	private Button mCancelButton, mWordButton;

	public PLPushNotificationSettingContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_push_notification, this);
		mSelectTimeView = findViewById(R.id.time_select_view);
		mCancelButton = findViewById(R.id.cancel_button);
		mWordButton = findViewById(R.id.word_button);

		mSelectTimeView.hideSec();
		mSelectTimeView.setMaxHour(23);
		initButtonEvent();
		showWordOnButton(MYLogUtil.getPreference().getString(KEY_PUSH_ALARM_HIT_WORD, ""));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setDefaultTimeIfNecessary();
	}

	public static boolean shouldStartAlarmByPushNotification(String message) {
		SharedPreferences pref = MYLogUtil.getPreference();
		Long timeInMillis = pref.getLong(KEY_PUSH_ALARM_PERIOD, 0);
		String word = pref.getString(KEY_PUSH_ALARM_HIT_WORD, null);
		if (timeInMillis == 0 || word == null) {
			return false;
		}
		return (System.currentTimeMillis() <= timeInMillis) && message.contains(word);
	}

	private void setDefaultTimeIfNecessary() {
		Long timeInMillis = MYLogUtil.getPreference().getLong(KEY_PUSH_ALARM_PERIOD, 0);
		if (timeInMillis == 0) {
			mSelectTimeView.resetAllTime();
			mCancelButton.setEnabled(false);
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		mSelectTimeView.setAllProgressFromCalendar(calendar);
		if (mSelectTimeView.isZeroAll()) {
			// 期間を超過していたら削除
			cancelAlarmPeriod();
		} else {
			mCancelButton.setEnabled(true);
		}
	}

	private void cancelAlarmPeriod() {
		mSelectTimeView.resetAllTime();
		mCancelButton.setEnabled(false);
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.remove(KEY_PUSH_ALARM_PERIOD);
		editor.commit();
	}

	private void initButtonEvent() {
		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectTimeView.isZeroAll()) {
					return;
				}
				Calendar calendar = mSelectTimeView.getCurrentSelectCalendar();
				Long timeInMillis = calendar.getTimeInMillis();
				SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
				editor.putLong(KEY_PUSH_ALARM_PERIOD, timeInMillis);
				editor.commit();

				mCancelButton.setEnabled(true);
				String dateString = MYCalendarUtil.getDateTextFromCalendar(calendar);
				String timeString = mSelectTimeView.getSelectTimeString();
				MYLogUtil.showLongToast("until :" +dateString +"\nremaining:" +timeString +"ago");
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelAlarmPeriod();

				// 誤タップでのキャンセル時に気付かせる
				Vibrator vibrator = (Vibrator) PLCoreService.getContext().getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator != null) {
					vibrator.vibrate(400);
				}
			}
		});
		mWordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String prevWord = MYLogUtil.getPreference().getString(KEY_PUSH_ALARM_HIT_WORD, "");
				new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
					@Override
					public boolean onEnter(View v, String text) {
						SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
						editor.putString(KEY_PUSH_ALARM_HIT_WORD, text);
						editor.commit();
						showWordOnButton(text);
						return true;
					}
				}).setDefaultText(prevWord).showPopover();
			}
		});
		findViewById(R.id.token_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLFirebaseMessagingService.outputToken();
			}
		});
	}

	private void showWordOnButton(String word) {
		String wordTitle = "ヒットワード：" + word;
		mWordButton.setText(wordTitle);
	}
}
