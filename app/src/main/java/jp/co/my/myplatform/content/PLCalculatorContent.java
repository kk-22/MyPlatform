package jp.co.my.myplatform.content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.popover.PLListPopover;


public class PLCalculatorContent extends PLContentView implements View.OnClickListener {

	private static final String KEY_LAST_INPUT_STRINGS = "KEY_LAST_INPUT_STRINGS";
	private static final int MAX_HISTORIES_COUNT = 5;

	private Button mCacheTextButton;
	private TextView mEntryText;
	private TextView mTotalText;
	private StringBuilder mInputString;
	private MYArrayList<String> mHistories;

	public PLCalculatorContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_calculator, this);
		ViewGroup headerView = findViewById(R.id.header);
		if (PLCoreService.getNavigationController().isHalf()) {
			headerView.removeAllViews();
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (inflater != null) {
				inflater.inflate(R.layout.view_calculator_header_small, headerView);
			}
		}

		mCacheTextButton = headerView.findViewById(R.id.cache_button);
		mEntryText = headerView.findViewById(R.id.entry_text);
		mTotalText = headerView.findViewById(R.id.total_text);

		mHistories = MYLogUtil.loadArrayList(KEY_LAST_INPUT_STRINGS);
		if (mHistories == null) {
			mHistories = new MYArrayList<>();
		}
		String lastText = mHistories.getLast();
		if (lastText == null) {
			lastText = "";
		}
		mCacheTextButton.setText(new StringBuilder(lastText));

		mInputString = new StringBuilder("0");

		mCacheTextButton.setOnClickListener(this);
		findViewById(R.id.clear_all_button).setOnClickListener(this);
		findViewById(R.id.clear_entry_button).setOnClickListener(this);
		findViewById(R.id.back_button).setOnClickListener(this);
		findViewById(R.id.equal_button).setOnClickListener(this);
		findViewById(R.id.plus_button).setOnClickListener(this);
		findViewById(R.id.minus_button).setOnClickListener(this);
		findViewById(R.id.multiplication_button).setOnClickListener(this);
		findViewById(R.id.divide_button).setOnClickListener(this);
		findViewById(R.id.point_button).setOnClickListener(this);
		findViewById(R.id.zero_button).setOnClickListener(this);
		findViewById(R.id.one_button).setOnClickListener(this);
		findViewById(R.id.two_button).setOnClickListener(this);
		findViewById(R.id.three_button).setOnClickListener(this);
		findViewById(R.id.four_button).setOnClickListener(this);
		findViewById(R.id.five_button).setOnClickListener(this);
		findViewById(R.id.six_button).setOnClickListener(this);
		findViewById(R.id.seven_button).setOnClickListener(this);
		findViewById(R.id.eight_button).setOnClickListener(this);
		findViewById(R.id.nine_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cache_button: showHistories(); break;
			case R.id.clear_all_button: clearAll(); break;
			case R.id.clear_entry_button: clearEntry(); break;
			case R.id.back_button: backOneChar(); break;
			case R.id.equal_button: equalEvent(); break;
			case R.id.plus_button: addValue("+"); break;
			case R.id.minus_button: addValue("-"); break;
			case R.id.multiplication_button: addValue("×"); break;
			case R.id.divide_button: addValue("/"); break;
			case R.id.point_button: addValue("."); break;
			case R.id.zero_button: addValue("0"); break;
			case R.id.one_button: addValue("1"); break;
			case R.id.two_button: addValue("2"); break;
			case R.id.three_button: addValue("3"); break;
			case R.id.four_button: addValue("4"); break;
			case R.id.five_button: addValue("5"); break;
			case R.id.six_button: addValue("6"); break;
			case R.id.seven_button: addValue("7"); break;
			case R.id.eight_button: addValue("8"); break;
			case R.id.nine_button: addValue("9"); break;
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		equalEvent();
		MYLogUtil.saveObject(KEY_LAST_INPUT_STRINGS, mHistories, true);
	}

	private void addValue(String value) {
		String currentString = new String(mInputString);
		String[] values = currentString.split("[^0-9.]");
		String[] signs = currentString.split("[0-9.]+");
		Boolean lastIsNumber = values.length >= signs.length; // 最期の文字は数字なら true
		String lastValue = values[values.length - 1];
		if (value.equals("+") || value.equals("-") || value.equals("×") || value.equals("/")) {
			if (!lastIsNumber) {
				// 演算子を消す
				mInputString.deleteCharAt(mInputString.length() - 1);
			}
			if (lastValue.charAt(lastValue.length() - 1) == '.') {
				// 余計な小数点を削る
				mInputString.deleteCharAt(mInputString.length() - 1);
			}
		} else if (value.equals(".")) {
			if (lastValue.contains(".")) {
				return;
			}
			if (!lastIsNumber) {
				// 0を挿入
				value = "0.";
			}
		} else {
			// 0 ～ 9
			if (lastIsNumber && lastValue.length() >= 8) {
				// 最大8桁まで
				return;
			}

			if (value.equals("0")) {
				if (lastValue.equals("0")) {
					return;
				}
			} else {
				// 1 ～ 9
				if (lastValue.equals("0") && lastIsNumber) {
					// 余計な0を削る
					mInputString.deleteCharAt(mInputString.length() - 1);
				}
			}
		}
		mInputString.append(value);
		updateAllText();
	}

	private void clearAll() {
		deleteInputString(false);
		updateAllText();
		mCacheTextButton.setText("");
		mHistories.clear();
	}

	private void clearEntry() {
		deleteInputString(false);
		updateAllText();
	}

	private void backOneChar() {
		deleteInputString(true);
		updateAllText();
	}

	private boolean equalEvent() {
		String currentString = new String(mInputString);
		String[] values = currentString.split("[^0-9.]");
		if (values.length <= 1) {
			// 演算不可
			return false;
		}
		String cacheString = mEntryText.getText() + "=" +mTotalText.getText();
		mCacheTextButton.setText(cacheString);
		mHistories.add(cacheString);
		if (MAX_HISTORIES_COUNT < mHistories.size()) {
			mHistories.remove(0);
		}
		clearEntry();
		return true;
	}

	private void showHistories() {
		int count = mHistories.size();
		if (count == 0) {
			return;
		}
		ArrayList<String> titleArray = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			titleArray.add(mHistories.get(i).replace("=", "\n="));
		}
		new PLListPopover(titleArray.toArray(new String[0]), new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				revertCache(position);
			}
		}).showPopover();
	}

	private void revertCache(int index) {
		String nextText = mHistories.get(index);
		String[] values = nextText.split("=");
		if (values.length < 2) {
			return;
		}
		mHistories.remove(index);

		// キャッシュ文字列と交換
		if (!equalEvent()) {
			mCacheTextButton.setText("");
		}
		mInputString.setLength(0);
		mInputString.append(values[0]);
		updateAllText();
	}

	private void updateAllText() {
		String lineString = new String(mInputString);
		mEntryText.setText(lineString);

		String[] values = lineString.split("[^0-9.]");
		String[] signs = lineString.split("[0-9.]+");
		BigDecimal temp = new BigDecimal(values[0]);
		int length = Math.min(values.length, signs.length);
		for (int i = 1; i < length; i++) {
			String currentValue = values[i];
			String sign = signs[i];
			switch (sign) {
				case "+":
					temp = temp.add(new BigDecimal(currentValue));
					break;
				case "-":
					temp = temp.subtract(new BigDecimal(currentValue));
					break;
				case "×":
					temp = temp.multiply(new BigDecimal(currentValue));
					break;
				case "/":
					BigDecimal currentDecimal = new BigDecimal(currentValue);
					if (currentDecimal.floatValue() == 0) {
						continue;
					}
					temp = temp.divide(new BigDecimal(currentValue), 4, BigDecimal.ROUND_DOWN);
					break;
				default:
					break;
			}
		}
		mTotalText.setText(new DecimalFormat("#.##").format(temp));
	}

	private void deleteInputString(boolean isOneChar) {
		int length = mInputString.length();
		if (isOneChar) {
			mInputString.deleteCharAt(length - 1);
		} else {
			mInputString.delete(0, length);
		}
		if (mInputString.length() == 0) {
			mInputString.append("0");
		}
	}
}
