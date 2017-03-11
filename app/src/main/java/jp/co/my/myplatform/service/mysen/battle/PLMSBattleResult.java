package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSBattleResult {

	private PLMSBattleUnit mLeftUnit;
	private PLMSBattleUnit mRightUnit;
	private PLMSFieldView mFieldView;
	private MYArrayList<PLMSBattleScene> mSceneArray;

	public PLMSBattleResult(PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							PLMSUnitView rightUnitView, PLMSLandView rightLandView,
							PLMSFieldView fieldView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);
		mFieldView = fieldView;

		mSceneArray = new MYArrayList<>();
		createScene();
	}

	private void createScene() {
		for (int i = 0; i < 1; i++) {
			PLMSBattleUnit attackerUnit = mLeftUnit;
			PLMSBattleUnit defenderUnit = mRightUnit;
			PLMSBattleScene scene = new PLMSBattleScene(attackerUnit, defenderUnit);
			mSceneArray.add(scene);

			int attackerHP = Math.max(attackerUnit.getResultHP() + scene.getDamagePoint(), 0);
			int defenderHP = Math.min(defenderUnit.getResultHP() + scene.getRecoveryPoint(),
					defenderUnit.getUnitView().getUnitData().getMaxHP());
			attackerUnit.setResultHP(attackerHP);
			defenderUnit.setResultHP(defenderHP);
			if (attackerHP <= 0) {
				 break;
			}
		}
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
}
