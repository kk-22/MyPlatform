package jp.co.my.myplatform.service.popover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.navigation.PLNavigationView;
import jp.co.my.myplatform.service.layout.PLCenterLayoutController;
import jp.co.my.myplatform.service.layout.PLAbstractLayoutController;

public class PLPopoverView extends FrameLayout {

	protected View mSubView;
	protected PLNavigationView mParentNavigationView;
	protected PLAbstractLayoutController mLayout;

	public PLPopoverView(int subResource) {
		super(PLCoreService.getContext());

		LayoutInflater.from(getContext()).inflate(R.layout.popover_base_view, this);
		ViewGroup viewCroup = (ViewGroup) findViewById(R.id.content_relative);
		mSubView = LayoutInflater.from(getContext()).inflate(subResource, viewCroup);

		initBackgroundTouchEvent();
	}

	public void showPopover() {
		showPopover(new PLCenterLayoutController());
	}

	public void showPopover(PLAbstractLayoutController layout) {
		mLayout = layout;
		setSubViewPosition();

		PLNavigationView navigationView = PLCoreService.getNavigationController().getCurrentView();
		navigationView.addPopover(this);
	}

	public void popoverWillRemove() {
	}

	public void addedPopover(PLNavigationView navigationView) {
		mParentNavigationView = navigationView;
	}

	public void removeFromNavigation() {
		mParentNavigationView.removePopover(this);
	}

	private void setSubViewPosition() {
		mSubView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// 変更によってループしないように解除
				mSubView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				// サイズが設定された後のタイミングで位置調整
				FrameLayout frameLayout = (FrameLayout) getParent();
				mLayout.updateLayout(mSubView, frameLayout);
			}
		});
	}

	private void initBackgroundTouchEvent() {
		findViewById(R.id.background_view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFromNavigation();
			}
		});
		mSubView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// タップイベントを裏に送らない
			}
		});
	}
}
