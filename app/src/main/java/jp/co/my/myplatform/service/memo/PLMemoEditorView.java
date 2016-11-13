package jp.co.my.myplatform.service.memo;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;

public class PLMemoEditorView extends PLContentView {

	private static final int EDIT_MODE_HEIGHT = 1150;				// 入力モード中のEditTextの高さ

	private EditText mEditText;
	private PLMemoReadWriter mReadWriter;
	private PLMemoInputObserver mInputObserver;

	public PLMemoEditorView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mEditText = (EditText) findViewById(R.id.memo_edit);

		mReadWriter = new PLMemoReadWriter(mEditText);
		mInputObserver = new PLMemoInputObserver(mEditText);
		initEditTextEvent();
		initButtonEvent();

		mReadWriter.loadFirstMemo();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mReadWriter.saveToFile();
	}

	@Override
	public boolean onBackKey() {
		changeFullMode();
		return super.onBackKey();
	}

	private void initEditTextEvent() {
		mEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				MYLogUtil.outputLog("OnKeyListener");
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					return onBackKey();
				} else if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_DOWN) {
					return false;
				}


//				String replaceText = "\nテスト";
//				int start = mEditText.getSelectionStart();
//				int end = mEditText.getSelectionEnd();
//				Editable editable = mEditText.getText();
//				editable.replace(Math.min( start, end ), Math.max( start, end ), replaceText );

				MYLogUtil.outputLog("changed");
//				PLMemoEditor.this.set

				return false;
			}
		});
		mEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.outputLog("onClick");
				changeEditMode();
			}
		});
		mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					MYLogUtil.outputLog("onFocusChange");
					changeEditMode();
				}
			}
		});
		mEditText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				int rootHeight = PLMemoEditorView.this.getHeight();
				mEditText.setHeight(rootHeight - 200);

				// ちらつき防止
				findViewById(R.id.button_linear).setVisibility(View.VISIBLE);
			}
		});
	}

	private void changeEditMode() {
		MYLogUtil.outputLog("changeEditMode");
		mEditText.setHeight(EDIT_MODE_HEIGHT);
	}

	private void changeFullMode() {
		MYLogUtil.outputLog("changeFullMode");
		int rootHeight = PLMemoEditorView.this.getHeight();
		mEditText.setHeight(rootHeight - 200);
	}

	private void initButtonEvent() {
		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mReadWriter.saveToFile();
				MYLogUtil.showToast("Save to file");
			}
		});
		findViewById(R.id.change_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEditText.getHeight() == EDIT_MODE_HEIGHT) {
					InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethod.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
					changeFullMode();
				} else {
					mEditText.setHeight(EDIT_MODE_HEIGHT);
				}
			}
		});
	}
}
