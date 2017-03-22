package jp.co.my.myplatform.service.mysen.information;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;


public class PLMSUnitInfoView extends LinearLayout {

	private TextView mLeftNameTextView;
	private TextView mCurrentHPTextView;
	private TextView mMaxHPTextView;
	private TextView mAttackTextView;
	private TextView mSpeedTextView;
	private TextView mDefenseTextView;
	private TextView mMagicDefenseTextView;

	private PLMSUnitView mUnitView;

	public PLMSUnitInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_info, this);

		mLeftNameTextView = (TextView) findViewById(R.id.name_text);
		mCurrentHPTextView = (TextView) findViewById(R.id.current_hp_text);
		mMaxHPTextView = (TextView) findViewById(R.id.result_hp_text);
		mAttackTextView = (TextView) findViewById(R.id.attack_text);
		mSpeedTextView = (TextView) findViewById(R.id.speed_text);
		mDefenseTextView = (TextView) findViewById(R.id.defense_text);
		mMagicDefenseTextView = (TextView) findViewById(R.id.magic_defense_text);
	}

	public PLMSUnitInfoView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSUnitInfoView(Context context) {
		this(context, null);
	}

	public void updateUnitInfo(PLMSUnitView unitView) {
		mUnitView = unitView;

		mLeftNameTextView.setText(unitView.getUnitData().getUnitModel().getName());
		setIntToText(unitView.getUnitData().getCurrentHP(), mCurrentHPTextView);
		setIntToText(unitView.getUnitData().getMaxHP(), mMaxHPTextView);
		setIntToText(unitView.getUnitData().getCurrentAttack(), mAttackTextView);
		setIntToText(unitView.getUnitData().getCurrentSpeed(), mSpeedTextView);
		setIntToText(unitView.getUnitData().getCurrentDefense(), mDefenseTextView);
		setIntToText(unitView.getUnitData().getCurrentMagicDefense(), mMagicDefenseTextView);
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}
}
