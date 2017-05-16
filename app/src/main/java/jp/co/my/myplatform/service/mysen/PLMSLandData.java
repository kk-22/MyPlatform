package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYLogUtil;

public class PLMSLandData {

	public static final int CAN_NOT_ENTER = 999;

	private PLMSLandType mLandType;

	PLMSLandData(int landNumber) {
		switch (landNumber % 10) {
			case 1: mLandType = new PLMSGrassyPlainLand(); break;
			case 2: mLandType = new PLMSForestLand(); break;
			case 3: mLandType = new PLMSWaterLand(); break;
			case 4: mLandType = new PLMSMountainLand(); break;
			case 9: mLandType = new PLMSWallLand(); break;
			default:
				MYLogUtil.showErrorToast("未登録の地形 " +landNumber);
				break;
		}
	}

	public int getMovementCost(PLMSUnitData unitData) {
		return mLandType.getMovementCostEachLand(unitData);
	}

	String getImagePath() {
		return "land/" + mLandType.getImageName();
	}

	private abstract class PLMSLandType {
		abstract String getImageName();
		abstract int getMovementCostEachLand(PLMSUnitData unitData);
	}

	private class PLMSGrassyPlainLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "grassland.gif";
		}
		@Override
		int getMovementCostEachLand(PLMSUnitData unitData) {
			return 1;
		}
	}
	private class PLMSForestLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "forest.png";
		}
		@Override
		int getMovementCostEachLand(PLMSUnitData unitData) {
			switch (unitData.getBranch().getMovementType()) {
				case INFANTRY:
					if (unitData.getCurrentMovementForce() == 1) {
						// 移動力が下がっている場合はコスト1で移動可能
						return 1;
					}
					return 2;
				case ARMOR:
					return 1;
				case CAVALRY:
					return CAN_NOT_ENTER;
				case FLYING:
					return 1;
				default:
					return CAN_NOT_ENTER;
			}
		}
	}
	private class PLMSWaterLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "water.png";
		}
		@Override
		int getMovementCostEachLand(PLMSUnitData unitData) {
			switch (unitData.getBranch().getMovementType()) {
				case FLYING:
					return 1;
				case INFANTRY:
				case ARMOR:
				case CAVALRY:
				default:
					return CAN_NOT_ENTER;
			}
		}
	}
	private class PLMSMountainLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "mountain.jpg";
		}
		@Override
		int getMovementCostEachLand(PLMSUnitData unitData) {
			switch (unitData.getBranch().getMovementType()) {
				case FLYING:
					return 1;
				case INFANTRY:
				case ARMOR:
				case CAVALRY:
				default:
					return CAN_NOT_ENTER;
			}
		}
	}
	private class PLMSWallLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "wall.png";
		}
		@Override
		int getMovementCostEachLand(PLMSUnitData unitData) {
			return CAN_NOT_ENTER;
		}
	}
}
