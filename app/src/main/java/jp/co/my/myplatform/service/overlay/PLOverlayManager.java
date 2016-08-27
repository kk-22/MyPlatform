package jp.co.my.myplatform.service.overlay;

import android.content.Context;
import android.view.WindowManager;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.navigation.PLNavigationController;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLOverlayManager {

	private static PLOverlayManager sInstance;

	private Context mContext;
	private WindowManager mWindowManager;
	private PLNavigationController mNavigationController;
	private ArrayList<PLOverlayView> mOverlayViews;

	private PLOverlayManager(Context context) {
		super();
		mContext = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mOverlayViews = new ArrayList<>();
	}

	private void initFrontOverlays() {
		addOverlayView(new PLFrontButtonView());

		mNavigationController = new PLNavigationController();
	}

	public void removeAllView() {
		while (0 < mOverlayViews.size()) {
			PLOverlayView view = mOverlayViews.get(0);
			removeOverlayView(view);
		}
		removeNavigationController();
		sInstance = null;
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

	public void displayNavigationView(PLNavigationView view) {
		if (!mOverlayViews.contains(mNavigationController)) {
			addOverlayView(mNavigationController);
		}
		mNavigationController.pushView(view);
	}

	public void removeNavigationController() {
		if (mNavigationController != null && mOverlayViews.contains(mNavigationController)) {
			removeOverlayView(mNavigationController);
		}
	}

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

	public Context getContext() {
		return mContext;
	}

	public static void init(Context context) {
		if (sInstance != null) {
			MYLogUtil.showErrorToast("PLOverlayManagerは既に初期化済みです");
			return;
		}
		sInstance = new PLOverlayManager(context);
		sInstance.initFrontOverlays();
	}

	public static PLOverlayManager getInstance() {
		return sInstance;
	}
}
