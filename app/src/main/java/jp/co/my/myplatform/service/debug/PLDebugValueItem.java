package jp.co.my.myplatform.service.debug;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLDebugValueItem extends PLDebugAbstractItem {

	private String[] mTitles;
	private String[] mValues;

	public PLDebugValueItem(String[] titles, String[] values) {
		super();
		mTitles = titles;
		mValues = values;

		if (titles.length != values.length) {
			MYLogUtil.showErrorToast("Length error titles=" +titles.length +" values=" +values.length);
		}
	}

	public PLDebugValueItem(String title, String value) {
		this(new String[]{title}, new String[]{value});
	}

	public PLDebugValueItem(String title1, String value1, String title2, String value2) {
		this(new String[]{title1, title2}, new String[]{value1, value2});
	}

	@Override
	public View updateView(View view, ViewGroup parent) {

		LinearLayout linear = (LinearLayout) view;
		int currentCount = (linear.getChildCount() + 1) / 2;
		int nextCount = mTitles.length;
		if (nextCount < currentCount ) {
			int lastIndex = nextCount * 2 - 1;
			linear.removeViews(lastIndex, linear.getChildCount() - lastIndex);
		} else {
			for (int i = currentCount; i < nextCount; i++) {
				if (i > 0) {
					addPartitionToParent(linear);
				}
				LinearLayout subLinear = (LinearLayout) LayoutInflater.from(
						view.getContext()).inflate(R.layout.item_debug_value, linear, false);
				subLinear.setTag("subLinear" +i);
				addViewToEqualInterval(subLinear, linear);
			}
		}

		for (int i = 0; i < nextCount; i++) {
			LinearLayout subLinear = (LinearLayout) linear.findViewWithTag("subLinear" + i);
			((TextView) subLinear.findViewById(R.id.title_text)).setText(mTitles[i]);
			((TextView) subLinear.findViewById(R.id.value_text)).setText(mValues[i]);
		}
		return view;
	}
}
