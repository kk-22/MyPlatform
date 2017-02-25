package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import java.util.ArrayList;

import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_X;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MAX_Y;
import static jp.co.my.myplatform.service.mysen.PLMSFieldView.MIN_XY;

public class PLMYAreaManager {

	private PLMSFieldView mField;
	private ArrayList<PLMSUnitView> mUnitArray;

	public PLMYAreaManager(PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mField = field;
		mUnitArray = unitArray;
	}

	public void showMoveArea(PLMSUnitView unitView) {
		ArrayList<PLMSLandView> landArray = adjacentLandArray(unitView.getCurrentPoint(), 1);
		int movementForce = unitView.getUnitData().getBranch().getMovementForce();
		for (PLMSLandView landView : landArray) {
			showAdjacentMoveArea(unitView, landView, movementForce);
		}
	}

	public void hideAllMoveArea() {
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.getMoveAreaCover().hideCoverView();
		}
	}

	private void showAdjacentMoveArea(PLMSUnitView unitView, PLMSLandView landView, int remainingMove) {
		if (landView.getMoveAreaCover().isShowingMoveArea() || landView.getUnitView() != null) {
			// 移動不可
			return;
		}
		int nextRemainingMove = remainingMove - unitView.getUnitData().moveCost(landView.getLandData());
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		landView.getMoveAreaCover().showCoverView();

		ArrayList<PLMSLandView> moveLandArray = adjacentLandArray(landView.getPoint(), 1);
		for (PLMSLandView adjacentLandView : moveLandArray) {
			showAdjacentMoveArea(unitView, adjacentLandView, nextRemainingMove);
		}
	}

	private ArrayList<PLMSLandView> adjacentLandArray(Point point, int range) {
		ArrayList<PLMSLandView> landArray = new ArrayList<>();
		// 上右下左 の順番
		ArrayList<Point> pointArray = new ArrayList<>();
		for (int i = - range; i <= range; i++) {
			int y = range - Math.abs(i);
			pointArray.add(new Point(point.x + i, point.y + y));
			if (y != 0) {
				pointArray.add(new Point(point.x + i, point.y + y * -1));
			}
		}
		for (Point targetPoint : pointArray) {
			int x = targetPoint.x;
			int y = targetPoint.y;
			if (MIN_XY <= x && x < MAX_X && MIN_XY <= y && y < MAX_Y) {
				PLMSLandView landView = mField.getLandViewForPoint(new Point(x, y));
				landArray.add(landView);
			}
		}
		return landArray;
	}
}
