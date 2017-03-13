package jp.co.my.myplatform.service.mysen;

public class PLMSColorData {

	private static final double ADVANTAGE_DAMAGE_RATIO = 0.2;
	private static final double DISADVANTAGE_DAMAGE_RATIO = -0.2;

	private enum Type {
		RED(1), BLUE(2), GREEN(3),
		WHITE(11);

		final int id;

		Type(final int id) {
			this.id = id;
		}

		public int getInt() {
			return this.id;
		}
	}

	private Type mColorType;

	public PLMSColorData(int no) {
		mColorType = getType(no);
	}

	public double damageRatio(PLMSColorData enemyColor) {
		switch (enemyColor.getColorType()) {
			case RED:
				if (mColorType == Type.BLUE) {
					return ADVANTAGE_DAMAGE_RATIO;
				} else if (mColorType == Type.GREEN) {
					return DISADVANTAGE_DAMAGE_RATIO;
				}
				break;
			case BLUE:
				if (mColorType == Type.GREEN) {
					return ADVANTAGE_DAMAGE_RATIO;
				} else if (mColorType == Type.RED) {
					return DISADVANTAGE_DAMAGE_RATIO;
				}
				break;
			case GREEN:
				if (mColorType == Type.RED) {
					return ADVANTAGE_DAMAGE_RATIO;
				} else if (mColorType == Type.BLUE) {
					return DISADVANTAGE_DAMAGE_RATIO;
				}
				break;
			case WHITE:
				break;
		}
		return 0.0;
	}

	private Type getType(int no) {
		Type[] types = Type.values();
		for (Type type : types) {
			if (type.getInt() == no) {
				return type;
			}
		}
		return Type.WHITE;
	}

	// getter
	public Type getColorType() {
		return mColorType;
	}
}
