package jp.co.my.myplatform.overlay;

import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;

public class PLFrontButtonView extends PLOverlayView {

	protected Button mButton;
	protected Class mTextClass;

	public PLFrontButtonView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.overlay_front_button, this);

		mButton = (Button) findViewById(R.id.front_button);
		mButton.setLayoutParams(createMatchParams());
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PLNavigationController navigation = PLCoreService.getNavigationController();
				if (!navigation.displayNavigationIfNeeded()) {
					navigation.hideNavigationIfNeeded();
				}
			}
		});
		mButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				PLOverlayManager overlayManager = PLCoreService.getOverlayManager();
				if (overlayManager.getOverlayView(PLPointSelectView.class) == null) {
					overlayManager.addOverlayView(new PLPointSelectView(PLFrontButtonView.this));
				}
				return true;
			}
		});
	}

	@Override
	public WindowManager.LayoutParams getOverlayParams() {
		SharedPreferences pref = MYLogUtil.getPreference();
		Float horizontal = pref.getFloat(PLPointSelectView.KEY_HORIZONTAL_MARGIN, 0);
		Float vertical = pref.getFloat(PLPointSelectView.KEY_VERTICAL_MARGIN, 0);

		WindowManager.LayoutParams params = getBaseParamsForButtonView();
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.horizontalMargin = horizontal;
		params.verticalMargin = vertical;
		params.alpha = 0.5f;
		return params;
	}

	public void setText(Class klass, int fontSize, String text) {
		mTextClass = klass;
		mButton.setTextSize(fontSize);
		mButton.setText(text);
	}

	public boolean clearText(Class klass) {
		if (!klass.equals(mTextClass)) {
			return false;
		}
		mTextClass = null;
		mButton.setText("");
		return true;
	}
}
