package jp.co.my.myplatform.activity.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLServiceController;

public class PLMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setButtonEvent();

		PLServiceController.getInstance().startCoreService();
	}

	private void setButtonEvent() {
		findViewById(R.id.start_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLServiceController.getInstance().startCoreService();
			}
		});
		findViewById(R.id.stop_Service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLServiceController.getInstance().stopCoreService();
			}
		});
	}
}
