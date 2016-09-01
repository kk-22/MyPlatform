package jp.co.my.myplatform.service.popover;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLTextFieldPopover extends PLPopoverView {

	private EditText mEditText;
	private OnEnterListener mEnterListener;

	public PLTextFieldPopover(OnEnterListener enterListener) {
		super(R.layout.popover_text_field);
		mEnterListener = enterListener;

		mEditText = (EditText) findViewById(R.id.form_edit);
		setKeyEvent();
	}

	private void setKeyEvent() {
		mEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					// 戻るボタン押下で閉じる
					removeFromNavigation();
					return true;
				}
				if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}

				String inputStr = mEditText.getText().toString();
				if (inputStr.length() == 0) {
					return false;
				}
				if (!mEnterListener.onEnter(v, inputStr)) {
					// 入力文字列に問題あり
					return false;
				}
				// テキストフィールド終了
				removeFromNavigation();
				return true;
			}
		});
	}

	@Override
	public void addedPopover(PLNavigationView navigationView) {
		super.addedPopover(navigationView);

		// キーボード表示
		mEditText.requestFocus();
		InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethod.toggleSoftInput(0, 0);
	}

	@Override
	public void popoverWillRemove() {
		// キーボードを閉じる
		InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethod.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

	public interface OnEnterListener {
		boolean onEnter(View v, String text);
	}
}
