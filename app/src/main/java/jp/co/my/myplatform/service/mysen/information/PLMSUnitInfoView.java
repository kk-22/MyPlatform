package jp.co.my.myplatform.service.mysen.information;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;
import jp.co.my.myplatform.service.popover.PLTextViewPopover;

import static jp.co.my.myplatform.service.mysen.PLMSUnitData.PARAMETER_ATTACK;
import static jp.co.my.myplatform.service.mysen.PLMSUnitData.PARAMETER_DEFENSE;
import static jp.co.my.myplatform.service.mysen.PLMSUnitData.PARAMETER_MAGIC_DEFENSE;
import static jp.co.my.myplatform.service.mysen.PLMSUnitData.PARAMETER_NUMBER;
import static jp.co.my.myplatform.service.mysen.PLMSUnitData.PARAMETER_SPEED;
import static jp.co.my.myplatform.service.mysen.PLMSUnitData.SKILL_NUMBER;


public class PLMSUnitInfoView extends LinearLayout implements View.OnClickListener {

	private TextView mLeftNameTextView;
	private TextView mCurrentHPTextView;
	private TextView mMaxHPTextView;
	private TextView[] mParamTextViews;
	private MYArrayList<TextView> mSkillTextViewArray;

	private PLMSUnitView mUnitView;

	public PLMSUnitInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_unit_info, this);

		mParamTextViews = new TextView[PARAMETER_NUMBER];
		mLeftNameTextView = (TextView) findViewById(R.id.name_text);
		mCurrentHPTextView = (TextView) findViewById(R.id.current_hp_text);
		mMaxHPTextView = (TextView) findViewById(R.id.result_hp_text);
		mParamTextViews[PARAMETER_ATTACK] = (TextView) findViewById(R.id.attack_text);
		mParamTextViews[PARAMETER_SPEED] = (TextView) findViewById(R.id.speed_text);
		mParamTextViews[PARAMETER_DEFENSE] = (TextView) findViewById(R.id.defense_text);
		mParamTextViews[PARAMETER_MAGIC_DEFENSE] = (TextView) findViewById(R.id.magic_defense_text);

		mSkillTextViewArray = new MYArrayList<>(SKILL_NUMBER);
		mSkillTextViewArray.add((TextView) findViewById(R.id.weapon_text));
		mSkillTextViewArray.add((TextView) findViewById(R.id.support_text));
		mSkillTextViewArray.add((TextView) findViewById(R.id.secret_text));
		mSkillTextViewArray.add((TextView) findViewById(R.id.passive_a_text));
		mSkillTextViewArray.add((TextView) findViewById(R.id.passive_b_text));
		mSkillTextViewArray.add((TextView) findViewById(R.id.passive_c_text));

		for (TextView textView : mSkillTextViewArray) {
			textView.setOnClickListener(this);
		}
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
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			setIntToText(unitData.getCurrentParameterOfNo(i), mParamTextViews[i]);
		}

		for (int i = 0; i < SKILL_NUMBER; i++) {
			PLMSSkillModel skillModel = unitData.getSkillOfNo(i).getSkillModel();
			TextView textView = mSkillTextViewArray.get(i);
			if (skillModel== null) {
				textView.setText("-");
			} else {
				textView.setText(skillModel.getName());
			}
		}
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}

	@Override
	public void onClick(View v) {
		TextView textView = (TextView) v;
		int index = mSkillTextViewArray.indexOf(textView);
		PLMSSkillData skillData = mUnitView.getUnitData().getSkillOfNo(index);
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
