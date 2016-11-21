package jp.co.my.myplatform.service.overlay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.content.PLHomeView;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLNavigationController extends PLOverlayView {

	private FrameLayout mFrameLayout;
	private PLContentView mCurrentView;
	private ArrayList<PLContentView> mViewCache;

	public PLNavigationController() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_navigation_controller, this);
		mFrameLayout = (FrameLayout) findViewById(R.id.content_frame);

		findViewById(R.id.space_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getOverlayManager().removeOverlayView(PLNavigationController.this);
			}
		});
		findViewById(R.id.home_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (1 < mViewCache.size()) {
					goBackView();
				}
			}
		});
		findViewById(R.id.home_button).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PLCoreService.getNavigationController().pushView(PLHomeView.class);
				return true;
			}
		});

		mViewCache = new ArrayList<>();

		pushView(PLHomeView.class);
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		return getBaseParamsForNavigationView();
	}

	@SuppressWarnings("unchecked")
	public <T extends PLContentView> T pushView(Class<T> clazz) {
		if (clazz.isInstance(mCurrentView)) {
			// 前回のViewを表示
			return (T) mCurrentView;
		}

		T view = getContentView(clazz);
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
		return pushView(view);
	}

	public <T extends PLContentView> T pushView(T view) {
		mViewCache.add(view);
		changeCurrentView(view);
		return (T) view;
	}

	public void goBackView() {
		int count = mViewCache.size();
		if (count <= 1) {
			MYLogUtil.showErrorToast("戻り先なし count=" +count);
			return;
		}
		PLContentView prevView = mViewCache.get(count - 2);
		mViewCache.remove(count - 1);
		if (!mViewCache.contains(mCurrentView)) {
			mCurrentView.viewWillDisappear();
		}
		changeCurrentView(prevView);
	}

	@SuppressWarnings("unchecked")
	public <T extends PLContentView> T getContentView(Class<T> clazz) {
		for (PLContentView view : mViewCache) {
			if (clazz.isInstance(view)) {
				return (T) view;
			}
		}
		return null;
	}

	public boolean displayNavigationIfNeeded() {
		if (getParent() != null) {
			return false;
		}
		PLOverlayManager manager = PLCoreService.getOverlayManager();
		manager.addOverlayView(this);
		return true;
	}

	public void hideNavigationIfNeeded() {
		if (getParent() == null) {
			return;
		}
		PLCoreService.getOverlayManager().removeOverlayView(this);
	}

	public void destroyNavigation() {
		for (PLContentView view : mViewCache) {
			view.viewWillDisappear();
		}
		mFrameLayout.removeAllViews();
		removeAllViews();
		mCurrentView = null;

		if (getParent() != null) {
			PLCoreService.getOverlayManager().removeOverlayView(this);
		}
	}

	public PLContentView getCurrentView() {
		return mCurrentView;
	}

	private void changeCurrentView(PLContentView view) {
		// フォーカスが前のViewに残らないように移動
		view.requestFocus();
		if (mCurrentView != null) {
			mFrameLayout.removeAllViews();
			mCurrentView = null;
		}

		mFrameLayout.addView(view, createMatchParams());
		mCurrentView = view;
	}
}
