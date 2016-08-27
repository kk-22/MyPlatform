package jp.co.my.myplatform.service.navigation;

import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLHomeView extends PLNavigationView {


	public PLHomeView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.view_home, this);

		setButtonEvent();
	}

	private void setButtonEvent() {
		findViewById(R.id.lock_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.showToast("ok");
			}
		});
		findViewById(R.id.alarm_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.showToast("ok");
			}
		});	}
}
