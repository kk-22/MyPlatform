package jp.co.my.myplatform.browser;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.popover.PLActionListPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.view.PLFlickGestureRegistrant;

public class PLBaseBrowserContent extends PLContentView implements PLActionListPopover.PLActionListListener<PLWebPageModel> {
	private final String[] DISABLE_URLS = {"https://drive.google.com/"
			, "https://docs.google.com/spreadsheets/", "https://docs.google.com/document/"};

	private PLWebView mCurrentWebView;
	private ImageButton mBackButton, mForwardButton, mShowButton;
	private ProgressBar mProgressBar;
	private LinearLayout mToolbar;

	public PLBaseBrowserContent() {
		super();

		LayoutInflater.from(getContext()).inflate(R.layout.content_browser, this);
		mBackButton = findViewById(R.id.back_button);
		mForwardButton = findViewById(R.id.forward_button);
		mShowButton = findViewById(R.id.show_toolbar_button);
		mProgressBar = findViewById(R.id.progressBar);
		mCurrentWebView = findViewById(R.id.su_web_view);
		mToolbar = findViewById(R.id.browser_toolbar);
		initButtonEvent();

		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (mCurrentWebView != null) {
					updateProgressBar(mProgressBar, progress);
					updateArrowButtonImage();
				}
			}
		});
		mCurrentWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				willChangePage("Loading page", url, false);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				willChangePage(view.getTitle(), url, true);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				willLoadPage(url);
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
	}

	@Override
	public int getNavigationButtonVisibility() {
		return VISIBLE;
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		if (mCurrentWebView != null) {
			mCurrentWebView.destroy();
			mCurrentWebView = null;
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mCurrentWebView == null) {
			MYLogUtil.showErrorToast("viewが開放済み");
			PLCoreService.getNavigationController().popView();
		}
	}

	public void hideToolbar() {
		mToolbar.setVisibility(View.GONE);
		mShowButton.setVisibility(View.VISIBLE);
	}

	protected void willLoadPage(String url) {
		// サブクラスで実装
	}

	protected void willChangePage(String title, String url, boolean isFinished) {
		// サブクラスで実装
	}

	@Override
	public boolean onBackKey() {
		if (removeTopPopover()) {
			// 子ビューがあったときのみWebView戻る処理を行わない
			return true;
		} if (canGoHistory(true)) {
			goHistory(true);
			return true;
		}
		PLCoreService.getNavigationController().popView();
		return true;
	}

	protected boolean canGoHistory(boolean isBack) {
		if (isBack) {
			return mCurrentWebView.canGoBack();
		}
		return mCurrentWebView.canGoForward();
	}

	protected void goHistory(boolean isBack) {
		if (isBack) {
			mCurrentWebView.goBack();
		} else {
			mCurrentWebView.goForward();
		}
	}

	private void onForwardKey() {
		if (canGoHistory(false)) {
			goHistory(false);
		}
	}

	protected void updateArrowButtonImage() {
		boolean backTag = Boolean.valueOf((String)mBackButton.getTag());
		if (canGoHistory(true) != backTag) {
			if (backTag) {
				mBackButton.setBackgroundResource(R.drawable.back_arrow_off);
			} else {
				mBackButton.setBackgroundResource(R.drawable.back_arrow_on);
			}
			mBackButton.setTag(String.valueOf(!backTag));
		}
		boolean forwardTag = Boolean.valueOf((String)mForwardButton.getTag());
		if (canGoHistory(false) != forwardTag) {
			if (forwardTag) {
				mForwardButton.setBackgroundResource(R.drawable.forward_arrow_off);
			} else {
				mForwardButton.setBackgroundResource(R.drawable.forward_arrow_on);
			}
			mForwardButton.setTag(String.valueOf(!forwardTag));
		}
	}

	private void updateProgressBar(ProgressBar progressBar, int progress) {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(progress);
		if (progress == 100) {
			progressBar.setVisibility(View.GONE);
		}
	}

	private void initButtonEvent() {
		setOnKeyListener(this);
		mCurrentWebView.setOnKeyListener(this);

		new PLFlickGestureRegistrant(getContext(), mCurrentWebView, new PLFlickGestureRegistrant.PLFlickGestureListener() {
			@Override
			public void flickToRight() {
				onBackKey();
			}

			@Override
			public void flickToLeft() {
				onForwardKey();
			}

			@Override
			public boolean cancelFlickEvent(int direction) {
				String currentUrl = mCurrentWebView.getUrl();
				for (String url: DISABLE_URLS) {
					if (currentUrl.startsWith(url)) {
						// スクロールを使用するサイトを除く
						return true;
					}
				}
				if (mCurrentWebView.canScrollHorizontally(direction)) {
					// ページ内の横スクロールが可能
					return true;
				}
				return false;
			}
		});

		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackKey();
			}
		});
		mBackButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showHistoryList();
				return true;
			}
		});
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onForwardKey();
			}
		});
		mForwardButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showHistoryList();
				return true;
			}
		});
		findViewById(R.id.down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int currentY = mCurrentWebView.getScrollY();
				int moveY = (int)(mCurrentWebView.getHeight() * 0.7);
				mCurrentWebView.setScrollY(currentY + moveY);
			}
		});
		findViewById(R.id.down_button).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				hideToolbar();
				return true;
			}
		});
		findViewById(R.id.bookmark_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
						.where(PLWebPageModel_Table.bookmarkDirectoryNo.greaterThanOrEq(PLWebPageModel.BOOKMARK_DIRECTORY_NO_ROOT))
						.queryList();
				List<String> titleArray = new ArrayList<>();
				for (PLWebPageModel model : pageArray) {
					titleArray.add(model.getTitle());
				}
				new PLActionListPopover<>(titleArray, pageArray, PLBaseBrowserContent.this).showPopover(new PLRelativeLayoutController(v));
			}
		});
		findViewById(R.id.function_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLBrowserFunctionList(PLBaseBrowserContent.this).showPopover(new PLRelativeLayoutController(v));
			}
		});
		findViewById(R.id.show_toolbar_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbar.setVisibility(View.VISIBLE);
				mShowButton.setVisibility(View.GONE);
			}
		});
	}

	private void showHistoryList() {
		new PLListPopover(historyTitles(), new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				loadHistoryOfIndex(position);
			}
		}).setMatchWidth().showPopover(new PLRelativeLayoutController(mBackButton));
	}

	String[] historyTitles() {
		WebBackForwardList list = mCurrentWebView.copyBackForwardList() ;
		int size = list.getSize();
		int currentIndex = list.getCurrentIndex();
		String[] titles = new String[size];
		for (int i = 0; i < size; i++) {
			String title = list.getItemAtIndex(i).getTitle();
			if (i == currentIndex) {
				titles[i] = "■＞" +title;
			} else {
				titles[i] = title;
			}
		}
		return titles;
	}

	void loadHistoryOfIndex(int index) {
		WebBackForwardList list = mCurrentWebView.copyBackForwardList() ;
		int step = index - list.getCurrentIndex();
		mCurrentWebView.goBackOrForward(step);
	}


	public PLWebView getCurrentWebView() {
		return mCurrentWebView;
	}

	protected LinearLayout getToolbar() {
		return mToolbar;
	}

	// PLActionListListener of bookmark list
	@Override
	public void onItemClick(PLWebPageModel object, PLActionListPopover listPopover) {
		getCurrentWebView().loadPageModel(object);
		listPopover.removeFromContentView();
	}

	@Override
	public void onActionClick(final PLWebPageModel object, final PLActionListPopover<PLWebPageModel> listPopover, View buttonView) {
		String[] titles = {"編集", "移動", "削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case 1: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case 2: {
						MYLogUtil.showToast("ブックマーク削除：" +object.getTitle());
						object.delete();
						listPopover.removeObject(object);
						// アクションPopoverだけ削除してリストは残す
						PLBaseBrowserContent.this.removeTopPopover();
						break;
					}
				}
			}
		}).showPopover(new PLRelativeLayoutController(buttonView));
	}
}
