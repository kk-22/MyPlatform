package jp.co.my.myplatform.service.browser;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;

public class PLBrowserView extends PLContentView {
	private PLWebView mCurrentWebView;
	private ImageButton mBackButton, mForwardButton, mShowButton;
	private ProgressBar mProgressBar;
	private LinearLayout mToolbar;

	public PLBrowserView() {
		super();

		LayoutInflater.from(getContext()).inflate(R.layout.content_browser, this);
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mForwardButton = (ImageButton) findViewById(R.id.forward_button);
		mShowButton = (ImageButton) findViewById(R.id.show_toolbar_button);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mCurrentWebView = (PLWebView) findViewById(R.id.su_web_view);
		mToolbar = (LinearLayout) findViewById(R.id.browser_toolbar);
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
				saveLastPage("Loading page", url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				saveLastPage(view.getTitle(), url);
			}
		});
		loadFirstPage();
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
			PLCoreService.getNavigationController().goBackView();
		}
	}

	public void hideToolbar() {
		mToolbar.setVisibility(View.GONE);
		mShowButton.setVisibility(View.VISIBLE);
	}

	protected void loadFirstPage() {
		PLModelContainer<PLWebPageModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.limit(1));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLWebPageModel>() {
			@Override
			public void onLoad(List<PLWebPageModel> modelLists) {
				if (modelLists.size() == 0) {
					mCurrentWebView.loadUrl("http://news.yahoo.co.jp/");
					return;
				}
				mCurrentWebView.loadUrl(modelLists.get(0).getUrl());
			}
		});
	}

	protected void saveLastPage(String title, String url) {
		// TODO: 前のモデルを使いまわすべき
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.queryList();
		PLWebPageModel model = new PLWebPageModel(title, url, null);
		model.save();
		for (PLWebPageModel pageModel : pageArray) {
			pageModel.delete();
		}
	}

	@Override
	public boolean onBackKey() {
		if (removeTopPopover()) {
			// 子ビューがあったときのみWebView戻る処理を行わない
			return true;
		} if (mCurrentWebView.canGoBack()) {
			mCurrentWebView.goBack();
			return true;
		}
		PLCoreService.getNavigationController().goBackView();
		return true;
	}
	private void onForwardKey() {
		mCurrentWebView.goForward();
	}

	private void updateArrowButtonImage() {
		boolean backTag = Boolean.valueOf((String)mBackButton.getTag());
		if (mCurrentWebView.canGoBack() != backTag) {
			if (backTag) {
				mBackButton.setBackgroundResource(R.drawable.back_arrow_off);
			} else {
				mBackButton.setBackgroundResource(R.drawable.back_arrow_on);
			}
			mBackButton.setTag(String.valueOf(!backTag));
		}
		boolean forwarTag = Boolean.valueOf((String)mForwardButton.getTag());
		if (mCurrentWebView.canGoForward() != forwarTag) {
			if (forwarTag) {
				mForwardButton.setBackgroundResource(R.drawable.forward_arrow_off);
			} else {
				mForwardButton.setBackgroundResource(R.drawable.forward_arrow_on);
			}
			mForwardButton.setTag(String.valueOf(!forwarTag));
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

		mCurrentWebView.setListener(new PLWebView.PLWebViewGestureListener() {
			@Override
			public void swipeToRight() {
				onBackKey();
			}

			@Override
			public void swipeToLeft() {
				onForwardKey();
			}
		});

		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackKey();
			}
		});
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onForwardKey();
			}
		});
		findViewById(R.id.stop_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentWebView.stopLoading();
				updateArrowButtonImage();
			}
		});
		findViewById(R.id.bookmark_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLActionListPopover(PLBrowserView.this).showPopover(new PLRelativeLayoutController(v));
			}
		});
		findViewById(R.id.function_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLBrowserFunctionList(PLBrowserView.this).showPopover(new PLRelativeLayoutController(v));
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

	public PLWebView getCurrentWebView() {
		return mCurrentWebView;
	}

	protected LinearLayout getToolbar() {
		return mToolbar;
	}
}
