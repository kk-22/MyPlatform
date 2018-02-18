package jp.co.my.myplatform.memo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class PLMemoEditText extends EditText {

	private static final int NUMBER_OF_NONEXISTENT_LINES = -1; // 存在しない行数

	public PLMemoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
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
}
