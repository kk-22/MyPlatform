package jp.co.my.myplatform.service.navigation;

import android.widget.LinearLayout;

import jp.co.my.myplatform.service.overlay.PLOverlayManager;

public class PLNavigationView extends LinearLayout {

	public PLNavigationView() {
		super(PLOverlayManager.getInstance().getContext());
	}

	public void viewWillDisappear() {
		// remove前に必要な処理をoverrideして実装
	}
}
