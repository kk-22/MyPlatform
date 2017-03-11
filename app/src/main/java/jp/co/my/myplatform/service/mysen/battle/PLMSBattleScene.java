package jp.co.my.myplatform.service.mysen.battle;

public class PLMSBattleScene {

	private PLMSBattleUnit mAttackerUnit;
	private PLMSBattleUnit mDefenderUnit;

	private int mDamagePoint;			// 負の値
	private int mRecoveryPoint;			// 正の値。奥義などによる回復値

	public PLMSBattleScene(PLMSBattleUnit attackerUnit, PLMSBattleUnit defenderUnit) {
		mAttackerUnit = attackerUnit;
		mDefenderUnit = defenderUnit;

		calculateDamage();
	}

	private void calculateDamage() {
		int damage = mAttackerUnit.getBattleAttack() - mDefenderUnit.getBattleDefense();
		mDamagePoint = (damage > 0) ? damage * -1 : 0;
	}

	// getter
	public PLMSBattleUnit getAttackerUnit() {
		return mAttackerUnit;
	}

	public PLMSBattleUnit getDefenderUnit() {
		return mDefenderUnit;
	}

	public int getDamagePoint() {
		return mDamagePoint;
	}

	public int getRecoveryPoint() {
		return mRecoveryPoint;
	}
}
