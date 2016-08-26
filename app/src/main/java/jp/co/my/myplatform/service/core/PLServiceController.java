package jp.co.my.myplatform.service.core;

import android.content.Context;
import android.content.Intent;

import jp.co.my.common.util.MYLogUtil;

public class PLServiceController {

	private static PLServiceController sInstance;

	private Context mContext;

	private PLServiceController() {
		super();
		mContext = PLApplication.getContext();
	}

	public static PLServiceController getInstance() {
		if (sInstance == null) {
			sInstance = new PLServiceController();
		}
		return sInstance;
	}

	public void startCoreService() {
		MYLogUtil.outputLog("startCoreService");
		mContext.startService(new Intent(mContext, PLCoreService.class));
	}

	public void stopCoreService() {
		MYLogUtil.outputLog("stopCoreService");
		mContext.stopService(new Intent(mContext, PLCoreService.class));
	}

	public void destroyCoreService() {

	}
}
