package jp.co.my.myplatform.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/*
 画面遷移やNavigationの非表示時にListViewがremoveされることで起きる、
 スクロール位置のリセット問題を解消するクラス
 */
public class PLSavePositionListView extends ListView {

	private int mScrollPosition;
	private int mScrollY;

	public PLSavePositionListView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	public PLSavePositionListView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLSavePositionListView(Context context) {
		this(context, null);
	}

	public void savePosition() {
		if (getAdapter() != null && getAdapter().getCount() > 0) {
			mScrollPosition = getFirstVisiblePosition();
			mScrollY = getChildAt(0).getTop();
		}
	}

	public void loadPosition() {
		if (mScrollY != 0 || mScrollPosition != 0) {
			setSelectionFromTop(mScrollPosition, mScrollY);
			mScrollPosition = 0;
			mScrollY = 0;
		}
	}
}
