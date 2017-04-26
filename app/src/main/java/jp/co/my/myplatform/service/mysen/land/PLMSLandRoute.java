package jp.co.my.myplatform.service.mysen.land;

import android.graphics.Point;

import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.PLMSLandView;

/*
 ユニットの現在位置から目標地点までの PLMSLandView を持つ配列。
 ワープ移動の場合は現在地点を含まず、要素は目標地点の1つだけとなる。
  */
public class PLMSLandRoute extends MYArrayList<PLMSLandView> {

	private int mRemainingMovementPower; // 残りの移動力
	private int mNumberOfTurn; // 移動に必要なターン数
	private int mOneTurnIndex; // 1ターン目で移動可能な PLMSLandView を指す index

	public PLMSLandRoute(ArrayList<PLMSLandView> landArray) {
		super(landArray);
	}

	public PLMSLandRoute(PLMSLandView... objects) {
		super(objects);
	}

	public PLMSLandRoute(PLMSLandView landView) {
		super();
		add(landView);
	}

	// Debug
	public String debugLog() {
		StringBuilder builder = new StringBuilder(" LandRoute");
		for (PLMSLandView landView : this) {
			Point point = landView.getPoint();
			builder.append(" → x-y=");
			builder.append(point.x);
			builder.append("-");
			builder.append(point.y);
		}

		String debugText = builder.toString();
		MYLogUtil.outputLog(debugText);
		return debugText;
	}

	// getter
	public int getRemainingMovementPower() {
		return mRemainingMovementPower;
	}

	public int getNumberOfTurn() {
		return mNumberOfTurn;
	}

	public int getOneTurnIndex() {
		return mOneTurnIndex;
	}

	// setter
	public void setRemainingMovementPower(int remainingMovementPower) {
		mRemainingMovementPower = remainingMovementPower;
	}

	public void setNumberOfTurn(int numberOfTurn) {
		if (mNumberOfTurn == 1 && numberOfTurn == 2) {
			// 1ターン目中に移動可能な位置を保持
			mOneTurnIndex = size() - 1;
		}
		mNumberOfTurn = numberOfTurn;
	}
}
