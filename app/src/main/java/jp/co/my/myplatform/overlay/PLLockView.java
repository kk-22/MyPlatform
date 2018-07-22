package jp.co.my.myplatform.overlay;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLDeviceSetting;
import jp.co.my.myplatform.core.PLWakeLockManager;

public class PLLockView extends PLOverlayView {
	private Handler mWakeLockReleaseHandler;
	private boolean mIsStrongLock; // trueならロック画面が非表示にならず、閉じるの操作が複雑になる
	private Switch mSwitch1, mSwitch2;
	private Button mStrongButton;

	public PLLockView() {
		super();

		View view = LayoutInflater.from(getContext()).inflate(R.layout.overlay_lock_view, this);
		mSwitch1 = view.findViewById(R.id.switch1);
		mSwitch2 = view.findViewById(R.id.switch2);
		view.findViewById(R.id.open_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mSwitch1.isChecked() || !mSwitch2.isChecked()) {
					mSwitch1.setChecked(false);
					mSwitch2.setChecked(false);
					return;
				}
				PLCoreService.getOverlayManager().removeOverlayView(PLLockView.this);
			}
		});
		mStrongButton = view.findViewById(R.id.strong_button);
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
				if (mIsStrongLock) {
					return;
				}

				// ロック解除後のタップを防止するために画面を消灯させる
				PLWakeLockManager.getInstance().incrementKeepCPU();
				final int prevTimeout = PLDeviceSetting.getScreenOffTimeout();
				PLDeviceSetting.setScreenOffTimeout(5000);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// ロック解除
						PLCoreService.getOverlayManager().removeOverlayView(PLLockView.this);
						PLDeviceSetting.setScreenOffTimeout(prevTimeout);
						PLWakeLockManager.getInstance().decrementKeepCPU();
					}
				}, 15000);
			}
		}, minute * 60000);
	}

	private void enableStrongLock() {
		if (mIsStrongLock) {
			return;
		}
		mIsStrongLock = true;
		mSwitch1.setChecked(false);
		mSwitch2.setChecked(false);
		mSwitch1.setEnabled(true);
		mSwitch2.setEnabled(true);
		mStrongButton.setVisibility(View.GONE);
	}
}
