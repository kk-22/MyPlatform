package jp.co.my.myplatform.mysen.battle;

import jp.co.my.myplatform.mysen.PLMSLandView;
import jp.co.my.myplatform.mysen.PLMSUnitView;


public class PLMSSupportUnit extends PLMSForecastUnit {

	private PLMSSupportUnit mAnotherUnit;
	private int mDiffHP; // 補助スキルによるHPの変動値

	public PLMSSupportUnit(PLMSUnitView unitView, PLMSLandView landView) {
		super(unitView, landView);
	}

	// getter
	@Override
	public PLMSSupportUnit getAnotherUnit() {
		return mAnotherUnit;
	}

	public int getDiffHP() {
		return mDiffHP;
	}

	// setter
	public void setAnotherUnit(PLMSSupportUnit anotherUnit) {
		mAnotherUnit = anotherUnit;
	}

	public void setDiffHP(int diffHP) {
		mDiffHP = diffHP;
		mRemainingHP = getUnitData().calculateSkillRemainingHP(mRemainingHP, diffHP);
	}
}
