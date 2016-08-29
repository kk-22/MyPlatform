package jp.co.my.myplatform.service.navigation;

import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLHomeView extends PLNavigationView {


	public PLHomeView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.navigation_home, this);

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.start_activity_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLApplication.startMainActivity();
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
		findViewById(R.id.browser_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLBrowserView.class);
			}
		});
		findViewById(R.id.application_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getAppStrategy().startApp();
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLSetAlarmView.class);
			}
		});
	}
}
