package jp.co.my.myplatform.debug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import jp.co.my.common.util.MYLogUtil;

public class PLDebugButtonItem extends PLDebugAbstractItem {

	private String[] mTitles;
	private View.OnClickListener[] mOnClickListeners;

	public PLDebugButtonItem(String[] titles, View.OnClickListener... onClickListeners) {
		super();
		mTitles = titles;
		mOnClickListeners = onClickListeners;

		if (titles.length != onClickListeners.length) {
			MYLogUtil.showErrorToast("Length error titles=" +titles.length +" listeners=" +onClickListeners.length);
		}
	}

	public PLDebugButtonItem(String title, View.OnClickListener onClickListener) {
		this(new String[]{title}, onClickListener);
	}

	public PLDebugButtonItem(String title1, View.OnClickListener onClickListener1,
							 String title2, View.OnClickListener onClickListener2) {
		this(new String[]{title1, title2}, onClickListener1, onClickListener2);
	}

	@Override
	public View updateView(View view, ViewGroup parent) {
		LinearLayout linear = (LinearLayout) view;
		int currentCount = linear.getChildCount();
		int nextCount = mTitles.length;
		if (nextCount < currentCount ) {
			linear.removeViews(nextCount, currentCount - nextCount);
		} else {
			for (int i = currentCount; i < nextCount; i++) {
				Button button = new Button(view.getContext());
				button.setTag("button" +i);
				addViewToEqualInterval(button, linear);
			}
		}

		for (int i = 0; i < nextCount; i++) {
			Button button = (Button) linear.findViewWithTag("button" +i);
			button.setText(mTitles[i]);
			button.setOnClickListener(mOnClickListeners[i]);
		}
		return view;
	}
}
