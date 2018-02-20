package jp.co.my.myplatform.memo;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLMemoEditorView extends PLContentView {

	private PLMemoEditText mEditText;
	private PLMemoReadWriter mReadWriter;
	private Button mBackButton, mForwardButton;

	public PLMemoEditorView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mBackButton = findViewById(R.id.back_button);
		mForwardButton = findViewById(R.id.forward_button);
		mEditText = findViewById(R.id.memo_edit);
		mEditText.setEditorContent(this);

		mReadWriter = new PLMemoReadWriter(mEditText);
		initEditTextEvent();
		initButtonEvent();

		mReadWriter.loadFirstMemo();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mReadWriter.saveToFile();
	}

	void updateButtons() {
		mBackButton.setEnabled(mEditText.hasBackText());
		mForwardButton.setEnabled(mEditText.hasForwardText());
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
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.goBack();
			}
		});
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.goForward();
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
		findViewById(R.id.file_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayFileOperationPopup();
			}
		});
		findViewById(R.id.line_up_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(false);
			}
		});
		findViewById(R.id.line_down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(true);
			}
		});
		findViewById(R.id.delete_line_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.deleteSelectionLine();
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

	private void displayFileOperationPopup() {
		PLListPopover.showItems(new PLListPopover.PLListItem("リネーム", new Runnable() {
			@Override
			public void run() {
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
		}), new PLListPopover.PLListItem("削除", new Runnable() {
			@Override
			public void run() {
				new PLConfirmationPopover("削除する", new PLConfirmationPopover.PLConfirmationListener() {
					@Override
					public void onClickButton(boolean isYes) {
						PLMemoEditorView.this.removeTopPopover();
						if (mReadWriter.deleteCurrentFile()) {
							MYLogUtil.showToast("削除成功");
						} else {
							MYLogUtil.showToast("削除に失敗しました");
						}
						displayFileList();
					}
				}, null);
			}
		}));
	}
}
