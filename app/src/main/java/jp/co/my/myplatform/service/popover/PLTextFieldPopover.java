package jp.co.my.myplatform.service.popover;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import jp.co.my.myplatform.R;

public class PLTextFieldPopover extends PLPopoverView {

	private EditText mEditText;
	private OnEnterListener mEnterListener;

	public PLTextFieldPopover(View parentView, OnEnterListener enterListener) {
		super(parentView, R.layout.popover_text_field);
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

//	@Override
//	protected void didShowPopover() {
//		// キーボード表示
//		InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//		inputMethodManager.showSoftInput(mEditText, 0);
//	}

	@Override
	public void popoverWillRemove() {
		// キーボードを閉じる
		InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
	}

	public interface OnEnterListener {
		boolean onEnter(View v, String text);
	}
}
