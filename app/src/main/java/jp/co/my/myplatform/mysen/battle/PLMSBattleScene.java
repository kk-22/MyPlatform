package jp.co.my.myplatform.mysen.battle;

import jp.co.my.myplatform.mysen.PLMSBranchData;

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

	// TODO: givingDamageOfBattleUnit と共通化
	private void calculateDamage() {
		PLMSBranchData attackerBranch = mAttackerUnit.getUnitData().getBranch();
		int attack = mAttackerUnit.getTotalAttack();
		int defense = mDefenderUnit.getDefenseForEnemyAttack();
		int damage = Math.max(0, attack - defense);
		mDefenderDiffHP = (int) (damage * attackerBranch.getDamageRatio() * -1);
		mDefenderRemainingHP = mDefenderUnit.getUnitData().calculateBattleRemainingHP(mDefenderUnit.getRemainingHP(), mDefenderDiffHP);

		mAttackerDiffHP = 0;
		mAttackerRemainingHP = mAttackerUnit.getUnitData().calculateBattleRemainingHP(mAttackerUnit.getRemainingHP(), mAttackerDiffHP);
		mAttackerUnit.setRemainingHP(mAttackerRemainingHP);
		mDefenderUnit.setRemainingHP(mDefenderRemainingHP);
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
