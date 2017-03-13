package jp.co.my.myplatform.service.mysen;

public class PLMSWeaponData {

	private enum Type {
		SWORD(1), SPEAR(2), AXE(3),
		BOW(11), SHURIKEN(16),
		RED_MAGIC(21), BLUE_MAGIC(22), GREEN_MAGIC(23),
		STAFF(26),
		BREATH(31),;

		final int id;

		Type(final int id) {
			this.id = id;
		}

		public int getInt() {
			return this.id;
		}
	}

	private Type mWeaponType;
	private int mAttackRange;
	private boolean mIsPhysicalAttack;

	public PLMSWeaponData(int no) {
		mWeaponType = getType(no);
		initParams();
	}

	private void initParams() {
		switch (mWeaponType) {
			case SWORD:
			case SPEAR:
			case AXE:
			case BREATH:
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
			case BREATH:
				mIsPhysicalAttack = false;
				break;
		}
	}

	private Type getType(int no) {
		Type[] types = Type.values();
		for (Type type : types) {
			if (type.getInt() == no) {
				return type;
			}
		}
		return Type.BREATH;
	}

	// getter
	public int getAttackRange() {
		return mAttackRange;
	}

	public boolean isPhysicalAttack() {
		return mIsPhysicalAttack;
	}
}
