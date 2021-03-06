package jp.co.my.myplatform.overlay;

import android.content.Context;
import android.view.WindowManager;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;

public class PLOverlayManager {

	private Context mContext;
	private WindowManager mWindowManager;
	private ArrayList<PLOverlayView> mOverlayViews;
	private Boolean mIsShowingFrontViews;

	public PLOverlayManager(Context context) {
		super();
		mContext = context;
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mOverlayViews = new ArrayList<>();
		mIsShowingFrontViews = false;
	}

	public void destroyOverlay() {
		while (0 < mOverlayViews.size()) {
			PLOverlayView view = mOverlayViews.get(0);
			removeOverlayView(view);
		}
	}

	public void addOverlayView(PLOverlayView view) {
		mOverlayViews.add(view);
		mWindowManager.addView(view, view.getOverlayParams());
	}

	public void removeOverlayView(PLOverlayView view) {
		if (mOverlayViews.remove(view)) {
			view.viewWillRemove();
			mWindowManager.removeView(view);
		} else {
			MYLogUtil.outputErrorLog("remove対象が存在しません" +view);
		}
	}

	public void removeOverlayView(Class clazz) {
		PLOverlayView view = getOverlayView(clazz);
		if (view != null) {
			removeOverlayView(view);
		}
	}

	public void clearFocus(PLOverlayView willRemoveView) {
		// フロントアプリにフォーカスを渡すために外す
		WindowManager.LayoutParams params = (WindowManager.LayoutParams) willRemoveView.getLayoutParams();
		params.height = 0;
		params.width = 0;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		updateOverlayLayout(willRemoveView, params);
	}

	@SuppressWarnings("unchecked")
	public <T extends PLOverlayView> T getOverlayView(Class<T> clazz) {
		for (PLOverlayView view : mOverlayViews) {
			if (clazz.isInstance(view)) {
				return (T)view;
			}
		}
		return null;
	}

	public <T extends PLOverlayView> void bringToFront(Class<T> clazz) {
		PLOverlayView view = getOverlayView(clazz);
		removeOverlayView(view);
		addOverlayView(view);
	}

	public void updateOverlayLayout(PLOverlayView view, WindowManager.LayoutParams nextParams) {
		mWindowManager.updateViewLayout(view, nextParams);
	}

	public void addFrontOverlays() {
		if (mIsShowingFrontViews) {
			return;
		}
		mIsShowingFrontViews = true;
		addOverlayView(new PLFrontButtonOverlay());
	}

	public void removeFrontOverlays() {
		removeOverlayView(PLFrontButtonOverlay.class);
		mIsShowingFrontViews = false;
	}
}
