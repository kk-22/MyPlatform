package jp.co.my.myplatform.service.mysen;

public class PLMSColorData {

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

	/*
	3すくみの相性を返す
	+1 = 有利
	 0 = 通常
	-1 = 不利
	 */
	public int threeWayCompatibility(PLMSColorData enemyColor) {
		switch (enemyColor.getColorType()) {
			case RED:
				if (mColorType == Type.BLUE) {
					return 1;
				} else if (mColorType == Type.GREEN) {
					return -1;
				}
				break;
			case BLUE:
				if (mColorType == Type.GREEN) {
					return 1;
				} else if (mColorType == Type.RED) {
					return -1;
				}
				break;
			case GREEN:
				if (mColorType == Type.RED) {
					return 1;
				} else if (mColorType == Type.BLUE) {
					return -1;
				}
				break;
			case WHITE:
				break;
		}
		return 0;
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
