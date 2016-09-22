package jp.co.my.myplatform.service.browser;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.content.PLHomeView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;

public class PLBrowserView extends PLContentView {
	private PLWebView mCurrentWebView;
	private ImageButton mBackButton, mForwardButton, mShowButton;
	private ProgressBar mProgressBar1, mProgressBar2;
	private LinearLayout mToolbar;

	public PLBrowserView() {
		super();
		setKeepCache(true);

		LayoutInflater.from(getContext()).inflate(R.layout.content_browser, this);
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mForwardButton = (ImageButton) findViewById(R.id.forward_button);
		mShowButton = (ImageButton) findViewById(R.id.show_toolbar_button);
		mProgressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		mCurrentWebView = (PLWebView) findViewById(R.id.su_web_view);
		mToolbar = (LinearLayout) findViewById(R.id.browser_toolbar);
		initButtonEvent();

		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (mCurrentWebView == null) {
					return;
				}
				updateProgressBar(mProgressBar1, progress);
				updateProgressBar(mProgressBar2, progress);
				updateArrowButtonImage();

				if (progress == 100) {
					finishLoadPage();
				}
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
			setKeepCache(false);
			PLCoreService.getNavigationController().pushView(PLHomeView.class);
			PLCoreService.getNavigationController().pushView(PLBrowserView.class);
		}
	}

	protected void loadFirstPage() {
		PLWebPageModel bookmark = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.querySingle();
			if (bookmark == null) {
			mCurrentWebView.loadUrl("http://news.yahoo.co.jp/");
		} else {
			mCurrentWebView.loadUrl(bookmark.getUrl());
		}
	}

	protected void finishLoadPage() {
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.queryList();
		PLWebPageModel model = new PLWebPageModel(mCurrentWebView.getTitle(), mCurrentWebView.getUrl(), null);
		model.save();
		for (PLWebPageModel pageModel : pageArray) {
			pageModel.delete();
		}
	}

	protected boolean onBackKey() {
		if (removeTopPopover()) {
			// 子ビューがあったときのみWebView戻る処理を行わない
			return true;
		} if (mCurrentWebView.canGoBack()) {
			mCurrentWebView.goBack();
			updateArrowButtonImage();
			return true;
		}
		return false;
	}
	private void onForwardKey() {
		mCurrentWebView.goForward();
		updateArrowButtonImage();
	}

	private void updateArrowButtonImage() {
		if (mCurrentWebView.canGoBack()) {
			mBackButton.setBackgroundResource(R.drawable.back_arrow_on);
		} else {
			mBackButton.setBackgroundResource(R.drawable.back_arrow_off);
		}
		if (mCurrentWebView.canGoForward()) {
			mForwardButton.setBackgroundResource(R.drawable.forward_arrow_on);
		} else {
			mForwardButton.setBackgroundResource(R.drawable.forward_arrow_off);
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
		mCurrentWebView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					onBackKey();
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
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onForwardKey();
			}
		});
		findViewById(R.id.hide_toolbar_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbar.setVisibility(View.GONE);
				mShowButton.setVisibility(View.VISIBLE);
			}
		});
		findViewById(R.id.bookmark_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLBookmarkList(PLBrowserView.this).showPopover(new PLRelativeLayoutController(v));
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
}
