package jp.co.my.myplatform.service.content;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.app.PLAppStrategy;
import jp.co.my.myplatform.service.app.PLYurudoraApp;
import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.debug.PLDebugView;
import jp.co.my.myplatform.service.explorer.PLExplorerView;
import jp.co.my.myplatform.service.news.PLNewsPagerView;
import jp.co.my.myplatform.service.overlay.PLLockView;
import jp.co.my.myplatform.service.popover.PLListPopover;

public class PLHomeView extends PLContentView {


	public PLHomeView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_home, this);

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.stop_service_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopService();
			}
		});
		findViewById(R.id.app_list_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLAppListView.class);
			}
		});
		findViewById(R.id.hide_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideButton();
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
		findViewById(R.id.debug_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLDebugView.class);
			}
		});
		findViewById(R.id.memo_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLMemoEditorView.class);
			}
		});
		findViewById(R.id.browser_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLBrowserView.class);
			}
		});
		findViewById(R.id.explorer_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLExplorerView.class);
			}
		});
		findViewById(R.id.news_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLNewsPagerView.class);
			}
		});
		findViewById(R.id.application_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startApp();
			}
		});
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLSetAlarmView.class);
			}
		});
	}

	private void stopService() {
		String[] titles = {"サービス終了"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLApplication.stopCoreService();
			}
		}).showPopover();
	}

	private void hideButton() {
		String[] titles = {"ボタン非表示"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLHomeView.this.removeTopPopover();
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
				PLCoreService.getOverlayManager().removeFrontOverlays();
			}
		}).showPopover();
	}

	private void startApp() {
		String[] titles = {"ゆるドラシル"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
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
		}).showPopover();
	}
}
