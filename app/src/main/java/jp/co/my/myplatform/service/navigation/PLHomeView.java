package jp.co.my.myplatform.service.navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.app.PLAppStrategy;
import jp.co.my.myplatform.service.app.PLYurudoraApp;
import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.overlay.PLLockView;
import jp.co.my.myplatform.service.popover.PLListPopover;

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
		findViewById(R.id.lock_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
				PLLockView lockView = new PLLockView();
				PLCoreService.getOverlayManager().addOverlayView(lockView);
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
				putAppButton();
			}
		});
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLSetAlarmView.class);
			}
		});
	}

	private void putAppButton() {
		String[] titles = {"ゆるドラシル"};
		PLListPopover popover = new PLListPopover(null, titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLAppStrategy appStrategy = null;
				switch (position) {
					case 0: {
						appStrategy = new PLYurudoraApp();
						break;
					}
					default:
						break;
				}
				if (appStrategy != null) {
					PLCoreService.setAppStrategy(appStrategy);
					PLCoreService.getAppStrategy().startApp();
				}
				PLHomeView.this.removeTopPopover();
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
		addPopover(popover);
	}
}
