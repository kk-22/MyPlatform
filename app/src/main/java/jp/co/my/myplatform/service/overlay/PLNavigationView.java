package jp.co.my.myplatform.service.overlay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import jp.co.my.myplatform.R;

public class PLNavigationView extends PLOverlayView {

	private FrameLayout mFrameLayout;
	private View mCurrentView;

	public PLNavigationView() {
		super();

		LayoutInflater.from(getContext()).inflate(R.layout.overlay_navigation_controller, this);
		mFrameLayout = (FrameLayout) findViewById(R.id.navigation_frame);


		findViewById(R.id.space_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLOverlayManager.getInstance().removeNavigationView();
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		return getBaseParamsForFullView();
	}

	public void pushView(View view) {
		if (view == null) {
			// 前回のViewを表示
			return;
		}

		if (mCurrentView != null) {
			mFrameLayout.removeView(mCurrentView);
		}
		mFrameLayout.addView(view, createMatchParams());
		mCurrentView = view;
	}
}
