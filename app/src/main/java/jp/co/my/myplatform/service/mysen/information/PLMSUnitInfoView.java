package jp.co.my.myplatform.service.mysen.information;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;
import jp.co.my.myplatform.service.popover.PLTextViewPopover;


public class PLMSUnitInfoView extends LinearLayout implements View.OnClickListener {

	private TextView mLeftNameTextView;
	private TextView mCurrentHPTextView;
	private TextView mMaxHPTextView;
	private TextView mAttackTextView;
	private TextView mSpeedTextView;
	private TextView mDefenseTextView;
	private TextView mMagicDefenseTextView;

	private TextView mWeaponTextView;
	private TextView mSupportTextView;
	private TextView mSecretTextView;
	private TextView mPassiveATextView;
	private TextView mPassiveBTextView;
	private TextView mPassiveCTextView;

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

		mWeaponTextView = (TextView) findViewById(R.id.weapon_text);
		mSupportTextView = (TextView) findViewById(R.id.support_text);
		mSecretTextView = (TextView) findViewById(R.id.secret_text);
		mPassiveATextView = (TextView) findViewById(R.id.passive_a_text);
		mPassiveBTextView = (TextView) findViewById(R.id.passive_b_text);
		mPassiveCTextView = (TextView) findViewById(R.id.passive_c_text);

		initTouchEvent();
	}

	public PLMSUnitInfoView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSUnitInfoView(Context context) {
		this(context, null);
	}

	public void updateUnitInfo(PLMSUnitView unitView) {
		mUnitView = unitView;

		PLMSUnitData unitData = unitView.getUnitData();
		mLeftNameTextView.setText(unitData.getUnitModel().getName());
		setIntToText(unitData.getCurrentHP(), mCurrentHPTextView);
		setIntToText(unitData.getMaxHP(), mMaxHPTextView);
		setIntToText(unitData.getCurrentAttack(), mAttackTextView);
		setIntToText(unitData.getCurrentSpeed(), mSpeedTextView);
		setIntToText(unitData.getCurrentDefense(), mDefenseTextView);
		setIntToText(unitData.getCurrentMagicDefense(), mMagicDefenseTextView);

		setSkillText(unitData.getWeaponSkill(), mSupportTextView);
		setSkillText(unitData.getSupportSkill(), mSupportTextView);
		setSkillText(unitData.getSecretSkill(), mSecretTextView);
		setSkillText(unitData.getPassiveASkill(), mPassiveATextView);
		setSkillText(unitData.getPassiveBSkill(), mPassiveBTextView);
		setSkillText(unitData.getPassiveCSkill(), mPassiveCTextView);
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}

	private void setSkillText(PLMSSkillData skillData, TextView textView) {
		PLMSSkillModel skillModel = skillData.getSkillModel();
		if (skillModel== null) {
			textView.setText("-");
		} else {
			textView.setText(skillModel.getName());
		}
	}

	private void initTouchEvent() {
		mWeaponTextView.setOnClickListener(this);
		mSupportTextView.setOnClickListener(this);
		mSecretTextView.setOnClickListener(this);
		mPassiveATextView.setOnClickListener(this);
		mPassiveBTextView.setOnClickListener(this);
		mPassiveCTextView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		PLMSSkillData skillData = null;
		PLMSUnitData unitData = mUnitView.getUnitData();
		switch (v.getId()) {
			case R.id.weapon_text: skillData = unitData.getWeaponSkill(); break;
			case R.id.support_text: skillData = unitData.getSupportSkill(); break;
			case R.id.secret_text: skillData = unitData.getSecretSkill(); break;
			case R.id.passive_a_text: skillData = unitData.getPassiveASkill(); break;
			case R.id.passive_b_text: skillData = unitData.getPassiveBSkill(); break;
			case R.id.passive_c_text: skillData = unitData.getPassiveCSkill(); break;
		}
		if (skillData == null) {
			return;
		}
		PLMSSkillModel skillModel = skillData.getSkillModel();
		if (skillModel == null) {
			return;
		}

		String description = skillModel.getDescription();
		new PLTextViewPopover(description).showPopover(new PLRelativeLayoutController(v));
	}
}
