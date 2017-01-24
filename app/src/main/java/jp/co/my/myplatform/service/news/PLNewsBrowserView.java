package jp.co.my.myplatform.service.news;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.webkit.WebSettings;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.browser.PLBaseBrowserView;
import jp.co.my.myplatform.service.browser.PLWebView;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsBrowserView extends PLBaseBrowserView {

	private PLNewsPageModel mPageModel;

	@SuppressLint("SetJavaScriptEnabled")
	public PLNewsBrowserView(PLNewsPageModel page) {
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
