package jp.co.my.myplatform.service.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.co.my.common.util.MYCalendarUtil;
import jp.co.my.myplatform.R;

public class PLSelectTimeView extends LinearLayout
		implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

	private TextView mCurrentTimeText;
	private TextView mSelectTimeText;
	private SeekBar mHourSeekBar;
	private SeekBar mMinSeekBar;
	private SeekBar mSecSeekBar;
	private SeekBar mFocusSeekBar;		// 最後に操作したシークバー

	public PLSelectTimeView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);

		LayoutInflater.from(context).inflate(R.layout.view_select_time, this);
		mCurrentTimeText = (TextView)findViewById(R.id.current_time_text);
		mSelectTimeText = (TextView)findViewById(R.id.schedule_time_text);
		mHourSeekBar = (SeekBar)findViewById(R.id.hour_seekBar);
		mMinSeekBar = (SeekBar)findViewById(R.id.min_seekBar);
		mSecSeekBar = (SeekBar)findViewById(R.id.sec_seekBar);
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

		mFocusSeekBar = mMinSeekBar;
		updateTimerText();
	}

	public PLSelectTimeView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLSelectTimeView(Context context) {
		this(context, null);
	}

	public Calendar getSelectTimeCalendar() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, hour);
		calendar.add(Calendar.MINUTE, min);
		calendar.add(Calendar.SECOND, sec);
		return calendar;
	}

	public String getSelectTimeString() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();
		return (hour + "時間" + min + "分" + sec + "秒");
	}

	public void setPrevCalendar(Calendar prevCalendar) {
		if (prevCalendar == null) {
			return;
		}
		int[] diffTimes = MYCalendarUtil.getDiffTimesForHms(Calendar.getInstance(), prevCalendar);
		setAllProgress(diffTimes);
	}

	public void resetAllTime() {
		int[] progresses = {0, 0, 0};
		setAllProgress(progresses);
	}

	public boolean isZeroAll() {
		int hour = mHourSeekBar.getProgress();
		int min = mMinSeekBar.getProgress();
		int sec = mSecSeekBar.getProgress();
		return (hour == 0 && min == 0 && sec == 0);
	}

	private void setAllProgress(int[] progresses) {
		mHourSeekBar.setProgress(progresses[0]);
		mMinSeekBar.setProgress(progresses[1]);
		mSecSeekBar.setProgress(progresses[2]);
		updateNumberText(mHourSeekBar);
		updateNumberText(mMinSeekBar);
		updateNumberText(mSecSeekBar);
		mFocusSeekBar = mMinSeekBar;

		updateTimerText();
	}

	private void updateNumberText(SeekBar seekBar) {
		TextView textView = null;
		String baseText = null;
		switch (seekBar.getId()) {
			case R.id.hour_seekBar: {
				textView = (TextView)findViewById(R.id.hour_text);
				baseText = "時";
				break;
			}
			case R.id.min_seekBar: {
				textView = (TextView)findViewById(R.id.min_text);
				baseText = "分";
				break;
			}
			case R.id.sec_seekBar: {
				textView = (TextView)findViewById(R.id.sec_text);
				baseText = "秒";
				break;
			}
		}
		textView.setText(seekBar.getProgress() + baseText);
	}

	private void tuningSeekBar(int plusProgress) {
		int nextValue = mFocusSeekBar.getProgress() + plusProgress;
		if (nextValue < 0) {
			nextValue = 0;
		} else if (mFocusSeekBar.getMax() < nextValue) {
			nextValue = mFocusSeekBar.getMax();
		}
		mFocusSeekBar.setProgress(nextValue);
		updateNumberText(mFocusSeekBar);
	}

	// For time calendar
	private void setTimeToTextView(TextView textView, Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("HH時mm分ss秒");
		textView.setText(format.format(calendar.getTime()));
	}

	public void updateTimerText() {
		Calendar currentCalendar = Calendar.getInstance();
		setTimeToTextView(mCurrentTimeText, currentCalendar);
		setTimeToTextView(mSelectTimeText, getSelectTimeCalendar());
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
		mFocusSeekBar = seekBar;
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
