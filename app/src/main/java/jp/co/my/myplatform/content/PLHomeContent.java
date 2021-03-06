package jp.co.my.myplatform.content;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.app.PLAppStrategy;
import jp.co.my.myplatform.app.PLFireEmblemApp;
import jp.co.my.myplatform.browser.PLBaseBrowserContent;
import jp.co.my.myplatform.browser.PLHistoryBrowserContent;
import jp.co.my.myplatform.core.PLApplication;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.debug.PLDebugContent;
import jp.co.my.myplatform.explorer.PLExplorerContent;
import jp.co.my.myplatform.memo.PLMemoEditorContent;
import jp.co.my.myplatform.news.PLNewsPagerContent;
import jp.co.my.myplatform.overlay.PLLockOverlay;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.puyo.PLPuyoGameContent;
import jp.co.my.myplatform.simulator.PLDamageSimulateContent;

public class PLHomeContent extends PLContentView {


	public PLHomeContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_home, this);

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.menu_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMenu();
			}
		});
		findViewById(R.id.simulator_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLDamageSimulateContent.class);
			}
		});
		findViewById(R.id.lock_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLockList();
			}
		});
		findViewById(R.id.games_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showGameList();
			}
		});
		findViewById(R.id.memo_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLMemoEditorContent.class);
			}
		});
		findViewById(R.id.browser_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLHistoryBrowserContent.class);
			}
		});
		findViewById(R.id.twitter_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLBaseBrowserContent browserView = new PLBaseBrowserContent();
				browserView.setDisableNaviCloseButton(true);
				browserView.getCurrentWebView().loadUrl("https://twitter.com/dorann217/lists/%E3%83%AA%E3%82%B9%E3%83%88");
				PLCoreService.getNavigationController().pushView(browserView);
			}
		});
		findViewById(R.id.explorer_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLExplorerContent.class);
			}
		});
		findViewById(R.id.news_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLNewsPagerContent.class);
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
				PLCoreService.getNavigationController().pushView(PLAlarmContent.class);
			}
		});
		findViewById(R.id.calculator_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLCalculatorContent.class);
			}
		});
	}

	private void showMenu() {
		PLListPopover.showItems(
				new PLListPopover.PLListItem("Push??????", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLPushNotificationSettingContent.class);
					}
				}),
				new PLListPopover.PLListItem("??????????????????", new Runnable() {
					@Override
					public void run() {
						stopService();
					}
				}),
				new PLListPopover.PLListItem("??????????????????", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getOverlayManager().removeFrontOverlays();
						PLCoreService.getNavigationController().hideNavigationIfNeeded();
					}
				}),
				new PLListPopover.PLListItem("???????????????", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLAppListContent.class);
					}
				}),
				new PLListPopover.PLListItem("????????????", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLDebugContent.class);
					}
				}),
				new PLListPopover.PLListItem("???????????????", new Runnable() {
					@Override
					public void run() {
						showSizeList();
					}
				})
		);
	}

	private void showLockList() {
		PLListPopover.showItems(
				new PLListPopover.PLListItem("Wakelock??????", new Runnable() {
					@Override
					public void run() {
						displayLockView(-1);
					}
				}),
				new PLListPopover.PLListItem("10???", new Runnable() {
					@Override
					public void run() {
						displayLockView(10);
					}
				}),
				new PLListPopover.PLListItem("7???", new Runnable() {
					@Override
					public void run() {
						displayLockView(7);
					}
				}),
				new PLListPopover.PLListItem("5???", new Runnable() {
					@Override
					public void run() {
						displayLockView(5);
					}
				}),
				new PLListPopover.PLListItem("3???", new Runnable() {
					@Override
					public void run() {
						displayLockView(3);
					}
				}),
				new PLListPopover.PLListItem("2???", new Runnable() {
					@Override
					public void run() {
						displayLockView(2);
					}
				}),
				new PLListPopover.PLListItem("1???", new Runnable() {
					@Override
					public void run() {
						displayLockView(1);
					}
				})
		);
	}

	private void showGameList() {
		PLListPopover.showItems(
				new PLListPopover.PLListItem("????????????", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLPuyoGameContent.class);
					}
				})
		);
	}

	private void showSizeList() {
		PLListPopover.showItems(
				new PLListPopover.PLListItem("?????????", new Runnable() {
					@Override
					public void run() {
						PLNavigationOverlay navigation = PLCoreService.getNavigationController();
						navigation.setNavigationButtonVisibility(View.VISIBLE);
						navigation.resizeNavigation(true, false);
					}
				})
				, new PLListPopover.PLListItem("?????????", new Runnable() {
					@Override
					public void run() {
						PLNavigationOverlay navigation = PLCoreService.getNavigationController();
						navigation.setNavigationButtonVisibility(View.GONE);
						navigation.resizeNavigation(false, false);
					}
				})
				, new PLListPopover.PLListItem("?????????", new Runnable() {
					@Override
					public void run() {
						PLNavigationOverlay navigation = PLCoreService.getNavigationController();
						navigation.setNavigationButtonVisibility(View.VISIBLE);
						navigation.resizeNavigation(true, true);
					}
				})
		);
	}

	private void displayLockView(int keepScreenMin) {
		PLCoreService.getNavigationController().hideNavigationIfNeeded();
		PLLockOverlay lockView = new PLLockOverlay();
		lockView.keepScreenWithLock(keepScreenMin);
		PLCoreService.getOverlayManager().addOverlayView(lockView);
	}

	private void stopService() {
		new PLConfirmationPopover("??????????????????", new PLConfirmationPopover.PLConfirmationListener() {
			@Override
			public void onClickButton(boolean isYes) {
				PLApplication.stopCoreService();
			}
		}, null);
	}

	private void startApp() {
		String[] titles = {"FE???????????????"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLAppStrategy appStrategy = null;
				switch (position) {
					case 0: {
						appStrategy = new PLFireEmblemApp();
						break;
					}
					default:
						break;
				}
				if (appStrategy != null) {
					PLCoreService.setAppStrategy(appStrategy);
					PLCoreService.getAppStrategy().startApp();
				}
				PLHomeContent.this.removeTopPopover();
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		}).showPopover();
	}
}
