package jp.co.my.myplatform.service.news;

import jp.co.my.myplatform.service.browser.PLBrowserView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLNewsPageModel;

public class PLNewsBrowserView extends PLBrowserView {

	private PLNewsPageModel mPageModel;

	public PLNewsBrowserView(PLNewsPageModel page) {
		super();
		setKeepCache(false);
		mPageModel = page;

		getCurrentWebView().loadUrl(mPageModel.getUrl());
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
