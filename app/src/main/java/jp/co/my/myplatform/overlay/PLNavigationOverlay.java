package jp.co.my.myplatform.overlay;

import android.content.SharedPreferences;
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
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.content.PLHomeContent;
import jp.co.my.myplatform.core.PLCoreService;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

public class PLNavigationOverlay extends PLOverlayView {

	private static final String KEY_NAVIGATION_VISIBLE = "KEY_NAVIGATION_VISIBLE";

	public enum BarType {
		BOTTOM,
		TOP
	}

	private View mStatusBar;
	private FrameLayout mContentFrameLayout;
	private FrameLayout mBarFrame;
	private FrameLayout mNaviBarFrame;
	private ViewGroup mCustomizeNavigationBar;
	private Button mBackButton;
	private Button mNavigationButton;
	private PLContentView mCurrentView;
	private ArrayList<PLContentView> mViewCache;
	private Handler mMainHandler;
	private BarType mCurrentBarType; // mBarFrameの現在位置

	// ハーフモード用
	private boolean mIsHalf;
	private int mGravity;

	public PLNavigationOverlay() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_navigation_controller, this);
		mStatusBar = findViewById(R.id.status_bar_view);
		mContentFrameLayout = findViewById(R.id.content_frame);
		mBackButton = findViewById(R.id.back_button);
		mNavigationButton = findViewById(R.id.navigation_button);
		mBarFrame = findViewById(R.id.bar_frame);
		mNaviBarFrame = findViewById(R.id.customize_navigation_layout);

		findViewById(R.id.space_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getOverlayManager().removeOverlayView(PLNavigationOverlay.this);
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
				PLCoreService.getNavigationController().pushView(PLHomeContent.class);
				return true;
			}
		});
		mNavigationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYViewUtil.toggleVisibility(mBarFrame, true);
			}
		});
		mNavigationButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mIsHalf = !mIsHalf;
				updateLayout();
				return true;
			}
		});

		mViewCache = new ArrayList<>();
		mMainHandler = new Handler();
		mGravity = Gravity.TOP;
		mCurrentBarType = BarType.BOTTOM;
		if (MYLogUtil.getPreference().getBoolean(KEY_NAVIGATION_VISIBLE, false)) {
			mNavigationButton.setVisibility(VISIBLE);
		}

		pushView(PLHomeContent.class);
	}

	@Override
	public void viewWillRemove() {
		PLCoreService.getOverlayManager().clearFocus(this);
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		WindowManager.LayoutParams params = getBaseParamsForNavigationView();
		if (mIsHalf) {
			params.height = MYViewUtil.getDisplaySize(getContext()).y / 2;
			params.flags = FLAG_NOT_FOCUSABLE;
		}
		params.gravity = mGravity;
		return params;
	}

	@Override
	protected void updateLayout() {
		if (mIsHalf) {
			mStatusBar.setVisibility(GONE);
			mBarFrame.setVisibility(GONE);
		} else {
			mStatusBar.setVisibility(mCurrentView.getStatusBarVisibility());
			mBarFrame.setVisibility(VISIBLE);
		}
		super.updateLayout();
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
		} else if (clazz != PLHomeContent.class) {
			// 履歴の重複を無くす
			int index = mViewCache.indexOf(view);
			int nextIndex = index + 1;
			if (nextIndex > 0 && mViewCache.get(nextIndex) instanceof PLHomeContent) {
				// 2連続Homeになるのを防ぐ
				mViewCache.remove(nextIndex);
			}
			mViewCache.remove(view);
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
		updateLayout();
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

		mStatusBar.setVisibility(view.getStatusBarVisibility());

		mNavigationButton.setVisibility(view.getNavigationButtonVisibility());
		putNavigationBar(view.getNavigationBar());

		BarType nextType = view.getBarType();
		if (mCurrentBarType != nextType) {
			switch (nextType) {
				case BOTTOM:
					MYViewUtil.addViewAgain(mBarFrame);
					break;
				case TOP:
					MYViewUtil.addViewAgain(findViewById(R.id.main_frame));
					break;
			}
			mCurrentBarType = nextType;
		}
	}

	private void updateBackButton() {
		mBackButton.setEnabled(mViewCache.size() > 1);
	}

	// getter
	public PLContentView getCurrentView() {
		return mCurrentView;
	}

	public boolean isHalf() {
		return mIsHalf;
	}

	public void setNavigationButtonVisibility(int visibility) {
		mNavigationButton.setVisibility(visibility);
		boolean isVisible = (visibility == VISIBLE);
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putBoolean(KEY_NAVIGATION_VISIBLE, isVisible);
		editor.apply();
	}
}
