package jp.co.my.myplatform.service.browser;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.model.PLWebPageModel;

public class PLWebView extends WebView {

	private GestureDetector mGestureDetector;

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

		mGestureDetector = new GestureDetector(context, mOnGestureListener);
		initWebView();
	}

//	public void releaseWebView() {
//		mCurrentWebView.setWebChromeClient(null);
//		mCurrentWebView.setWebViewClient(null);
////		unregisterForContextMenu(mCurrentWebView);
//		mCurrentWebView.destroy();
//		mCurrentWebView = null;
//	}

	@Override
	protected void onFinishInflate() {
		MYLogUtil.outputLog("onFinishInflate");
		super.onFinishInflate();
	}

	@Override
	protected void onAttachedToWindow() {
		MYLogUtil.outputLog("onAttachedToWindow");
		super.onAttachedToWindow();

//		if (mCurrentWebView != null)	{
//			mCurrentWebView.onPause();
//			mCurrentWebView.pauseTimers();
//		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		MYLogUtil.outputLog("onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		MYLogUtil.outputLog("onLayout");
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onDetachedFromWindow() {
		MYLogUtil.outputLog("onDetachedFromWindow");
		super.onDetachedFromWindow();

//		if (mCurrentWebView != null)	{
//			mCurrentWebView.onResume();
//			mCurrentWebView.resumeTimers();
//		}
	}

	public void loadPageModel(PLWebPageModel pageModel) {
		WebSettings setting = getSettings();
		setting.setJavaScriptEnabled(pageModel.isEnableScript());
		loadUrl(pageModel.getUrl());
	}

	private void initWebView() {
		WebSettings setting = getSettings();
		setting.setDisplayZoomControls(false);	// ズームコントローラ非表示
		setting.setBuiltInZoomControls(true);	// ピンチズーム
		setting.setLoadWithOverviewMode(true);	// ページが画面に収まるように自動で縮小
		setting.setUseWideViewPort(true);		// ページサイズが画面サイズ以上なら縮小
		setting.setJavaScriptEnabled(true);
//		if (mPageData.getSiteData().isEnablePCViewr()) {
//			setting.setUserAgentString("MyUserAgent");
//		}

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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return (mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event));
	}

	private boolean canLoadUrl(String url) {
		if (url.startsWith("http:") || url.startsWith("https:")) {
			// WebView内で開く
			return false;
		}
		MYLogUtil.showToast("リンクストップ\n" + url);
		return true;
	}

	// タッチイベントのリスナー
	private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
		private final String[] DISABLE_URLS = {"https://drive.google.com/"
				, "https://docs.google.com/spreadsheets/", "https://docs.google.com/document/"};
		private static final int DIRECTION_RIGHT		= -1;
		private static final int DIRECTION_LEFT		= 1;
		// 右スワイプ
		private static final int MIN_DISTANCE_RIGHT		= 250;
		private static final int MIN_SPEED_RIGHT		= 3000;
		// 左スワイプ
		private static final int MIN_DISTANCE_LEFT		= 150;
		private static final int MIN_SPEED_LEFT			= 3000;
		// Y軸の許容移動距離
		private static final int MAX_Y_DISTANCE			= 150;

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			if (Math.abs(event1.getY() - event2.getY()) > MAX_Y_DISTANCE) {
				// 縦のスクロール
				return false;
			}
			// スクロールを使用するサイトを除く
			String currentUrl = PLWebView.this.getUrl();
			for (String url: DISABLE_URLS) {
				if (currentUrl.startsWith(url)) {
					return false;
				}
			}

			int direction, minDistance, minSpeed;
			if ((event2.getX() - event1.getX()) > 0) {
				direction = DIRECTION_RIGHT;
				minDistance = MIN_DISTANCE_RIGHT;
				minSpeed = MIN_SPEED_RIGHT;
			} else {
				direction = DIRECTION_LEFT;
				minDistance = MIN_DISTANCE_LEFT;
				minSpeed = MIN_SPEED_LEFT;
			}

			if (PLWebView.this.canScrollHorizontally(direction)) {
				// ページ内スクロールが可能
				return false;
			}
			float distance = Math.abs((event1.getX() - event2.getX()));
			float speed = Math.abs(velocityX);
			//MYLogUtil.outputLog("横の移動距離：" + distance + " 横の移動スピード：" + speed);
			if (distance < minDistance || speed < minSpeed) {
				// 移動量・速度が足りない
				return false;
			}

			// ページ切り替え
			if (direction == DIRECTION_RIGHT) {
				if (PLWebView.this.canGoBack()) {
					PLWebView.this.goBack();
					MYLogUtil.showToast("前に戻る");
				} else {
					MYLogUtil.showToast("戻り先がありません");
				}
			} else {
				if (PLWebView.this.canGoForward()) {
					PLWebView.this.goForward();
					MYLogUtil.showToast("次に進む");
				} else {
					MYLogUtil.showToast("進み先がありません");
				}
			}
			return false;
		}
	};
}
