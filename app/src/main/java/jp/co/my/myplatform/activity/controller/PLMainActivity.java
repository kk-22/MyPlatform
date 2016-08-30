package jp.co.my.myplatform.activity.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLDeviceSetting;

public class PLMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setButtonEvent();

		PLApplication.startCoreService();
	}

	private void setButtonEvent() {
		findViewById(R.id.start_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.startCoreService();
			}
		});
		findViewById(R.id.stop_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.stopCoreService();

				PLDeviceSetting.revertAllSetting();
			}
		});
	}
}
