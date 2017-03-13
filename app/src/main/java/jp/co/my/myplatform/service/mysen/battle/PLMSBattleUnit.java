package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.PLMSColorData;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSBattleUnit {

	private PLMSUnitView mUnitView;
	private PLMSLandView mLandView;
	private PLMSBattleUnit mEnemyUnit;

	private int mResultHP;
	private int mBattleAttack;
	private int mBattleSpeed;
	private int mBattleDefense;
	private int mBattleMagicDefense;

	private int mTotalAttack;					// 3すくみ・スキル補正後の値（奥義スキルは除く）

	public PLMSBattleUnit(PLMSUnitView unitView, PLMSLandView landView) {
		mUnitView = unitView;
		mLandView = landView;

		mResultHP = mUnitView.getUnitData().getCurrentHP();
		mBattleAttack = mUnitView.getUnitData().getCurrentAttack();
		mBattleSpeed = mUnitView.getUnitData().getCurrentSpeed();
		mBattleDefense = mUnitView.getUnitData().getCurrentDefense();
		mBattleMagicDefense = mUnitView.getUnitData().getCurrentMagicDefense();
	}

	public boolean canAttackWithDistance(int distance) {
		int myRange = mUnitView.getUnitData().getWeapon().getAttackRange();
		return (myRange == distance);
	}

	public boolean canChaseAttack(PLMSBattleUnit enemyUnit) {
		if (enemyUnit.getBattleSpeed() + 5 <= mBattleSpeed) {
			return true;
		}
		return false;
	}

	// TODO: 敵の武器種類（物理/魔法）によって分岐
	public int getDefenseForEnemyAttack() {
		if (mEnemyUnit.getUnitView().getUnitData().getWeapon().isPhysicalAttack()) {
			return mBattleDefense;
		} else {
			return mBattleMagicDefense;
		}
	}

	public void initParamsWithEnemyUnit(PLMSBattleUnit enemyUnit) {
		mEnemyUnit = enemyUnit;

		PLMSColorData enemyColor = enemyUnit.getUnitView().getUnitData().getColor();
		double ratio = mUnitView.getUnitData().getColor().damageRatio(enemyColor);
		// 正負どちらでも0に近い値を採用する
		// Math.floor は負の値の時にも低い値を採用するため使用不可
		int plusDamage = (int)(mBattleAttack * ratio);
		mTotalAttack = mBattleAttack + plusDamage;
	}

	// getter
	public PLMSUnitView getUnitView() {
		return mUnitView;
	}

	public PLMSLandView getLandView() {
		return mLandView;
	}

	public int getResultHP() {
		return mResultHP;
	}

	public int getBattleAttack() {
		return mBattleAttack;
	}

	public int getBattleSpeed() {
		return mBattleSpeed;
	}

	public int getBattleDefense() {
		return mBattleDefense;
	}

	public int getBattleMagicDefense() {
		return mBattleMagicDefense;
	}

	public int getTotalAttack() {
		return mTotalAttack;
	}

	public PLMSBattleUnit getEnemyUnit() {
		return mEnemyUnit;
	}

	// setter
	public void setResultHP(int resultHP) {
		mResultHP = resultHP;
	}
}
