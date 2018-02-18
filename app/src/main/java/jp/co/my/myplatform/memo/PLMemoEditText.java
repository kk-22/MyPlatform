package jp.co.my.myplatform.memo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import jp.co.my.common.util.MYArrayList;

@SuppressLint("AppCompatCustomView")
public class PLMemoEditText extends EditText implements TextWatcher {

	private static final int NUMBER_OF_NONEXISTENT_LINES = -1; // 存在しない行数
	private static final int NO_HISTORY_INDEX = -1;

	private int mInputStartLength; // 入力開始時の文字数
	private int mHistoryIndex;
	private boolean disableHistory; // true なら履歴の保存をしない
	private MYArrayList<CharSequence> mTextHistories;

	public PLMemoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHistoryIndex = NO_HISTORY_INDEX;
		mTextHistories = new MYArrayList<>();
		addTextChangedListener(this);
	}

	void goBack() {
		if (mHistoryIndex <= 0) {
			return;
		}
		loadHistory(true);
	}

	void goForward() {
		if (mTextHistories.size() <= mHistoryIndex + 1) {
			return;
		}
		loadHistory(false);
	}

	void deleteSelectionLine() {
		int currentLines = getSelectionNumberOfLines();
		int startIndex = getIndexOfLines(currentLines, true);
		int endIndex = getIndexOfLines(currentLines + 1, true);
		if (startIndex == endIndex) {
			// 最終行に文字がないケース。一番最後の改行コードを削除
			startIndex = Math.max(0, startIndex - 1);
		}

		Editable text = getText();
		text.delete(startIndex, endIndex);
		setSelection(startIndex);
	}

	void moveSelectionLine(boolean toDown) {
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
		Editable prevText = getText();
		String nextString = prevText.subSequence(0, startIndex).toString()
				+ prevText.subSequence(middleIndex, endIndex).toString()
				+ prevText.subSequence(startIndex, middleIndex).toString()
				+ prevText.subSequence(endIndex, prevText.length());
		setText(nextString);
		// 移動先の行端へカーソルを移動
		setSelection(getTailIndexOfLines(targetLines));
	}

	private int getSelectionNumberOfLines() {
		int lineCount = 1;
		Editable text = getText();
		int endIndex = getSelectionEnd();
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
		Editable text = getText();
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
		if (numberOfLines <= 0) {
			return 0;
		}

		int nextLinesIndex = getIndexOfLines(numberOfLines + 1, false);
		if (nextLinesIndex == NUMBER_OF_NONEXISTENT_LINES) {
			int length = length();
			if (getText().charAt(length - 1) == '\n') {
				return length - 1;
			}
			return length;
		}
		return nextLinesIndex - 1;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		mInputStartLength = s.toString().length();
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.toString().length() < mInputStartLength) {
			return;
		}
		Object[] spanned = s.getSpans(0, s.length(), Object.class);
		if (spanned == null) {
			return;
		}
		boolean unfixed = false;
		for (Object obj : spanned) {
			if ((s.getSpanFlags(obj) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
				unfixed = true;
			}
		}
		if (!unfixed) {
			// 半角数字の入力か、文字を確定したケース
			didEnter();
		}
	}

	private void didEnter() {
		if (disableHistory) {
			return;
		}
		mHistoryIndex++;
		mTextHistories.removeToLastFromIndex(mHistoryIndex);
		mTextHistories.add(getText().toString());
	}

	private void loadHistory(boolean isBack) {
		if (isBack) {
			mHistoryIndex--;
		} else {
			mHistoryIndex++;
		}
		disableHistory = true;
		setText(mTextHistories.get(mHistoryIndex));
		disableHistory = false;
	}
}
