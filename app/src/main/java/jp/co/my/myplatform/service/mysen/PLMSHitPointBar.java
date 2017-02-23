package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;


public class PLMSHitPointBar extends LinearLayout {

	private TextView mNumberText;
	private View mBarView;
	private FrameLayout mHPFrame;

	public PLMSHitPointBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.mysen_hp_bar, this);
		mNumberText = (TextView) findViewById(R.id.hp_text);
		mHPFrame = (FrameLayout) findViewById(R.id.hp_frame);
		mBarView = findViewById(R.id.hp_view);
	}

	public PLMSHitPointBar(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSHitPointBar(Context context) {
		this(context, null);
	}

	public void initFromUnitView(PLMSUnitView unitView) {
		updateFromUnitView(unitView);
	}

	public void updateFromUnitView(PLMSUnitView unitView) {
		PLMSUnitData unitData = unitView.getUnitData();
		mNumberText.setText(String.valueOf(unitData.getCurrentHP()));
	}
}
