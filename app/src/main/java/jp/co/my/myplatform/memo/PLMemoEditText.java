package jp.co.my.myplatform.memo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import java.util.Objects;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYStringUtil;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

@SuppressLint("AppCompatCustomView")
public class PLMemoEditText extends EditText implements TextWatcher {

	private static final int NUMBER_OF_NONEXISTENT_LINES = -1; // 存在しない行数
	private static final int NONE_DELETE_LINES = -1; // 削除した行が無い
	private static final int NO_HISTORY_INDEX = -1;

	private int mInputStartLength; // 入力開始時の文字数
	private int mHistoryIndex;
	private int mPrevDeleteLines; // 前回文字削除した行数
	private boolean mDisableHistory; // true なら履歴の保存をしない
	private MYArrayList<String> mTextHistories;
	private PLMemoEditorContent mEditorContent;
	private ScrollView mScrollView;
	private String mLastChangedText;

	public PLMemoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHistoryIndex = NO_HISTORY_INDEX;
		mTextHistories = new MYArrayList<>();
		addTextChangedListener(this);
	}

	// 外のクラスからsetOnTouchListenerを呼ぶのに必要
	@Override
	public boolean performClick() {
		return super.performClick();
	}

	void clearHistory() {
		// 次の新しいメモ読み込み時に mTextHistories に保存されるように空文字をセット
		setText("");
		mHistoryIndex = NO_HISTORY_INDEX;
		mTextHistories.clear();
		mEditorContent.updateButtons();
	}

	boolean hasBackText() {
		return (0 < mHistoryIndex);
	}

	boolean hasForwardText() {
		return (mHistoryIndex + 1 < mTextHistories.size());
	}

	void scrollWithSelection(boolean shouldScroll, int index) {
		scrollWithSelection(shouldScroll, index, index);
	}

	void scrollWithSelection(boolean shouldScroll, int start, int end) {
		int line = getLayout().getLineForOffset (start);
		int scrollY = (int) ((line + 0.5) * getLineHeight());
		int diffY = scrollY - mScrollView.getScrollY();
		if (shouldScroll || diffY <= 0 || mScrollView.getHeight() / 2 < diffY) {
			// キーボードに隠れたり画面外の位置なら強制スクロール
			mScrollView.scrollTo(0, scrollY);
		}
		if (!isFocused()) {
			requestFocus();
		}
		setSelection(start, end);
	}

	void goBack() {
		if (hasBackText()) {
			loadHistory(true);
		}
	}

	void goForward() {
		if (hasForwardText()) {
			loadHistory(false);
		}
	}

	void hideKeyboard() {
		InputMethodManager input = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (input != null) {
			input.hideSoftInputFromWindow(getWindowToken(), HIDE_NOT_ALWAYS );
		}
	}

	void deleteSelectionLine() {
		didFinishDeleting();
		int startLines = getSelectionNumberOfLines(true);
		int endLines = getSelectionNumberOfLines(false);

		int startIndex = getIndexOfLines(startLines, true);
		int endIndex = getIndexOfLines(endLines + 1, true);
		if (startIndex == endIndex) {
			// 最終行に文字がないケース。一番最後の改行コードを削除
			startIndex = Math.max(0, startIndex - 1);
		}

		Editable text = getText();
		text.delete(startIndex, endIndex);
		scrollWithSelection(false, startIndex);
	}

	void moveSelectionLine(boolean toDown) {
		didFinishDeleting();

		int currentStartLines = getSelectionNumberOfLines(true);
		int currentEndLines = getSelectionNumberOfLines(false);
		int targetLines = (toDown) ? currentEndLines + 1 : currentStartLines - 1;
		int targetIndex = getIndexOfLines(targetLines, false);
		if (targetIndex == NUMBER_OF_NONEXISTENT_LINES) {
			// 入れ替え対象の行が存在しない
			return;
		}
		// 入れ替え行の文字列を指定するために、文字列の開始と終了位置を取得
		int maxLines = Math.max(currentEndLines, targetLines);
		int overLines = maxLines + 1;
		int currentIndex = getIndexOfLines(currentStartLines, true);
		int startIndex = Math.min(currentIndex, targetIndex);
		int middleIndex = Math.max(currentIndex, targetIndex);
		int endIndex = getIndexOfLines(overLines, true);
		// 行を入れ替える
		Editable editable = getText();
		String replaceString = editable.subSequence(middleIndex, endIndex).toString()
				+ editable.subSequence(startIndex, middleIndex).toString();
		editable.replace(startIndex, endIndex, replaceString);
		if (currentStartLines == currentEndLines) {
			// 1行のみ選択していた場合、移動先の行端へカーソルを移動
			scrollWithSelection(false, getTailIndexOfLines(targetLines));
		} else {
			// 複数行選択していた場合、連続して行移動できるように移動行全体を選択
			if (toDown) {
				int numberOfTargetChar = endIndex - middleIndex;
				scrollWithSelection(false, startIndex + numberOfTargetChar, endIndex - 1);
			} else {
				int numberOfMovedChar = endIndex - middleIndex;
				scrollWithSelection(false, startIndex, startIndex + numberOfMovedChar - 1);
			}
		}
	}

	private int getSelectionNumberOfLines(boolean isStart) {
		int lineCount = 1;
		Editable text = getText();
		int endIndex = (isStart) ? getSelectionStart() : getSelectionEnd();
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
		String nextText = s.toString();
		if (mLastChangedText != null && mLastChangedText.equals(nextText)) {
			// 連続で呼ばれたケース
			return;
		}
		mLastChangedText = nextText;
		if (s.length() < 1 || start >= s.length() || start < 0 || count == 0) {
			return;
		}
		if (s.subSequence(start, start + 1).toString().equals("\n")) {
			// 改行入力・削除にカーソルがキーボード裏へ隠れないようにスクロール
			int diff = getLineHeight();
			mScrollView.scrollTo(0, mScrollView.getScrollY() + diff);
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {
		String string = editable.toString();
		if (string.equals(getCurrentHistoryText())) {
			return;
		}
		int diff = string.length() - mInputStartLength;
		if (diff < 0) {
			// 文字削除時
			int currentLines = getSelectionNumberOfLines(false);
			String prevString = Objects.requireNonNull(getCurrentHistoryText());
			String deletedText = MYStringUtil.diffString(string, prevString);
			boolean isDeletedLineBreak = (currentLines == mPrevDeleteLines - 1 && deletedText.contains("\n"));
			if (currentLines == mPrevDeleteLines || isDeletedLineBreak) {
				// 2文字目以降の削除の場合は履歴を上書き保存するためにindexをずらす
				mHistoryIndex--;
			}
			if (isDeletedLineBreak) {
				// 次回にindexをずらさないためにリセット
				mPrevDeleteLines = NONE_DELETE_LINES;
			} else {
				mPrevDeleteLines = currentLines;
			}
			saveHistory();
			return;
		}
		// 削除以外
		didFinishDeleting();

		Object[] spanned = editable.getSpans(0, editable.length(), Object.class);
		if (spanned == null) {
			return;
		}
		boolean unfixed = false;
		for (Object obj : spanned) {
			if ((editable.getSpanFlags(obj) & Spanned.SPAN_COMPOSING) == Spanned.SPAN_COMPOSING) {
				unfixed = true;
			}
		}
		if (!unfixed) {
			// 半角数字の入力か、文字を確定したケース
			saveHistory();
		}
	}

	private void didFinishDeleting() {
		mPrevDeleteLines = NONE_DELETE_LINES;
	}

	private void saveHistory() {
		if (mDisableHistory) {
			return;
		}
		mHistoryIndex++;
		mTextHistories.removeToLastFromIndex(mHistoryIndex);
		mTextHistories.add(getText().toString());
		mEditorContent.updateButtons();
	}

	private void loadHistory(boolean isBack) {
		didFinishDeleting();

		String prevText = getText().toString();
		String lastSaveText = getCurrentHistoryText();
		if (!prevText.equals(lastSaveText)) {
			// 差分があるため現在のメモを履歴へ保存
			saveHistory();
			mHistoryIndex--;
		}

		if (isBack) {
			mHistoryIndex--;
		} else {
			mHistoryIndex++;
		}
		mDisableHistory = true;
		String nextText = Objects.requireNonNull(getCurrentHistoryText());
		setText(nextText);
		mDisableHistory = false;
		mEditorContent.updateButtons();

		// 差分の位置へカーソルを移動
		int prevLength = prevText.length();
		int nextLength = nextText.length();
		int length = Math.min(prevLength, nextLength);
		int focusIndex = nextLength;
		for (int i = 0; i < length; i++) {
			if (prevText.charAt(i) != nextText.charAt(i)) {
				focusIndex = i;
				break;
			}
		}
		if (prevLength < nextLength && focusIndex != nextLength) {
			// TODO: 削除行と次の行の1文字目が同じ場合位置がずれる
			// 文字削除前にロードする場合、削除し直せるように削除していた文字の次へカーソルを当てる
			focusIndex += (nextLength - prevLength);
			if (nextText.charAt(focusIndex - 1) == '\n') {
				// 編集した行の末尾にカーソルを当てるためにデクリメント
				focusIndex--;
			}
		}
		scrollWithSelection(false, focusIndex);
	}

	private String getCurrentHistoryText() {
		if (mHistoryIndex == NO_HISTORY_INDEX) {
			return null;
		}
		return mTextHistories.get(mHistoryIndex);
	}

	// setter
	public void setEditorContent(PLMemoEditorContent editorContent) {
		mEditorContent = editorContent;
	}

	public void setScrollView(ScrollView scrollView) {
		mScrollView = scrollView;
	}
}
