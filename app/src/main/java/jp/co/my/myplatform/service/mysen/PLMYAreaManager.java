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
		ArrayList<PLMSLandView> landArray = adjacentLandArray(unitView.getCurrentPoint());
		int movementForce = unitView.getUnitModel().getBranch().getMovementForce();
		for (PLMSLandView landView : landArray) {
			showAdjacentMoveArea(unitView, landView, movementForce);
		}
	}

	public void hideAllMoveArea() {
		for (PLMSLandView landView : mField.getLandArray()) {
			landView.hideMoveArea();
		}
	}

	private void showAdjacentMoveArea(PLMSUnitView unitView, PLMSLandView landView, int remainingMove) {
		if (landView.isShowingMoveArea() || landView.getUnitView() != null) {
			// 移動不可
			return;
		}
		int nextRemainingMove = remainingMove - unitView.getUnitModel().moveCost(landView.getLandData());
		if (nextRemainingMove < 0) {
			// 移動不可
			return;
		}
		landView.showMoveArea();

		ArrayList<PLMSLandView> landArray = adjacentLandArray(landView.getPoint());
		for (PLMSLandView adjacentLandView : landArray) {
			showAdjacentMoveArea(unitView, adjacentLandView, nextRemainingMove);
		}
	}

	private ArrayList<PLMSLandView> adjacentLandArray(Point point) {
		ArrayList<PLMSLandView> landArray = new ArrayList<>();
		// 上右下左 の順番
		int[] adjacentXs = {point.x - 1, point.x, point.x + 1, point.x};
		int[] adjacentYs = {point.y, point.y + 1, point.y, point.y -1};
		for (int i = 0; i < 4; i++) {
			int x = adjacentXs[i];
			int y = adjacentYs[i];
			if (MIN_XY <= x && x < MAX_X && MIN_XY <= y && y < MAX_Y) {
				PLMSLandView landView = mField.getLandViewForPoint(new Point(x, y));
				landArray.add(landView);
			}
		}
		return landArray;
	}
}
