package jp.co.my.myplatform.mysen.battle;

import jp.co.my.myplatform.mysen.PLMSLandView;
import jp.co.my.myplatform.mysen.PLMSUnitView;
import jp.co.my.myplatform.mysen.unit.PLMSSkillData;

public class PLMSSupportForecast extends PLMSBaseForecast {

	private PLMSSupportUnit mLeftSupportUnit;
	private PLMSSupportUnit mRightSupportUnit;

	public PLMSSupportForecast(PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							   PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		super();
		mLeftSupportUnit = new PLMSSupportUnit(leftUnitView, leftLandView);
		mRightSupportUnit = new PLMSSupportUnit(rightUnitView, rightLandView);

		mLeftSupportUnit.setAnotherUnit(mRightSupportUnit);
		mRightSupportUnit.setAnotherUnit(mLeftSupportUnit);

		PLMSSkillData supportSkill = leftUnitView.getUnitData().getSupportSkillData();
		supportSkill.updateRemainingHPOfSupportUnit(this);
	}

	// getter
	@Override
	public PLMSSupportUnit getLeftUnit() {
		return mLeftSupportUnit;
	}

	@Override
	public PLMSSupportUnit getRightUnit() {
		return mRightSupportUnit;
	}

	@Override
	public String getInformationTitle() {
		return mLeftSupportUnit.getUnitData().getSupportSkillData().getSkillModel().getName();
	}
}
