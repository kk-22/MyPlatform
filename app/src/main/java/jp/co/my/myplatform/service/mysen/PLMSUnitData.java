package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;

public class PLMSUnitData {

	public static final int PARAMETER_ATTACK = 0;
	public static final int PARAMETER_SPEED = 1;
	public static final int PARAMETER_DEFENSE = 2;
	public static final int PARAMETER_MAGIC_DEFENSE = 3;
	public static final int PARAMETER_NUMBER = 4; // パラメータ数

	public static final int SKILL_WEAPON = 0;
	public static final int SKILL_SUPPORT = 1;
	public static final int SKILL_SECRET = 2;
	public static final int SKILL_A = 3;
	public static final int SKILL_B = 4;
	public static final int SKILL_C = 5;
	public static final int SKILL_NUMBER = 6;

	private PLMSUnitModel mUnitModel;
	private Point mFirstPoint;				// 初期位置
	private PLMSArmyStrategy mArmyStrategy;

	private PLMSColorData mColor;
	private PLMSWeaponData mWeapon;
	private PLMSBranchData mBranch;
	private PLMSSkillData[] mAllSkills;
	private MYArrayList<PLMSSkillData> mPassiveSkillArray;

	private int mMaxHP;
	private int mCurrentHP;
	private int[] mBaseParams; // バフ適用前の値
	private int[] mCurrentParams; // バフ適用後の値
	private int[] mBuffParams; // バフによる加算値

	private int mMoveCount; // 同一ターン内での移動回数

	public PLMSUnitData(PLMSUnitModel unitModel, Point firstPoint, PLMSArmyStrategy armyStrategy) {
		mUnitModel = unitModel;
		mFirstPoint = firstPoint;
		mArmyStrategy = armyStrategy;
		mAllSkills = new PLMSSkillData[SKILL_NUMBER];

		mColor = new PLMSColorData((mUnitModel.getColorType()));
		mWeapon = new PLMSWeaponData(mUnitModel.getWeaponType());
		mBranch = new PLMSBranchData(mUnitModel.getBranchType());
		mAllSkills[SKILL_WEAPON] = createSkillData(null);
		mAllSkills[SKILL_SUPPORT] = createSkillData(mUnitModel.getSupportSkillForeign());
		mAllSkills[SKILL_SECRET] = createSkillData(mUnitModel.getSecretSkillForeign());
		mAllSkills[SKILL_A] = createSkillData(mUnitModel.getPassiveASkillForeign());
		mAllSkills[SKILL_B] = createSkillData(mUnitModel.getPassiveBSkillForeign());
		mAllSkills[SKILL_C] = createSkillData(mUnitModel.getPassiveCSkillForeign());

		mPassiveSkillArray = new MYArrayList<>(3);
		mPassiveSkillArray.add(mAllSkills[SKILL_A]);
		mPassiveSkillArray.add(mAllSkills[SKILL_B]);
		mPassiveSkillArray.add(mAllSkills[SKILL_C]);

		mBaseParams = new int[PARAMETER_NUMBER];
		mBaseParams[PARAMETER_ATTACK] = mUnitModel.getAttackPoint();
		mBaseParams[PARAMETER_SPEED] = mUnitModel.getSpeedPoint();
		mBaseParams[PARAMETER_DEFENSE] = mUnitModel.getDefensePoint();
		mBaseParams[PARAMETER_MAGIC_DEFENSE] = mUnitModel.getMagicDefensePoint();
		mCurrentParams = new int[PARAMETER_NUMBER];
		mBuffParams = new int[PARAMETER_NUMBER];
		
		mMaxHP = mUnitModel.getHitPoint();
		mCurrentHP = mMaxHP;
		resetParams();
		setAllStatus();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getNo() +".png";
	}

	public void resetParams() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mBuffParams[i] = 0;
		}

		mMoveCount = 0;
	}

	public void setAllStatus() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mCurrentParams[i] = mBaseParams[i] + mBuffParams[i];
		}
	}

	private PLMSSkillData createSkillData(ForeignKeyContainer<PLMSSkillModel> foreign) {
		if (foreign == null) {
			return new PLMSSkillData(null);
		}
		return new PLMSSkillData(foreign.load());
	}

	// getter
	public PLMSUnitModel getUnitModel() {
		return mUnitModel;
	}

	public PLMSBranchData getBranch() {
		return mBranch;
	}

	public int getMaxHP() {
		return mMaxHP;
	}

	public int getCurrentHP() {
		return mCurrentHP;
	}

	public int getCurrentAttack() {
		return mCurrentParams[PARAMETER_ATTACK];
	}

	public int getCurrentSpeed() {
		return mCurrentParams[PARAMETER_SPEED];
	}

	public int getCurrentDefense() {
		return mCurrentParams[PARAMETER_DEFENSE];
	}

	public int getCurrentMagicDefense() {
		return mCurrentParams[PARAMETER_MAGIC_DEFENSE];
	}

	public PLMSArmyStrategy getArmyStrategy() {
		return mArmyStrategy;
	}

	public int getMoveCount() {
		return mMoveCount;
	}

	public Point getFirstPoint() {
		return mFirstPoint;
	}

	public PLMSWeaponData getWeapon() {
		return mWeapon;
	}

	public PLMSColorData getColor() {
		return mColor;
	}

	public PLMSSkillData getSkillOfNo(int no) {
		return mAllSkills[no];
	}

	public MYArrayList<PLMSSkillData> getPassiveSkillArray() {
		return mPassiveSkillArray;
	}

	public int getCurrentParameterOfNo(int no) {
		return mCurrentParams[no];
	}

	public int getBaseParameterOfNo(int no) {
		return mBaseParams[no];
	}

	public int getBuffParameterOfNo(int no) {
		return mBuffParams[no];
	}

	// setter
	public void setCurrentHP(int currentHP) {
		mCurrentHP = currentHP;
	}

	public void setBuffOfNo(int parameterNo, int buff) {
		if (mBuffParams[parameterNo] < buff) {
			mBuffParams[parameterNo] = buff;
		}
	}
}
