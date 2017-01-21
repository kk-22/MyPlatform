package jp.co.my.myplatform.service.content;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.Calendar;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.activity.controller.PLMainActivity;
import jp.co.my.myplatform.service.app.PLAppStrategy;
import jp.co.my.myplatform.service.app.PLYurudoraApp;
import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.core.PLApplication;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.debug.PLDebugView;
import jp.co.my.myplatform.service.explorer.PLExplorerView;
import jp.co.my.myplatform.service.memo.PLMemoEditorView;
import jp.co.my.myplatform.service.news.PLNewsPagerView;
import jp.co.my.myplatform.service.overlay.PLLockView;
import jp.co.my.myplatform.service.popover.PLListPopover;
import jp.co.my.myplatform.service.twitter.PLTWListView;
import jp.co.my.myplatform.service.wikipedia.PLWikipediaViewer;
import retrofit2.Call;

public class PLHomeView extends PLContentView {


	public PLHomeView() {
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
		findViewById(R.id.app_list_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLAppListView.class);
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
		findViewById(R.id.wikipedia_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLWikipediaViewer.class);
			}
		});
	}

	private void showMenu() {
		String[] titles = {"サービス終了", "ボタン非表示"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLHomeView.this.removeTopPopover();
				switch (position) {
					case 0: {
						stopService();
						break;
					}
					case 1: {
						hideButton();
						break;
					}
				}
			}
		}).showPopover();
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

	private void onClickTweetButton() {
		// 投稿
		TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
		StatusesService statusesService = twitterApiClient.getStatusesService();

		String message = DateFormat.format("yyyy/MM/dd kk:mm:ss", Calendar.getInstance()).toString() +"てすとです";
		Call<Tweet> tweet = statusesService.update(message, null, false, null, null, null, false, null, null);
		tweet.enqueue(new Callback<Tweet>() {
			@Override
			public void success(Result<Tweet> result) {
				MYLogUtil.showToast("tweet success");
			}

			@Override
			public void failure(TwitterException exception) {
				MYLogUtil.showErrorToast("Tweet error");
			}
		});
		MYLogUtil.outputLog("");

		// ログイン
		Intent intent = new Intent();
		intent.setClassName(getContext().getPackageName(), "jp.co.my.myplatform.activity.controller.PLMainActivity");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PLMainActivity.KEY_LOGIN_TWITTER, true);
		getContext().startActivity(intent);

		PLCoreService.getNavigationController().hideNavigationIfNeeded();
	}
}
