package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSBattleResult {

	private PLMSBattleUnit mLeftUnit;
	private PLMSBattleUnit mRightUnit;
	private PLMSFieldView mFieldView;
	private MYArrayList<PLMSBattleUnit> mAttackerArray;		// 攻撃順配列
	private MYArrayList<PLMSBattleScene> mSceneArray;

	private int mDistance;

	public PLMSBattleResult(PLMSFieldView fieldView,
							PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);
		mFieldView = fieldView;

		mDistance = leftUnitView.getUnitData().getWeapon().getAttackRange();
		mAttackerArray = createAttackerArray();
		mSceneArray = new MYArrayList<>();
		createScene();
	}

	private void createScene() {
		int maxScene = mAttackerArray.size();
		for (int i = 0; i < maxScene; i++) {
			PLMSBattleUnit attackerUnit = mAttackerArray.get(i);
			PLMSBattleUnit defenderUnit = getEnemyUnitFromUnit(attackerUnit);
			PLMSBattleScene scene = new PLMSBattleScene(attackerUnit, defenderUnit);
			mSceneArray.add(scene);

			int attackerHP = scene.getAttackerRemainingHP();
			int defenderHP = scene.getDefenderRemainingHP();
			attackerUnit.setResultHP(attackerHP);
			defenderUnit.setResultHP(defenderHP);
			if (attackerHP <= 0 || defenderHP <= 0) {
				 break;
			}
		}
	}

	// 攻撃順を返す
	private MYArrayList<PLMSBattleUnit> createAttackerArray() {
		MYArrayList<PLMSBattleUnit> attackerArray = new MYArrayList<>();
		PLMSBattleUnit firstAttacker = mLeftUnit;
		PLMSBattleUnit secondAttacker = getEnemyUnitFromUnit(firstAttacker);
		boolean canAttackSecondAttacker = secondAttacker.canAttackWithDistance(mDistance);

		attackerArray.add(firstAttacker);
		if (canAttackSecondAttacker) {
			attackerArray.add(secondAttacker);
		}

		// 追撃判定
		if (firstAttacker.canChaseAttack(secondAttacker)) {
			attackerArray.add(firstAttacker);
		}
		if (canAttackSecondAttacker && secondAttacker.canChaseAttack(firstAttacker)) {
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

	public MYArrayList<PLMSBattleUnit> getAttackerArray() {
		return mAttackerArray;
	}
}
