package jp.co.my.myplatform.overlay;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.content.PLHomeContent;
import jp.co.my.myplatform.core.PLCoreService;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

public class PLNavigationOverlay extends PLOverlayView {

	private static final String KEY_NAVIGATION_VISIBLE = "KEY_NAVIGATION_VISIBLE";
	private static final int MAX_NUMBER_OF_NAVIGATION_BAR_CHILD = 5; // 戻るボタンとスペースを含む最大数

	public enum BarType {
		BOTTOM,
		TOP
	}

	private View mStatusBar;
	private FrameLayout mContentFrameLayout;
	private PLContentView mCurrentView;
	private ArrayList<PLContentView> mViewCache;
	private Handler mMainHandler;
	private BarType mCurrentBarType; // mBarFrameの現在位置

	// ナビゲーションバー用
	private LinearLayout mNavigationBarLinear;
	private Space mNavigationSpace;
	private Button mBackButton;
	private Button mNavigationButton;

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
		mNavigationBarLinear = findViewById(R.id.navigation_bar_linear);
		mNavigationSpace = findViewById(R.id.navigation_space);

		mNavigationBarLinear.setOnClickListener(new OnClickListener() {
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
				MYViewUtil.toggleVisibility(mNavigationBarLinear, true);
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
			mNavigationBarLinear.setVisibility(GONE);
		} else {
			mStatusBar.setVisibility(mCurrentView.getStatusBarVisibility());
			mNavigationBarLinear.setVisibility(VISIBLE);
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
		// 戻るボタンと mNavigationSpace 以外を取り除く
		mNavigationBarLinear.removeViews(1, mNavigationBarLinear.getChildCount() - 2);
		if (view.getNavigationButtons() != null) {
			for (Button button : view.getNavigationButtons()) {
				showNavigationButton(button);
			}
		}
		updateNavigationSpace();

		BarType nextType = view.getBarType();
		if (mCurrentBarType != nextType) {
			// ナビゲーションバーの位置を変更
			ConstraintLayout layout = findViewById(R.id.navigation_root_constraint);
			ConstraintSet constraintSet = new ConstraintSet();
			constraintSet.clone(layout);
			switch (nextType) {
				case BOTTOM:
					constraintSet.connect(mStatusBar.getId(), ConstraintSet.BOTTOM, mContentFrameLayout.getId(), ConstraintSet.TOP);
					constraintSet.connect(mContentFrameLayout.getId(), ConstraintSet.TOP, mStatusBar.getId(), ConstraintSet.BOTTOM);
					constraintSet.connect(mContentFrameLayout.getId(), ConstraintSet.BOTTOM, mNavigationBarLinear.getId(), ConstraintSet.TOP);
					constraintSet.connect(mNavigationBarLinear.getId(), ConstraintSet.TOP, mContentFrameLayout.getId(), ConstraintSet.BOTTOM);
					constraintSet.connect(mNavigationBarLinear.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
					break;
				case TOP:
					constraintSet.connect(mStatusBar.getId(), ConstraintSet.BOTTOM, mNavigationBarLinear.getId(), ConstraintSet.TOP);
					constraintSet.connect(mNavigationBarLinear.getId(), ConstraintSet.TOP, mStatusBar.getId(), ConstraintSet.BOTTOM);
					constraintSet.connect(mNavigationBarLinear.getId(), ConstraintSet.BOTTOM, mContentFrameLayout.getId(), ConstraintSet.TOP);
					constraintSet.connect(mContentFrameLayout.getId(), ConstraintSet.TOP, mNavigationBarLinear.getId(), ConstraintSet.BOTTOM);
					constraintSet.connect(mContentFrameLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
					break;
			}
			constraintSet.applyTo(layout);
			mCurrentBarType = nextType;
		}
	}

	public void showNavigationButton(Button button) {
		// スペースの前に追加
		int childCount = mNavigationBarLinear.getChildCount();
		if (MAX_NUMBER_OF_NAVIGATION_BAR_CHILD <= childCount) {
			return;
		}
		mNavigationBarLinear.addView(button, childCount - 1, getNavigationButtonLayoutParams());
	}

	public void updateNavigationSpace() {
		LayoutParams params = (LayoutParams) mNavigationSpace.getLayoutParams();
		params.weight = MAX_NUMBER_OF_NAVIGATION_BAR_CHILD - mNavigationBarLinear.getChildCount() + 1;
	}

	private LinearLayout.LayoutParams getNavigationButtonLayoutParams() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, MATCH_PARENT);
		params.weight = 1;
		return params;
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
