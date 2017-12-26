package jp.co.my.myplatform.debug;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.co.my.myplatform.R;

public class PLDebugTitleItem extends PLDebugAbstractItem {

	private String mString;

	public PLDebugTitleItem(String title) {
		super();
		this.mString = title;
	}

	@Override
	public View updateView(View view, ViewGroup parent) {
		((TextView) view.findViewById(R.id.title_text)).setText(mString);
		return view;
	}

	@Override
	public int getResourceId() {
		return R.layout.cell_debug_title;
	}
}
