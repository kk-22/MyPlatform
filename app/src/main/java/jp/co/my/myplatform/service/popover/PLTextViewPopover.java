package jp.co.my.myplatform.service.popover;

import android.widget.TextView;

import jp.co.my.myplatform.R;

public class PLTextViewPopover extends PLPopoverView {

	private TextView mTextView;

	public PLTextViewPopover(String text) {
		super(R.layout.popover_text_view);
		mTextView = (TextView) findViewById(R.id.text_view);

		mTextView.setText(text);
	}

	// getter
	public TextView getTextView() {
		return mTextView;
	}
}
