package jp.co.my.myplatform.service.navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.overlay.PLOverlayManager;
import jp.co.my.myplatform.service.overlay.PLOverlayView;

public class PLNavigationController extends PLOverlayView {

	private FrameLayout mFrameLayout;
	private PLNavigationView mCurrentView;
	private ArrayList<PLNavigationView> mViewCache;

	public PLNavigationController() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_navigation_controller, this);
		mFrameLayout = (FrameLayout) findViewById(R.id.navigation_frame);

		findViewById(R.id.space_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getOverlayManager().removeOverlayView(PLNavigationController.this);
			}
		});
		findViewById(R.id.home_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLHomeView.class);
			}
		});

		mViewCache = new ArrayList<>();

		pushView(PLHomeView.class);
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		return getBaseParamsForFullView();
	}

	@SuppressWarnings("unchecked")
	public <T extends PLNavigationView> T pushView(Class<T> clazz) {
		displayNavigationIfNeeded();
		if (clazz.isInstance(mCurrentView)) {
			// 前回のViewを表示
			return (T) mCurrentView;
		}

		PLNavigationView view = getNavigationView(clazz);
		if (view == null) {
			// インスタンス作成
			try {
				String className = clazz.getName();
				view = (T) Class.forName(className).getConstructor().newInstance();
			} catch (Exception e) {
				MYLogUtil.showExceptionToast(e);
				return null;
			}
		}

		if (mCurrentView != null) {
			if (!mCurrentView.isKeepCache) {
				mCurrentView.viewWillDisappear();
				mViewCache.remove(mCurrentView);
			}
			mFrameLayout.removeAllViews();
			mCurrentView = null;
		}
		mViewCache.add(view);
		mFrameLayout.addView(view, createMatchParams());
		mCurrentView = view;
		return (T) view;
	}

	@SuppressWarnings("unchecked")
	public <T extends PLNavigationView> T getNavigationView(Class<T> clazz) {
		for (PLNavigationView view : mViewCache) {
			if (clazz.isInstance(view)) {
				return (T) view;
			}
		}
		return null;
	}

	public void displayNavigationIfNeeded() {
		if (getParent() != null) {
			return;
		}
		PLOverlayManager manager = PLCoreService.getOverlayManager();
		manager.addOverlayView(this);
	}

	public void hideNavigationIfNeeded() {
		if (getParent() == null) {
			return;
		}
		PLCoreService.getOverlayManager().removeOverlayView(this);
	}

	public void destroyNavigation() {
		for (PLNavigationView view : mViewCache) {
			view.viewWillDisappear();
		}
		mFrameLayout.removeAllViews();
		removeAllViews();
		mCurrentView = null;

		if (getParent() != null) {
			PLCoreService.getOverlayManager().removeOverlayView(this);
		}
	}
}
