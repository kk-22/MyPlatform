package jp.co.my.myplatform.content;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.app.PLAppStrategy;
import jp.co.my.myplatform.app.PLFireEmblemApp;
import jp.co.my.myplatform.browser.PLHistoryBrowserView;
import jp.co.my.myplatform.core.PLApplication;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.debug.PLDebugView;
import jp.co.my.myplatform.explorer.PLExplorerView;
import jp.co.my.myplatform.memo.PLMemoEditorView;
import jp.co.my.myplatform.mysen.PLMSWarContent;
import jp.co.my.myplatform.news.PLNewsPagerView;
import jp.co.my.myplatform.overlay.PLLockView;
import jp.co.my.myplatform.overlay.PLNavigationController;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.twitter.PLTWListView;
import jp.co.my.myplatform.wikipedia.PLWikipediaViewer;

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
		findViewById(R.id.size_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLListPopover.showItems(
						new PLListPopover.PLListItem("上半分", new Runnable() {
							@Override
							public void run() {
								PLNavigationController navigation = PLCoreService.getNavigationController();
								navigation.setNavigationButtonVisibility(View.VISIBLE);
								navigation.resizeNavigation(true, false);
							}
						})
						, new PLListPopover.PLListItem("全画面", new Runnable() {
							@Override
							public void run() {
								PLNavigationController navigation = PLCoreService.getNavigationController();
								navigation.setNavigationButtonVisibility(View.GONE);
								navigation.resizeNavigation(false, false);
							}
						})
						, new PLListPopover.PLListItem("下半分", new Runnable() {
							@Override
							public void run() {
								PLNavigationController navigation = PLCoreService.getNavigationController();
								navigation.setNavigationButtonVisibility(View.VISIBLE);
								navigation.resizeNavigation(true, true);
							}
						})
				);
			}
		});
		findViewById(R.id.twitter_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLTWListView.class);
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
		findViewById(R.id.games_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

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
				PLCoreService.getNavigationController().pushView(PLHistoryBrowserView.class);
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
				new PLListPopover.PLListItem("サービス終了", new Runnable() {
					@Override
					public void run() {
						stopService();
					}
				}),
				new PLListPopover.PLListItem("ボタン非表示", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().hideNavigationIfNeeded();
						PLCoreService.getOverlayManager().removeFrontOverlays();
					}
				}),
				new PLListPopover.PLListItem("アプリ一覧", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLAppListContent.class);
					}
				}),
				new PLListPopover.PLListItem("Wikipedia", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLWikipediaViewer.class);
					}
				}),
				new PLListPopover.PLListItem("MySen", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLMSWarContent.class);
					}
				}),
				new PLListPopover.PLListItem("デバッグ", new Runnable() {
					@Override
					public void run() {
						PLCoreService.getNavigationController().pushView(PLDebugView.class);
					}
				})
		);
	}

	private void stopService() {
		new PLConfirmationPopover("サービス終了", new PLConfirmationPopover.PLConfirmationListener() {
			@Override
			public void onClickButton(boolean isYes) {
				PLApplication.stopCoreService();
			}
		}, null);
	}

	private void startApp() {
		String[] titles = {"FEヒーローズ"};
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