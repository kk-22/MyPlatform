package jp.co.my.myplatform.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jp.co.my.common.util.MYLogUtil;

public class PLWebView extends WebView {

	public PLWebView(Context context) {
		this(context, null);
	}
	public PLWebView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	public PLWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) {
			// このクラスを使用するXMLレイアウトファイルでのエラー防止
			return;
		}

		initWebView();
	}

	@Override
	protected void onFinishInflate() {
		MYLogUtil.outputLog("onFinishInflate");
		super.onFinishInflate();
	}

	@Override
	protected void onAttachedToWindow() {
		MYLogUtil.outputLog("onAttachedToWindow");
		super.onAttachedToWindow();

		onResume();
		resumeTimers();
	}

	@Override
	protected void onDetachedFromWindow() {
		MYLogUtil.outputLog("onDetachedFromWindow");
		super.onDetachedFromWindow();

		onPause();
		pauseTimers();
		clearCache(false);
	}

	@Override
	public void destroy() {
		removeAllViews();

		stopLoading();
		setWebChromeClient(null);
		setWebViewClient(null);

		super.destroy();
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void loadPageModel(PLWebPageModel pageModel) {
		WebSettings setting = getSettings();
		setting.setJavaScriptEnabled(pageModel.isEnableScript());
		loadUrl(pageModel.getUrl());
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		WebSettings setting = getSettings();
		setting.setDisplayZoomControls(false);	// ズームコントローラ非表示
		setting.setBuiltInZoomControls(true);	// ピンチズーム
		setting.setLoadWithOverviewMode(true);	// ページが画面に収まるように自動で縮小
		setting.setUseWideViewPort(true);		// ページサイズが画面サイズ以上なら縮小
		setting.setJavaScriptEnabled(true);

		setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				String url = request.getUrl().toString();
				return canLoadUrl(url);
			}

			@Override
			@SuppressWarnings("deprecation")
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return canLoadUrl(url);
			}
		});
	}

	private boolean canLoadUrl(String url) {
		if (url.startsWith("http:") || url.startsWith("https:")) {
			// WebView内で開く
			return false;
		}
		MYLogUtil.showToast("リンクストップ\n" + url);
		return true;
	}
}
