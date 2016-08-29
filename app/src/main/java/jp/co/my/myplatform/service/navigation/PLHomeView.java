package jp.co.my.myplatform.service.navigation;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.browser.PLBrowserView;

public class PLHomeView extends PLNavigationView {


	public PLHomeView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.navigation_home, this);

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.browser_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLNavigationController.getInstance().pushView(PLBrowserView.class);
			}
		});
		findViewById(R.id.application_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClassName("jp.cloverlab.yurudora", "jp.cloverlab.yurudora.Yurudora");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);

				PLNavigationController.getInstance().hideNavigationIfNeeded();
			}
		});
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLNavigationController.getInstance().pushView(PLSetAlarmView.class);
			}
		});
	}
}
