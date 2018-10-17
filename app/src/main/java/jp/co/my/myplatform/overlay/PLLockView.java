package jp.co.my.myplatform.overlay;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLAlarmContent;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLDeviceSetting;
import jp.co.my.myplatform.core.PLWakeLockManager;

public class PLLockView extends PLOverlayView {
	private Handler mWakeLockReleaseHandler;
	private boolean mIsStrongLock; // trueならロック画面が非表示にならず、閉じるの操作が複雑になる
	private Switch mLockSwitch1, mLockSwitch2, mAlarmSwitch;
	private Button mStrongButton;
	private TextView mAlarmText;

	public PLLockView() {
		super();

		LayoutInflater.from(getContext()).inflate(R.layout.overlay_lock_view, this);
		mLockSwitch1 = findViewById(R.id.switch1);
		mLockSwitch2 = findViewById(R.id.switch2);
		mAlarmSwitch = findViewById(R.id.alarm_switch);
		mAlarmText = findViewById(R.id.alarm_text);
		findViewById(R.id.open_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mLockSwitch1.isChecked() || !mLockSwitch2.isChecked()) {
					mLockSwitch1.setChecked(false);
					mLockSwitch2.setChecked(false);
					return;
				}
				PLCoreService.getOverlayManager().removeOverlayView(PLLockView.this);
			}
		});
		mStrongButton = findViewById(R.id.strong_button);
		mStrongButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				enableStrongLock();
			}
		});

		// ディスプレイを暗くする
		PLDeviceSetting.setMinScreenBrightness();
	}

	@Override
	public void viewWillRemove() {
		PLCoreService.getOverlayManager().clearFocus(this);

		// 画面ロック時に変えた設定を元に戻す
		PLDeviceSetting.revertScreenBrightness();
		if (mWakeLockReleaseHandler != null) {
			PLWakeLockManager.getInstance().decrementKeepScreen();
			mWakeLockReleaseHandler.removeCallbacksAndMessages(null);
			mWakeLockReleaseHandler = null;
		}
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		return getBaseParamsForFullView();
	}

	public void keepScreenWithLock(int minute) {
		if (minute <= 0) {
			enableStrongLock();
			return;
		}
		PLWakeLockManager.getInstance().incrementKeepScreen();
		mWakeLockReleaseHandler = new Handler();
		mWakeLockReleaseHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				PLWakeLockManager.getInstance().decrementKeepScreen();
				mWakeLockReleaseHandler = null;
				if (mAlarmSwitch.isChecked()) {
					// 指定時間経過を通知するためにアラームを鳴らす
					PLAlarmContent alarmContent = new PLAlarmContent();
					alarmContent.startAlarm();
					PLCoreService.getNavigationController().pushView(alarmContent);
					PLCoreService.getNavigationController().displayNavigationIfNeeded();
				}
				if (!mIsStrongLock) {
					// ロック解除
					PLCoreService.getOverlayManager().removeOverlayView(PLLockView.this);
				}
			}
		}, minute * 60000);
	}

	private void enableStrongLock() {
		if (mIsStrongLock) {
			return;
		}
		mIsStrongLock = true;
		mLockSwitch1.setChecked(false);
		mLockSwitch2.setChecked(false);
		mLockSwitch1.setEnabled(true);
		mLockSwitch2.setEnabled(true);
		mStrongButton.setVisibility(View.INVISIBLE);
		mAlarmSwitch.setVisibility(View.INVISIBLE);
		if (mAlarmSwitch.isChecked()) {
			mAlarmText.append(" ON");
		}
	}
}
