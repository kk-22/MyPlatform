package jp.co.my.myplatform.memo;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;

import java.io.File;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLMemoEditorContent extends PLContentView {

	private PLMemoEditText mEditText;
	private PLMemoReadWriter mReadWriter;
	private Button mBackButton, mForwardButton, mCloseButton;
	private ScrollView mScrollView;

	public PLMemoEditorContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_memo_editor, this);
		mCloseButton = findViewById(R.id.close_button);
		mBackButton = findViewById(R.id.back_button);
		mForwardButton = findViewById(R.id.forward_button);
		mScrollView = findViewById(R.id.scroll_view);
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
		mEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					v.performClick(); // onTouchメソッドの必須処理
					scrollIfNeeded(event);
				}
				return false;
			}
		});
	}

	private void initButtonEvent() {
		// 上段
		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager inputMethod = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethod.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
				PLCoreService.getNavigationController().popView();
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
		findViewById(R.id.line_up_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(false);
			}
		});
		findViewById(R.id.delete_line_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.deleteSelectionLine();
			}
		});
		findViewById(R.id.line_down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEditText.moveSelectionLine(true);
			}
		});
		findViewById(R.id.hide_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});

		// 下段
		findViewById(R.id.list_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 現在のファイル保存
				mReadWriter.saveToFile();
				displayFileList();
			}
		});
		findViewById(R.id.file_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayFileOperationPopup();
			}
		});
		findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO:入力事に保存でsaveボタン不要にできる
				mReadWriter.saveToFile();
				MYLogUtil.showToast("Save to file");
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
				PLMemoEditorContent.this.removeTopPopover();

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
					mEditText.clearHistory();
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
						PLMemoEditorContent.this.removeTopPopover();
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

	// カーソル位置がキーボードにより隠れる場合、スクロールする
	private void scrollIfNeeded(MotionEvent event) {
		int scrollY = mScrollView.getScrollY();
		float tapRelativeY = event.getY() - scrollY;
		int scrollHeight = mScrollView.getHeight();
		int showingHeight = (int)(scrollHeight * 0.7); // キーボード表示中のScrollViewの可視範囲
		if (tapRelativeY < showingHeight) {
			// キーボードに隠れない位置
			return;
		}
		// カーソル位置がキーボードにより隠れる場合、スクロールする
		final int nextScrollY = scrollY +  (scrollHeight - showingHeight);
		int lackHeight = (nextScrollY + scrollHeight) - mEditText.getHeight();
		if (lackHeight > 0) {
			// 末尾の行をタップした場合は、スクロールできるように改行追加
			int lineHeight = mEditText.getLineHeight();
			for (; 0 < lackHeight ; lackHeight -= lineHeight) {
				mEditText.append("\n");
			}
		}
		// 改行追加時に高さ自動計算後にscrollToを実行する必要があるためディレイ
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(mScrollView.getScrollX(), nextScrollY);
			}
		}, 1);
	}
}
