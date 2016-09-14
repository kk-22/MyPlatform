package jp.co.my.myplatform.service.core;

import android.content.Context;
import android.os.PowerManager;

import jp.co.my.common.util.MYLogUtil;

public class PLWakeLockManager {

	private Context mContext;
	private int mKeepCPUCount;
	private int mKeepScreenCount;
	private PowerManager.WakeLock mWakeLock;

	private static PLWakeLockManager sInstance;

	private PLWakeLockManager(Context context) {
		super();
		mContext = context;

		mKeepCPUCount = 0;
		mKeepScreenCount = 0;
	}

	public static PLWakeLockManager getInstance() {
		if (sInstance == null) {
			sInstance = new PLWakeLockManager(PLApplication.getContext());
		}
		return sInstance;
	}

	synchronized public void incrementKeepCPU() {
		MYLogUtil.outputLog("CPUのインクリメント mKeepCPUCount=" +mKeepCPUCount);
		if (mKeepCPUCount == 0 && mKeepScreenCount == 0) {
			keepCPU();
		}
		mKeepCPUCount++;
	}

	synchronized public void incrementKeepScreen() {
		MYLogUtil.outputLog("Screenのインクリメント mKeepScreenCount=" +mKeepScreenCount);
		if (mKeepScreenCount == 0) {
			keepScreen();
		}
		mKeepScreenCount++;
	}

	synchronized public void decrementKeepCPU() {
		MYLogUtil.outputLog("CPUのデクリメント mKeepCPUCount=" +mKeepCPUCount);
		mKeepCPUCount--;
		if (mKeepCPUCount < 0) {
			MYLogUtil.showErrorToast("decrementKeepCPU mKeepCPUCount=" +mKeepCPUCount);
		}

		if (mKeepCPUCount <= 0 && mKeepScreenCount <= 0) {
			releaseWakeLockIfNeeded();
		} else {
			MYLogUtil.outputLog("他で使用中　mKeepCPUCount=" +mKeepCPUCount +", mKeepScreenCount=" +mKeepScreenCount);
		}
	}

	synchronized public void decrementKeepScreen() {
		MYLogUtil.outputLog("Screenのデクリメント mKeepScreenCount=" +mKeepScreenCount);
		mKeepScreenCount--;
		if (mKeepScreenCount < 0) {
			MYLogUtil.showErrorToast("decrementKeepScreen mKeepScreenCount=" +mKeepScreenCount);
		}

		if (mKeepScreenCount > 0) {
			MYLogUtil.outputLog("他でScreen使用中　mKeepCPUCount=" +mKeepCPUCount +", mKeepScreenCount=" +mKeepScreenCount);
		} else if (mKeepCPUCount > 0) {
			keepCPU();
			MYLogUtil.outputLog("他のためにCPU保持　mKeepCPUCount=" +mKeepCPUCount +", mKeepScreenCount=" +mKeepScreenCount);
		} else {
			releaseWakeLockIfNeeded();
		}
	}

	private void keepCPU() {
		MYLogUtil.outputLog("keepCPU");
		releaseWakeLockIfNeeded();

		PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPlatform");
		mWakeLock.acquire();
	}

	private void keepScreen() {
		MYLogUtil.outputLog("keepScreen");
		releaseWakeLockIfNeeded();

		// SCREEN_DIM_WAKE_LOCK のDeprecate代用はgetWindow()しかない
		PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyPlatform");
		mWakeLock.acquire();
	}

	private void releaseWakeLockIfNeeded() {
		MYLogUtil.outputLog("wakeLockリリース可能か判定");
		if (mWakeLock == null || !mWakeLock.isHeld()) {
			return;
		}
		MYLogUtil.outputLog("wakeLockをリリース");
		mWakeLock.release();
		mWakeLock = null;
	}

	// getter
	public int getKeepCPUCount() {
		return mKeepCPUCount;
	}

	public int getKeepScreenCount() {
		return mKeepScreenCount;
	}

	public PowerManager.WakeLock getWakeLock() {
		return mWakeLock;
	}
}
