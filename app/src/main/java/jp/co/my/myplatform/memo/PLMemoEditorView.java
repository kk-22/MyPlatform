package jp.co.my.myplatform.memo;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import java.io.File;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLMemoEditorView extends PLContentView {

	private static final int EDIT_MODE_HEIGHT = 1150;				// 入力モード中のEditTextの高さ

	private EditText mEditText;
	private PLMemoReadWriter mReadWriter;
	private PLMemoInputObserver mInputObserver;

	public PLMemoEditorView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mEditText = findViewById(R.id.memo_edit);

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
	}

	private void initButtonEvent() {
		// 1行目
		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO:入力事に保存でsaveボタン不要にできる
				mReadWriter.saveToFile();
				MYLogUtil.showToast("Save to file");
			}
		});
		findViewById(R.id.list_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 現在のファイル保存
				mReadWriter.saveToFile();
				displayFileList();
			}
		});

		// 2行目
		findViewById(R.id.set_name_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
					@Override
					public boolean onEnter(View v, String text) {
						if (mReadWriter.renameCurrentFile(text)) {
							MYLogUtil.showToast("リネーム成功");
						} else {
							MYLogUtil.showToast("リネームに成功しました");
						}
						return true;
					}
				}).showPopover();
			}
		});
		findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] titles = {"削除する"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						PLMemoEditorView.this.removeTopPopover();
						if (mReadWriter.deleteCurrentFile()) {
							MYLogUtil.showToast("削除成功");
						} else {
							MYLogUtil.showToast("削除に失敗しました");
						}
						displayFileList();
					}
				}).showPopover();
			}
		});
	}

	private void displayFileList() {
		final File[] files = mReadWriter.memoFiles();
		ArrayList<String> nameList = new ArrayList<>();
		for (File file : files) {
			nameList.add(file.getName());
		}
		nameList.add("新規メモ");
		String[] titles = nameList.toArray(new String[nameList.size()]);
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLMemoEditorView.this.removeTopPopover();

				if (files.length == position) {
					// 新規ファイル
					mReadWriter.loadedMemo(null, true);
					mEditText.setText("");
					MYLogUtil.showToast("新規メモ");
					return;
				}

				File file = files[position];
				String name = file.getName();
				if (name.equals(mReadWriter.getCurrentName())) {
					MYLogUtil.showToast("既に開いています");
				} else {
					mReadWriter.loadFromFile(name);
					mReadWriter.loadedMemo(name, true);
				}
			}
		}).showPopover();
	}
}
