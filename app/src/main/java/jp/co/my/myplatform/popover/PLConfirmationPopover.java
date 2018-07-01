package jp.co.my.myplatform.popover;

import android.view.View;
import android.widget.TextView;

import jp.co.my.myplatform.R;

public class PLConfirmationPopover extends PLPopoverView {

	private String mTitle;

	// YES / NO イベントを共通化する際に使うコンストラクタ
	public PLConfirmationPopover(String title, PLConfirmationListener listener) {
		this(title, listener, listener);
	}

	public PLConfirmationPopover(String title,
								 final PLConfirmationListener yesListener,
								 final PLConfirmationListener noListener) {
		super(R.layout.popover_confirmation);
		mTitle = title;

		((TextView)findViewById(R.id.title_text)).setText(title);
		findViewById(R.id.yes_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLConfirmationPopover.this.removeFromContentView();
				yesListener.onClickButton(true);
			}
		});
		findViewById(R.id.no_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLConfirmationPopover.this.removeFromContentView();
				if (noListener != null) {
					noListener.onClickButton(false);
				}
			}
		});
		showPopover();
	}

	public interface PLConfirmationListener {
		void onClickButton(boolean isYes);
	}

	@Override
	public boolean equals(Object obj) {
		if (getClass() != obj.getClass()) {
			return false;
		}
		PLConfirmationPopover popover = (PLConfirmationPopover) obj;
		return mTitle.equals(popover.mTitle);
	}
}
