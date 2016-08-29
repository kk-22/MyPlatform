package jp.co.my.myplatform.service.app;

import android.content.Intent;

public class PLYurudoraApp extends PLAppStrategy {

	protected Intent setClassNameToIntent(Intent intent) {
		intent.setClassName("jp.cloverlab.yurudora", "jp.cloverlab.yurudora.Yurudora");
		return intent;
	}
}
