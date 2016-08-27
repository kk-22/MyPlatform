package jp.co.my.myplatform.service.overlay;

import android.widget.LinearLayout;

public class PLNavigationView extends LinearLayout {

	public PLNavigationView() {
		super(PLOverlayManager.getInstance().getContext());
	}

	public void viewWillDisappear() {
		// remove前に必要な処理をoverrideして実装
	}
}
