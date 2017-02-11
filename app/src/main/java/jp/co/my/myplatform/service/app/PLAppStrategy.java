package jp.co.my.myplatform.service.app;

import android.content.Intent;
import android.content.pm.PackageManager;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLAppStrategy {

	public boolean startApp() {
		PackageManager packageManager = PLCoreService.getContext().getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
		if (intent == null) {
			MYLogUtil.showErrorToast("no package=" +getPackageName());
			return false;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PLApplication.getContext().startActivity(intent);
		return true;
	}

	protected String getPackageName() {
		return "";
	}
}
