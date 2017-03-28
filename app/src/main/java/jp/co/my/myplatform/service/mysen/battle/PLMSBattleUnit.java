package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.PLMSColorData;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSBattleUnit {

	private PLMSUnitView mUnitView;
	private PLMSLandView mLandView;
	private PLMSBattleUnit mEnemyUnit;

	private int mResultHP;

	private int[] mBattleBuffs;
	private int mTotalAttack;					// 3すくみ・スキル補正後の値（奥義スキルは除く）

	public PLMSBattleUnit(PLMSUnitView unitView, PLMSLandView landView) {
		mUnitView = unitView;
		mLandView = landView;

		mResultHP = mUnitView.getUnitData().getCurrentHP();

		mBattleBuffs = new int[PLMSUnitData.PARAMETER_NUMBER];
	}

	public boolean canAttackWithDistance(int distance) {
		int myRange = mUnitView.getUnitData().getWeapon().getAttackRange();
		return (myRange == distance);
	}

	public boolean canChaseAttack(PLMSBattleUnit enemyUnit) {
		if (enemyUnit.getBattleSpeed() + 5 <= getBattleSpeed()) {
			return true;
		}
		return false;
	}

	// TODO: 敵の武器種類（物理/魔法）によって分岐
	public int getDefenseForEnemyAttack() {
		if (mEnemyUnit.getUnitView().getUnitData().getWeapon().isPhysicalAttack()) {
			return getBattleDefense();
		} else {
			return getBattleMagicDefense();
		}
	}

	// TODO: 3すくみ補正値はBattleResultに初期値を設定し、スキル側で書き換え可能にする
	public void initParamsWithEnemyUnit() {
		PLMSColorData enemyColor = mEnemyUnit.getUnitView().getUnitData().getColor();
		double ratio = mUnitView.getUnitData().getColor().damageRatio(enemyColor);
		// 正負どちらでも0に近い値を採用する
		// Math.floor は負の値の時にも低い値を採用するため使用不可
		int plusDamage = (int)(getBattleAttack() * ratio);
		mTotalAttack = getBattleAttack() + plusDamage;
	}

	private int getBattleParameterOfNo(int no) {
		return mUnitView.getUnitData().getCurrentParameterOfNo(no) + mBattleBuffs[no];
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
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_ATTACK);
	}

	public int getBattleSpeed() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_SPEED);
	}

	public int getBattleDefense() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_DEFENSE);
	}

	public int getBattleMagicDefense() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_MAGIC_DEFENSE);
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

	public void setEnemyUnit(PLMSBattleUnit enemyUnit) {
		mEnemyUnit = enemyUnit;
	}

	public void setBattleBuffOfNo(int parameterNo, int buff) {
		if (mBattleBuffs[parameterNo] < buff) {
			mBattleBuffs[parameterNo] = buff;
		}
	}
}
