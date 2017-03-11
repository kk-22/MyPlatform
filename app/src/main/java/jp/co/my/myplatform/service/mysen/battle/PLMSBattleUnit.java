package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSBattleUnit {

	private PLMSUnitView mUnitView;
	private PLMSLandView mLandView;

	private int mResultHP;
	private int mBattleAttack;
	private int mBattleSpeed;
	private int mBattleDefense;
	private int mBattleMagicDefense;

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

	// setter
	public void setResultHP(int resultHP) {
		mResultHP = resultHP;
	}
}
