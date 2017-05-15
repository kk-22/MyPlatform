package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYLogUtil;

public class PLMSLandData {

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

	public int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
		return mLandType.getRemainingMovementForce(unitData, currentMoveForce);
	}

	public boolean canEnterLand(PLMSUnitData unitData) {
		int movementForce = unitData.getBranch().getMovementForce();
		return (getRemainingMovementForce(unitData, movementForce) >= 0);
	}

	String getImagePath() {
		return "land/" + mLandType.getImageName();
	}

	private abstract class PLMSLandType {
		abstract String getImageName();
		abstract int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce);
	}

	private class PLMSGrassyPlainLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "grassland.gif";
		}
		@Override
		int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
			return currentMoveForce - 1;
		}
	}
	private class PLMSForestLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "forest.png";
		}
		@Override
		int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
			return currentMoveForce - 1;
		}
	}
	private class PLMSWaterLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "water.png";
		}
		@Override
		int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
			return currentMoveForce - 1;
		}
	}
	private class PLMSMountainLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "mountain.jpg";
		}
		@Override
		int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
			return currentMoveForce - 1;
		}
	}
	private class PLMSWallLand extends PLMSLandType {
		@Override
		String getImageName() {
			return "wall.png";
		}
		@Override
		int getRemainingMovementForce(PLMSUnitData unitData, int currentMoveForce) {
			return Integer.MIN_VALUE;
		}
	}
}
