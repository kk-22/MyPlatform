package jp.co.my.myplatform.service.memo;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import jp.co.my.common.util.MYLogUtil;

public class PLMemoInputObserver implements TextWatcher {

	private EditText mEditText;

	public PLMemoInputObserver(EditText editText) {
		mEditText = editText;

		mEditText.addTextChangedListener(this);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				MYLogUtil.outputLog("onEditorAction");
				return false;
			}
		});
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		MYLogUtil.outputLog("beforeTextChanged");
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		MYLogUtil.outputLog("onTextChanged");
	}

	@Override
	public void afterTextChanged(Editable s) {
		MYLogUtil.outputLog("afterTextChanged");
	}
}
