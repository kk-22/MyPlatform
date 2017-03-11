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

	public PLMSBattleResult(PLMSFieldView fieldView,
							PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);
		mFieldView = fieldView;

		mSceneArray = new MYArrayList<>();
		createScene();
	}

	private void createScene() {
		MYArrayList<PLMSBattleUnit> attackerArray = createAttackerArray();
		int maxScene = attackerArray.size();
		for (int i = 0; i < maxScene; i++) {
			PLMSBattleUnit attackerUnit = attackerArray.get(i);
			PLMSBattleUnit defenderUnit = getEnemyUnitFromUnit(attackerUnit);
			PLMSBattleScene scene = new PLMSBattleScene(attackerUnit, defenderUnit);
			mSceneArray.add(scene);

			int attackerHP = Math.max(attackerUnit.getResultHP() + scene.getDamagePoint(), 0);
			int defenderHP = Math.min(defenderUnit.getResultHP() + scene.getRecoveryPoint(),
					defenderUnit.getUnitView().getUnitData().getMaxHP());
			attackerUnit.setResultHP(attackerHP);
			defenderUnit.setResultHP(defenderHP);
			if (defenderHP <= 0) {
				 break;
			}
		}
	}

	// 攻撃順を返す
	private MYArrayList<PLMSBattleUnit> createAttackerArray() {
		MYArrayList<PLMSBattleUnit> attackerArray = new MYArrayList<>();
		PLMSBattleUnit firstAttacker = mLeftUnit;
		PLMSBattleUnit secondAttacker = getEnemyUnitFromUnit(firstAttacker);
		attackerArray.add(firstAttacker);
		attackerArray.add(secondAttacker);
		if (firstAttacker.canChaseAttack(secondAttacker)) {
			attackerArray.add(firstAttacker);
		}
		if (secondAttacker.canChaseAttack(firstAttacker)) {
			attackerArray.add(secondAttacker);
		}
		return attackerArray;
	}

	// 引数に渡したユニットとは別のユニットを返す
	private PLMSBattleUnit getEnemyUnitFromUnit(PLMSBattleUnit battleUnit) {
		if (battleUnit.equals(mLeftUnit)) {
			return mRightUnit;
		}
		return mLeftUnit;
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
