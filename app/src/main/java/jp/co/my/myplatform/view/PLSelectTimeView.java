package jp.co.my.myplatform.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.co.my.common.util.MYCalendarUtil;
import jp.co.my.myplatform.R;

public class PLSelectTimeView extends LinearLayout
		implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView mCurrentTimeText, mSelectTimeText, mHourText, mMinText, mSecText;
	private SeekBar mHourSeekBar, mMinSeekBar, mSecSeekBar;
	private SeekBar mFocusSeekBar;		// 最後に操作したシークバー
	private HashMap<SeekBar, TextView> mTextMap;
	private Calendar mCurrentCalendar;	// 表示時刻と選択時刻のベースになる現在時刻

	public PLSelectTimeView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		mCurrentCalendar = Calendar.getInstance();

		LayoutInflater.from(context).inflate(R.layout.view_select_time, this);
		mCurrentTimeText = findViewById(R.id.current_time_text);
		mSelectTimeText = findViewById(R.id.schedule_time_text);
		mHourSeekBar = findViewById(R.id.hour_seekBar);
		mMinSeekBar = findViewById(R.id.min_seekBar);
		mSecSeekBar = findViewById(R.id.sec_seekBar);
		mHourText = findViewById(R.id.hour_text);
		mMinText = findViewById(R.id.min_text);
		mSecText = findViewById(R.id.sec_text);
		mHourSeekBar.setOnSeekBarChangeListener(this);
		mMinSeekBar.setOnSeekBarChangeListener(this);
		mSecSeekBar.setOnSeekBarChangeListener(this);
		findViewById(R.id.minus5_button).setOnClickListener(this);
		findViewById(R.id.minus1_button).setOnClickListener(this);
		findViewById(R.id.plus1_button).setOnClickListener(this);
		findViewById(R.id.plus5_button).setOnClickListener(this);
		findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetAllTime();
			}
		});

		initTextViews();

		setFocusSeekBar(mMinSeekBar);
		updateTimerText();
	}

	public PLSelectTimeView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLSelectTimeView(Context context) {
		this(context, null);
	}

	private void initTextViews() {
		mTextMap = new HashMap<>();
		mTextMap.put(mHourSeekBar, mHourText);
		mTextMap.put(mMinSeekBar, mMinText);
		mTextMap.put(mSecSeekBar, mSecText);

		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView focusText = (TextView) v;
				for (Map.Entry<SeekBar, TextView> entry : mTextMap.entrySet()) {
					if (focusText.equals(entry.getValue())) {
						setFocusSeekBar(entry.getKey());
						break;
					}
				}
			}
		};
		for (final TextView textView : mTextMap.values()) {
			textView.setOnClickListener(onClickListener);
		}
	}

	public Calendar getCurrentSelectCalendar() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mCurrentCalendar.getTimeInMillis());
		calendar.add(Calendar.HOUR, hour);
		calendar.add(Calendar.MINUTE, min);
		calendar.add(Calendar.SECOND, sec);
		return calendar;
	}

	public String getSelectTimeString() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();
		return String.format(Locale.JAPAN, "%02d:%02d:%02d", hour, min, sec);
	}

	public void setAllProgressFromCalendar(Calendar prevCalendar) {
		if (prevCalendar == null) {
			return;
		}
		int[] diffTimes = MYCalendarUtil.getDiffTimesForHms(mCurrentCalendar, prevCalendar);
		setAllProgress(diffTimes);
	}

	public void resetAllTime() {
		int[] progresses = {0, 0, 0};
		setAllProgress(progresses);
		setFocusSeekBar(mMinSeekBar);
	}

	public boolean isZeroAll() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();
		return (hour == 0 && min == 0 && sec == 0);
	}

	public void hideSec() {
		mSecSeekBar.setVisibility(View.GONE);
		mSecText.setVisibility(View.GONE);
	}

	private void setAllProgress(int[] progresses) {
		mHourSeekBar.setProgress(progresses[0]);
		mMinSeekBar.setProgress(progresses[1]);
		mSecSeekBar.setProgress(progresses[2]);
		updateNumberText(mHourSeekBar);
		updateNumberText(mMinSeekBar);
		updateNumberText(mSecSeekBar);

		updateTimerText();
	}

	private void updateNumberText(SeekBar seekBar) {
		TextView textView;
		String baseText;
		switch (seekBar.getId()) {
			case R.id.hour_seekBar: {
				textView = mHourText;
				baseText = "時";
				break;
			}
			case R.id.min_seekBar: {
				textView = mMinText;
				baseText = "分";
				break;
			}
			case R.id.sec_seekBar: {
				textView = mSecText;
				baseText = "秒";
				break;
			}
			default: return;
		}
		textView.setText(seekBar.getProgress() + baseText);
	}

	@SuppressWarnings("ResourceType")
	private void tuningSeekBar(int plusProgress) {
		mCurrentCalendar = Calendar.getInstance();
		Calendar selectCalendar =  getCurrentSelectCalendar();

		int field = calendarFieldFromSeekBar(mFocusSeekBar);
		selectCalendar.add(field, plusProgress);
		setAllProgressFromCalendar(selectCalendar);
	}

	private int calendarFieldFromSeekBar(SeekBar seekBar) {
		switch (seekBar.getId()) {
			case R.id.hour_seekBar: return Calendar.HOUR;
			case R.id.min_seekBar: return Calendar.MINUTE;
			case R.id.sec_seekBar: return Calendar.SECOND;
			default: return -1;
		}
	}

	public void updateTimerText() {
		mCurrentTimeText.setText(MYCalendarUtil.getDateTextFromCalendar(mCurrentCalendar));
		mSelectTimeText.setText(MYCalendarUtil.getDateTextFromCalendar(getCurrentSelectCalendar()));
	}

	private void setFocusSeekBar(SeekBar focusSeekBar) {
		mFocusSeekBar = focusSeekBar;
		updateAllText(mTextMap.get(focusSeekBar));
	}

	private void updateAllText(TextView focusText) {
		for (TextView textView : mTextMap.values()) {
			if (textView.equals(focusText)) {
				textView.setTextColor(Color.BLUE);
				textView.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
			} else {
				textView.setTextColor(Color.BLACK);
				textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
			}
		}
	}

	// OnClickListener
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.minus5_button: {
				tuningSeekBar(-5);
				break;
			}
			case R.id.minus1_button: {
				tuningSeekBar(-1);
				break;
			}
			case R.id.plus1_button: {
				tuningSeekBar(1);
				break;
			}
			case R.id.plus5_button: {
				tuningSeekBar(5);
				break;
			}
		}
	}

	// OnSeekBarChangeListener
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			setFocusSeekBar(seekBar);
			mCurrentCalendar = Calendar.getInstance();
		}
		updateNumberText(seekBar);
		updateTimerText();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// ツマミに触れたときに呼ばれる
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// ツマミを離したときに呼ばれる
	}
}
