package jp.co.my.myplatform.memo;

import android.text.Editable;
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
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLMemoEditorView extends PLContentView {

	private static final int EDIT_MODE_HEIGHT = 1150;				// 入力モード中のEditTextの高さ
	private static final int NUMBER_OF_NONEXISTENT_LINES = -1; // 存在しない行数

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
		findViewById(R.id.file_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayFileOperationPopup();
			}
		});
		findViewById(R.id.line_up_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveSelectionLine(false);
			}
		});
		findViewById(R.id.line_down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveSelectionLine(true);
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

	private int getSelectionNumberOfLines() {
		int lineCount = 1;
		Editable text = mEditText.getText();
		int endIndex = mEditText.getSelectionEnd();
		for (int i = 0; i < endIndex; i++) {
			if (text.charAt(i) == '\n') {
				lineCount++;
			}
		}
		return lineCount;
	}

	// 行頭の位置を取得
	// returnLastIndex : true なら存在しない行数を指定された場合に最後の index を返す
	private int getIndexOfLines(int numberOfLines, boolean returnLastIndex) {
		if (numberOfLines <= 0) {
			return NUMBER_OF_NONEXISTENT_LINES;
		}
		int lineCount = 1;
		Editable text = mEditText.getText();
		int i = 0;
		int length = text.length();
		for (; i < length; i++) {
			if (lineCount == numberOfLines) {
				return i;
			}
			if (text.charAt(i) == '\n') {
				lineCount++;
			}
		}
		if (returnLastIndex) {
			return length;
		} else {
			// 引数の行数は存在しない
			return NUMBER_OF_NONEXISTENT_LINES;
		}
	}

	// 行端の位置を取得
	private int getTailIndexOfLines(int numberOfLines) {
		 int nextLinesIndex = getIndexOfLines(numberOfLines + 1, false);
		 if (nextLinesIndex == NUMBER_OF_NONEXISTENT_LINES) {
		 	int length = mEditText.length();
		 	if (mEditText.getText().charAt(length - 1) == '\n') {
		 		return length - 1;
			}
		 	return length;
		 }
		 return nextLinesIndex - 1;
	}

	private void moveSelectionLine(boolean toDown) {
		int currentLines = getSelectionNumberOfLines();
		int targetLines = (toDown) ? currentLines + 1 : currentLines - 1;
		int targetIndex = getIndexOfLines(targetLines, false);
		if (targetIndex == NUMBER_OF_NONEXISTENT_LINES) {
			// 入れ替え対象の行が存在しない
			return;
		}
		// 入れ替え行の文字列を指定するために、文字列の開始と終了位置を取得
		int maxLines = Math.max(currentLines, targetLines);
		int overLines = maxLines + 1;
		int currentIndex = getIndexOfLines(currentLines, true);
		int startIndex = Math.min(currentIndex, targetIndex);
		int middleIndex = Math.max(currentIndex, targetIndex);
		int endIndex = getIndexOfLines(overLines, true);
		// 行を入れ替えた文章を作成してセット
		Editable prevText = mEditText.getText();
		String nextString = prevText.subSequence(0, startIndex).toString()
				+ prevText.subSequence(middleIndex, endIndex).toString()
				+ prevText.subSequence(startIndex, middleIndex).toString()
				+ prevText.subSequence(endIndex, prevText.length());
		mEditText.setText(nextString);
		// 移動先の行端へカーソルを移動
		mEditText.setSelection(getTailIndexOfLines(targetLines));
	}
}
