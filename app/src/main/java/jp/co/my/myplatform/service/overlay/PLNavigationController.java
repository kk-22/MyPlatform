package jp.co.my.myplatform.service.overlay;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.content.PLHomeView;
import jp.co.my.myplatform.service.core.PLCoreService;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

public class PLNavigationController extends PLOverlayView {

	private FrameLayout mContentFrameLayout;
	private FrameLayout mBottomFrame;
	private FrameLayout mNaviBarFrame;
	private ViewGroup mCustomizeNavigationBar;
	private Button mBackButton;
	private Button mNavigationButton;
	private PLContentView mCurrentView;
	private ArrayList<PLContentView> mViewCache;
	private Handler mMainHandler;

	// ハーフモード用
	private boolean mIsHalf;
	private int mGravity;

	public PLNavigationController() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_navigation_controller, this);
		mContentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
		mBackButton = (Button) findViewById(R.id.back_button);
		mNavigationButton = (Button) findViewById(R.id.navigation_button);
		mBottomFrame = (FrameLayout) findViewById(R.id.bottom_frame);
		mNaviBarFrame = (FrameLayout) findViewById(R.id.customize_navigation_layout);

		findViewById(R.id.space_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getOverlayManager().removeOverlayView(PLNavigationController.this);
			}
		});
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popView();
			}
		});
		mBackButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PLCoreService.getNavigationController().pushView(PLHomeView.class);
				return true;
			}
		});
		mNavigationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYViewUtil.toggleVisibility(mBottomFrame, true);
			}
		});

		mViewCache = new ArrayList<>();
		mMainHandler = new Handler();

		pushView(PLHomeView.class);
	}

	@Override
	public void viewWillRemove() {
		// フロントアプリにフォーカスを渡すために外す
		WindowManager.LayoutParams params = getOverlayParams();
		params.height = 0;
		params.width = 0;
		params.flags = FLAG_NOT_FOCUSABLE;
		PLCoreService.getOverlayManager().updateOverlayLayout(this, params);
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		WindowManager.LayoutParams params = getBaseParamsForNavigationView();
		if (mIsHalf) {
			params.height = MYViewUtil.getDisplaySize(getContext()).y / 2;
			params.flags = FLAG_NOT_FOCUSABLE;
			mNavigationButton.setVisibility(VISIBLE);
			mBottomFrame.setVisibility(GONE);
		} else {
			mNavigationButton.setVisibility(GONE);
			mBottomFrame.setVisibility(VISIBLE);
		}
		params.gravity = mGravity;
		return params;
	}

	public <T extends PLContentView> void pushInMainThread(final Class<T> clazz) {
		MYOtherUtil.runOnUiThread(PLCoreService.getContext(), mMainHandler, new Runnable() {
			@Override
			public void run() {
				pushView(clazz);
			}
		});
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
		updateBackButton();
		return (T) view;
	}

	public void popView() {
		int count = mViewCache.size();
		if (count <= 1) {
			MYLogUtil.showErrorToast("戻り先なし count=" + count);
			return;
		}
		PLContentView prevView = mViewCache.get(count - 2);
		mViewCache.remove(count - 1);
		if (!mViewCache.contains(mCurrentView)) {
			mCurrentView.viewWillDisappear();
		}
		changeCurrentView(prevView);
		updateBackButton();
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
		mContentFrameLayout.removeAllViews();
		removeAllViews();
		mCurrentView = null;

		if (getParent() != null) {
			PLCoreService.getOverlayManager().removeOverlayView(this);
		}
	}

	public void putNavigationBar(ViewGroup navigationBar) {
		if (mCustomizeNavigationBar != null) {
			mNaviBarFrame.removeView(mCustomizeNavigationBar);
		}

		mCustomizeNavigationBar = navigationBar;
		if (navigationBar != null) {
			mNaviBarFrame.addView(navigationBar);
		}
	}

	public void resizeNavigation(boolean isHalf, boolean isBottom) {
		mIsHalf = isHalf;
		if (isBottom) {
			mGravity = Gravity.BOTTOM;
		} else {
			mGravity = Gravity.TOP;
		}
		PLCoreService.getOverlayManager().updateOverlayLayout(this, getOverlayParams());
	}

	private void changeCurrentView(PLContentView view) {
		// フォーカスが前のViewに残らないように移動
		view.requestFocus();
		if (mCurrentView != null) {
			mContentFrameLayout.removeAllViews();
			mCurrentView = null;
		}

		mContentFrameLayout.addView(view, createMatchParams());
		mCurrentView = view;

		putNavigationBar(view.getNavigationBar());
	}

	private void updateBackButton() {
		mBackButton.setEnabled(mViewCache.size() > 1);
	}

	// getter
	public PLContentView getCurrentView() {
		return mCurrentView;
	}
}
