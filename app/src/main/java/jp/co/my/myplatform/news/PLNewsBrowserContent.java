package jp.co.my.myplatform.news;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.webkit.WebSettings;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.browser.PLBaseBrowserContent;
import jp.co.my.myplatform.browser.PLWebView;

public class PLNewsBrowserContent extends PLBaseBrowserContent {

	private PLNewsPageModel mPageModel;

	@SuppressLint("SetJavaScriptEnabled")
	public PLNewsBrowserContent(PLNewsPageModel page) {
		super();
		mPageModel = page;

		loadFirstPage();
		customizeDesign();
	}

	private void loadFirstPage() {
		PLWebView webView = getCurrentWebView();
		webView.loadUrl(mPageModel.getUrl());

		PLNewsSiteModel site = mPageModel.getSiteForeign().load();
		if (site == null) {
			MYLogUtil.showErrorToast("site is null");
			return;
		}
		WebSettings setting = webView.getSettings();
		setting.setJavaScriptEnabled(site.isEnableScript());
		if (site.isEnablePCViewer()) {
			setting.setUserAgentString("MyUserAgent");
		}
	}

	private void customizeDesign() {
		getToolbar().setBackgroundColor(Color.parseColor("#0068B7"));
	}
}
