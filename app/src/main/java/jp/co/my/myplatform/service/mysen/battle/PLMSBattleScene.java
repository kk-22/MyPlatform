package jp.co.my.myplatform.service.mysen.battle;

public class PLMSBattleScene {

	private PLMSBattleUnit mAttackerUnit;
	private PLMSBattleUnit mDefenderUnit;

	private int mAttackerDiffHP;
	private int mDefenderDiffHP;
	private int mAttackerRemainingHP;
	private int mDefenderRemainingHP;

	public PLMSBattleScene(PLMSBattleUnit attackerUnit, PLMSBattleUnit defenderUnit) {
		mAttackerUnit = attackerUnit;
		mDefenderUnit = defenderUnit;

		calculateDamage();
	}

	private void calculateDamage() {
		int attack = mAttackerUnit.getTotalAttack();
		int defense = mDefenderUnit.getDefenseForEnemyAttack();
		int damage = Math.max(0, attack - defense);
		mDefenderDiffHP = damage * -1;
		mDefenderRemainingHP = mDefenderUnit.getUnitData().calculateBattleRemainingHP(mDefenderUnit.getResultHP(), mDefenderDiffHP);

		mAttackerDiffHP = 0;
		mAttackerRemainingHP = mAttackerUnit.getUnitData().calculateBattleRemainingHP(mAttackerUnit.getResultHP(), mAttackerDiffHP);
		mAttackerUnit.setResultHP(mAttackerRemainingHP);
		mDefenderUnit.setResultHP(mDefenderRemainingHP);
	}

	// getter
	public PLMSBattleUnit getAttackerUnit() {
		return mAttackerUnit;
	}

	public PLMSBattleUnit getDefenderUnit() {
		return mDefenderUnit;
	}

	public int getAttackerDiffHP() {
		return mAttackerDiffHP;
	}

	public int getDefenderDiffHP() {
		return mDefenderDiffHP;
	}

	public int getAttackerRemainingHP() {
		return mAttackerRemainingHP;
	}

	public int getDefenderRemainingHP() {
		return mDefenderRemainingHP;
	}
}
