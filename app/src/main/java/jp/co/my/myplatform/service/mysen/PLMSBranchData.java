package jp.co.my.myplatform.service.mysen;

public class PLMSBranchData {

	private enum Type {
		INFANTRY(1),
		ARMOR(11),
		CAVALRY(21),
		FLYING(31);

		final int id;

		Type(final int id) {
			this.id = id;
		}

		public int getInt() {
			return this.id;
		}
	}

	private Type mBranchType;
	private int mMovementForce;

	public PLMSBranchData(int no) {
		mBranchType = getType(no);
		initParams();
	}

	public int getNo() {
		return mBranchType.getInt();
	}

	private void initParams() {
		switch (mBranchType) {
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

	private Type getType(int no) {
		Type[] types = Type.values();
		for (Type type : types) {
			if (type.getInt() == no) {
				return type;
			}
		}
		return Type.ARMOR;
	}

	// getter
	public int getMovementForce() {
		return mMovementForce;
	}
}
