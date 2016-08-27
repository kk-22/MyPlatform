package jp.co.my.myplatform.service.navigation;

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
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLNavigationController.getInstance().pushView(PLSetAlarmView.class);
			}
		});
	}
}
