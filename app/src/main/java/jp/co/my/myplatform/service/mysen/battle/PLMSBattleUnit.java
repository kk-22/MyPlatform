package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSBranchData;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

public class PLMSBattleUnit extends PLMSForecastUnit {

	private PLMSBattleUnit mAnotherUnit;
	private int[] mBattleBuffs;
	private int mThreeWayCompatibility; // 3すくみ(-1:不利、0:対等、1:有利)
	private int mTotalAttack; // 3すくみ・スキル補正後の値（奥義スキルは除く）
	private int mChasePoint; // 追撃補正値（0の時速さ参照。1以上で絶対追撃）
	private int mNumberOfConsecutiveAttack; // 連続攻撃回数
	private boolean mIsWeaknessAttack; // 特効攻撃をする

	private MYArrayList<PLMSSkillData.EffectType> mSkillEffectArray;

	public PLMSBattleUnit(PLMSUnitView unitView, PLMSLandView landView) {
		super(unitView, landView);

		mBattleBuffs = new int[PLMSUnitData.PARAMETER_NUMBER];
		mSkillEffectArray = new MYArrayList<>();
		mChasePoint = 0;
		mNumberOfConsecutiveAttack = 1;
	}

	public boolean canChaseAttack() {
		if (mChasePoint > 0) {
			return true;
		} else if (mChasePoint < 0) {
			return false;
		}
		return mAnotherUnit.getBattleSpeed() + 5 <= getBattleSpeed();
	}

	public int getDefenseForEnemyAttack() {
		if (mAnotherUnit.getUnitView().getUnitData().getBranch().isPhysicalAttack()) {
			return getBattleDefense();
		} else {
			return getBattleMagicDefense();
		}
	}

	public void initParamsWithThreeWayRatio(int threeWayRatio) {
		// 正負どちらでも0に近い値を採用する
		// Math.floor は負の値の時にも低い値を採用するため使用不可
		int plusDamage = getBattleAttack() * threeWayRatio / 100 * mThreeWayCompatibility;
		mTotalAttack = getBattleAttack() + plusDamage;

		if (getUnitData().getWeaponSkillData().isWeaknessBranch(mAnotherUnit)
				&& !mAnotherUnit.getSkillEffectArray().contains(PLMSSkillData.EffectType.PROTECT_WEAKNESS_ATTACK)) {
			mIsWeaknessAttack = true;
			mTotalAttack = (int)Math.floor(mTotalAttack * 1.5);
		}
	}

	private int getBattleParameterOfNo(int no) {
		return mUnitView.getUnitData().getCurrentParameterOfNo(no) + mBattleBuffs[no];
	}

	public void incrementChasePoint() {
		mChasePoint += 1;
	}

	public void decrementChasePoint() {
		mChasePoint -= 1;
	}

	// getter
	@Override
	public PLMSBattleUnit getAnotherUnit() {
		return mAnotherUnit;
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

	public MYArrayList<PLMSSkillData.EffectType> getSkillEffectArray() {
		return mSkillEffectArray;
	}

	public int getNumberOfConsecutiveAttack() {
		return mNumberOfConsecutiveAttack;
	}

	public boolean isWeaknessAttack() {
		return mIsWeaknessAttack;
	}

	// setter
	public void setAnotherUnit(PLMSBattleUnit anotherUnit) {
		mAnotherUnit = anotherUnit;

		PLMSBranchData enemyBranch = mAnotherUnit.getUnitView().getUnitData().getBranch();
		mThreeWayCompatibility = mUnitView.getUnitData().getBranch().threeWayCompatibility(enemyBranch);
	}

	public void setRemainingHP(int remainingHP) {
		mRemainingHP = remainingHP;
	}

	public void setNumberOfConsecutiveAttack(int numberOfConsecutiveAttack) {
		mNumberOfConsecutiveAttack = numberOfConsecutiveAttack;
	}

	public void addBattleBuffOfNo(int parameterNo, int buff) {
		mBattleBuffs[parameterNo] += buff;
	}

	public void addSkill(PLMSSkillData skillData) {
		mSkillEffectArray.add(skillData.getEffectType());
	}

	public void setThreeWayCompatibility(int threeWayCompatibility) {
		// お互い相手色に有利になるスキルを持っていた場合に値が0にするため、セットではなく加算する
		mThreeWayCompatibility += threeWayCompatibility;
	}
}
