package jp.co.my.myplatform.service.news;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.browser.PLWebView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsBrowserView extends PLBrowserView {

	private PLNewsPageModel mPageModel;

	@SuppressLint("SetJavaScriptEnabled")
	public PLNewsBrowserView(PLNewsPageModel page) {
		super();
		setKeepCache(false);
		mPageModel = page;

		PLWebView webView = getCurrentWebView();
		webView.loadUrl(mPageModel.getUrl());

		PLNewsSiteModel site = page.getSiteForeign().load();
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

	@Override
	protected void loadFirstPage() {
		// PageModelをセット後にloadする
	}

	@Override
	protected void finishLoadPage() {
		// 最終ページを保存しない
	}

	@Override
	protected boolean onBackKey() {
		if (super.onBackKey()) {
			return true;
		}
		PLCoreService.getNavigationController().pushView(PLNewsPagerView.class);
		return true;
	}
}
