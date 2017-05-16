package jp.co.my.myplatform.service.mysen;

public class PLMSBranchData {

	public enum MovementType {
		INFANTRY(1),
		ARMOR(2),
		CAVALRY(4),
		FLYING(8);

		final int id;
		MovementType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}
	}

	private enum WeaponType {
		SWORD(32), SPEAR(64), AXE(128),
		BOW(256), SHURIKEN(512),
		RED_MAGIC(1024), BLUE_MAGIC(2048), GREEN_MAGIC(4096),
		STAFF(8192),
		RED_BREATH(16384), BLUE_BREATH(32768), GREEN_BREATH(65536);

		final int id;
		WeaponType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}
	}

	private enum ColorType {
		RED, BLUE, GREEN, WHITE;
	}

	private int mBranchNo;
	private ColorType mColorType;
	// 移動タイプ
	private MovementType mMovementType;
	private int mMovementForce;
	// 武器タイプ
	private WeaponType mWeaponType;
	private int mAttackRange;
	private boolean mIsPhysicalAttack;


	public PLMSBranchData(int no) {
		mBranchNo = no;
		mMovementType = getMovementType(no);
		mWeaponType = getWeaponType(no);
		initMovementParams();
		initWeaponParams();
	}

	public String getWeaponImagePath() {
		return "weapon/" + mWeaponType.getInt() + ".png";
	}

	private void initMovementParams() {
		switch (mMovementType) {
			case ARMOR:
				mMovementForce = 1;
				break;
			case INFANTRY:
			case FLYING:
				mMovementForce = 2;
				break;
			case CAVALRY:
				mMovementForce = 3;
				break;
		}
	}

	private void initWeaponParams() {
		switch (mWeaponType) {
			case SWORD:
			case SPEAR:
			case AXE:
			case RED_BREATH:
			case BLUE_BREATH:
			case GREEN_BREATH:
				mAttackRange = 1;
				break;
			case BOW:
			case SHURIKEN:
			case RED_MAGIC:
			case BLUE_MAGIC:
			case GREEN_MAGIC:
			case STAFF:
				mAttackRange = 2;
				break;
		}

		switch (mWeaponType) {
			case SWORD:
			case SPEAR:
			case AXE:
			case BOW:
			case SHURIKEN:
				mIsPhysicalAttack = true;
				break;
			case RED_MAGIC:
			case BLUE_MAGIC:
			case GREEN_MAGIC:
			case STAFF:
			case RED_BREATH:
			case BLUE_BREATH:
			case GREEN_BREATH:
				mIsPhysicalAttack = false;
				break;
		}

		switch (mWeaponType) {
			case SWORD:
			case RED_MAGIC:
			case RED_BREATH:
				mColorType = ColorType.RED;
				break;
			case SPEAR:
			case BLUE_MAGIC:
			case BLUE_BREATH:
				mColorType = ColorType.BLUE;
				break;
			case AXE:
			case GREEN_MAGIC:
			case GREEN_BREATH:
				mColorType = ColorType.GREEN;
				break;
			case BOW:
			case SHURIKEN:
			case STAFF:
				mColorType = ColorType.WHITE;
				break;
		}
	}

	private MovementType getMovementType(int no) {
		MovementType[] types = MovementType.values();
		for (MovementType type : types) {
			if ((type.getInt() & no) != 0) {
				return type;
			}
		}
		return MovementType.ARMOR;
	}

	private WeaponType getWeaponType(int no) {
		WeaponType[] types = WeaponType.values();
		for (WeaponType type : types) {
			if ((type.getInt() & no) != 0) {
				return type;
			}
		}
		return WeaponType.GREEN_BREATH;
	}

	// getter
	public int getNo() {
		return mBranchNo;
	}

	public MovementType getMovementType() {
		return mMovementType;
	}

	public ColorType getColorType() {
		return mColorType;
	}

	public int getMovementForce() {
		return mMovementForce;
	}

	public int getAttackRange() {
		return mAttackRange;
	}

	public boolean isPhysicalAttack() {
		return mIsPhysicalAttack;
	}

	/*
	3すくみの相性を返す
	+1 = 有利
	 0 = 通常
	-1 = 不利
	 */
	public int threeWayCompatibility(PLMSBranchData enemyBranch) {
		switch (enemyBranch.getColorType()) {
			case RED:
				if (mColorType == ColorType.BLUE) {
					return 1;
				} else if (mColorType == ColorType.GREEN) {
					return -1;
				}
				break;
			case BLUE:
				if (mColorType == ColorType.GREEN) {
					return 1;
				} else if (mColorType == ColorType.RED) {
					return -1;
				}
				break;
			case GREEN:
				if (mColorType == ColorType.RED) {
					return 1;
				} else if (mColorType == ColorType.BLUE) {
					return -1;
				}
				break;
			case WHITE:
				break;
		}
		return 0;
	}
}
