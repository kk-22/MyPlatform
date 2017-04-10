package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

public abstract class PLMSBaseForecast {

	PLMSBattleUnit mLeftUnit;
	PLMSBattleUnit mRightUnit;
	MYArrayList<PLMSBattleUnit> mBattleUnitArray;
	MYArrayList<PLMSBattleUnit> mAttackerArray; // 攻撃順配列
	MYArrayList<PLMSBattleScene> mSceneArray;

	PLMSBaseForecast(PLMSUnitView leftUnitView, PLMSLandView leftLandView,
					 PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);

		mLeftUnit.setEnemyUnit(mRightUnit);
		mRightUnit.setEnemyUnit(mLeftUnit);
		mBattleUnitArray = new MYArrayList<>(2);
		mBattleUnitArray.add(mLeftUnit);
		mBattleUnitArray.add(mRightUnit);
	}

	// getter
	public PLMSBattleUnit getLeftUnit() {
		return mLeftUnit;
	}

	public PLMSBattleUnit getRightUnit() {
		return mRightUnit;
	}

	public MYArrayList<PLMSBattleScene> getSceneArray() {
		return mSceneArray;
	}

	public MYArrayList<PLMSBattleUnit> getAttackerArray() {
		return mAttackerArray;
	}

	public PLMSBattleUnit getBattleUnitOfUnitTeam(PLMSUnitInterface unit) {
		PLMSArmyStrategy armyStrategy = unit.getUnitData().getArmyStrategy();
		MYArrayList<PLMSUnitView> teamUnitArray = armyStrategy.getAllUnitViewArray();
		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			if (teamUnitArray.contains(battleUnit.getUnitView())) {
				return battleUnit;
			}
		}
		return null;
	}

	public PLMSUnitInterface getUnitOfBattle(PLMSUnitInterface unit) {
		PLMSUnitView unitView = unit.getUnitView();
		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			if (battleUnit.getUnitView().equals(unitView)) {
				return battleUnit;
			}
		}
		return unit;
	}
}
