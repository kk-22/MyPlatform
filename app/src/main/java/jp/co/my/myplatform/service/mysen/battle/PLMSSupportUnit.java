package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;


public class PLMSSupportUnit extends PLMSForecastUnit {

	private int mDiffHP; // 補助スキルによるHPの変動値

	public PLMSSupportUnit(PLMSUnitView unitView, PLMSLandView landView) {
		super(unitView, landView);
	}

	// getter
	public int getDiffHP() {
		return mDiffHP;
	}

	// setter
	public void setDiffHP(int diffHP) {
		mDiffHP = diffHP;
		mRemainingHP = getUnitData().calculateSkillRemainingHP(mRemainingHP, diffHP);
	}
}
