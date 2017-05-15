package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYLogUtil;

public class PLMSLandData {

	private enum LandType {
		GRASSLAND(1), FOREST(2), WATER(3), MOUNTAIN(4), WALL(9);

		final int id;
		LandType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}
		public static LandType getType(int no) {
			LandType[] types = LandType.values();
			for (LandType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未登録の地形 " +no);
			return LandType.WALL;
		}
	}

	private LandType mLandType;

	PLMSLandData(int landNumber) {
		mLandType = LandType.getType(landNumber % 10);
	}

	public int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
		return currentMoveForce - 1;
	}

	public boolean canEnterLand(PLMSUnitData unitData) {
		int movementForce = unitData.getBranch().getMovementForce();
		return (getRemainingMovementForce(unitData, movementForce) >= 0);
	}

	String getImagePath() {
		String name;
		switch (mLandType) {
			case GRASSLAND:
				name = "grassland.gif";
				break;
			case FOREST:
				name = "forest.png";
				break;
			case WATER:
				name = "water.png";
				break;
			case MOUNTAIN:
				name = "mountain.jpg";
				break;
			case WALL:
				name = "wall.png";
				break;
			default:
				name = "noCase";
				break;
		}
		return "land/" + name;
	}
}
