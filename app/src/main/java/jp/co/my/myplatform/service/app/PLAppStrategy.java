package jp.co.my.myplatform.service.app;

import android.content.Intent;

import jp.co.my.myplatform.service.core.PLApplication;

public class PLAppStrategy {

	public boolean startApp() {
		Intent intent = setClassNameToIntent(new Intent());
		if (intent == null) {
			return false;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PLApplication.getContext().startActivity(intent);
		return true;
	}

	protected Intent setClassNameToIntent(Intent intent) {
		return null;
	}
}
