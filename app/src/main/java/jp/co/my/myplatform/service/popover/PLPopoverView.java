package jp.co.my.myplatform.service.popover;

import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLPopoverView extends FrameLayout {

	protected View mSubView;
	protected PLNavigationView mParentNavigationView;

	public PLPopoverView(View parentView, int subResource) {
		super(PLCoreService.getContext());

		LayoutInflater.from(getContext()).inflate(R.layout.popover_base_view, this);
		ViewGroup viewCroup = (ViewGroup) findViewById(R.id.content_relative);
		mSubView = LayoutInflater.from(getContext()).inflate(subResource, viewCroup);

		initBackgroundTouchEvent();
		setSubViewPosition(parentView);
	}

	public void addedPopover(PLNavigationView navigationView) {
		mParentNavigationView = navigationView;
	}

	public void removeFromNavigation() {
		mParentNavigationView.removePopover(this);
	}

	private void setSubViewPosition(final View parentView) {
		mSubView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// サイズが設定された後のタイミングで位置調整
				// 変更によってループしないように解除
				mSubView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				FrameLayout frameLayout = (FrameLayout) getParent();
				Point displaySize = new Point(frameLayout.getWidth(), frameLayout.getHeight());
				float subSizeX = mSubView.getWidth();
				float subSizeY = mSubView.getHeight();

				if (parentView == null) {
					// 中央に配置
					mSubView.setTranslationX((displaySize.x - subSizeX) / 2);
					mSubView.setTranslationY((displaySize.y - subSizeY) / 2);
					return;
				}

				// 親ビューの座標取得
				int[] locations = new int[2];
				parentView.getLocationInWindow(locations);
				int parentPointX = locations[0];
				int parentPointY = locations[1];

				if (parentPointX + subSizeX < displaySize.x) {
					// 画面に収まる場合は親のX座標と同じ
					mSubView.setTranslationX(parentPointX);
				} else {
					// 画面右端に寄せる
					mSubView.setTranslationX(displaySize.x - subSizeX);
				}
				if (parentPointY + subSizeY < displaySize.y) {
					mSubView.setTranslationY(parentPointY);
				} else {
					// 収まるように上方向にずらす
					mSubView.setTranslationY(displaySize.y - parentView.getHeight() - subSizeY);
				}
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
