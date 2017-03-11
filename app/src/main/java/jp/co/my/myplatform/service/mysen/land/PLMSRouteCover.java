package jp.co.my.myplatform.service.mysen.land;

import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import jp.co.my.common.util.MYImageUtil;
import jp.co.my.common.util.MYViewUtil;
import jp.co.my.myplatform.service.mysen.PLMSLandView;


public class PLMSRouteCover extends PLMSAbstractCover {

	// 数値として比較するためにenumは使わない
	private static final int DIRECTION_TOP = 0;
	private static final int DIRECTION_RIGHT = 1;
	private static final int DIRECTION_BOTTOM = 2;
	private static final int DIRECTION_LEFT = 3;

	private ImageView mBeginImageView;
	private ImageView mEndImageView;
	private ArrayList<ImageView> mStraightViewArray;        // ストレート
	private ArrayList<ImageView> mCurveViewArray;            // カーブ

	public PLMSRouteCover() {
		super();
		mBeginImageView = MYImageUtil.getImageViewFromImagePath("cover/arrow_begin.png", getContext());
		mEndImageView = MYImageUtil.getImageViewFromImagePath("cover/arrow_end.png", getContext());

		mStraightViewArray = new ArrayList<>();
		mCurveViewArray = new ArrayList<>();
	}

	@Override
	protected View createCoverView() {
		// getCoverViewをoverrideしているため呼ばれない
		return null;
	}

	@Override
	protected View getCoverView(int index, PLMSLandView currentLandView, ArrayList<PLMSLandView> landViews) {
		View coverView;
		int direction;
		if (index == 0) {
			direction = directionOfLand(currentLandView, landViews.get(index + 1));
			coverView = mBeginImageView;
		} else if (index == landViews.size() - 1) {
			direction = directionOfLand(landViews.get(index - 1), currentLandView);
			coverView = mEndImageView;
		} else {
			int nextDirection = directionOfLand(currentLandView, landViews.get(index + 1));
			int prevDirection = directionOfLand(landViews.get(index - 1), currentLandView);
			// begin分の1を引く
			int routeIndex = index - 1;
			boolean isStraight = (nextDirection == prevDirection);
			coverView = getRouteView(routeIndex, isStraight);
			if (isStraight) {
				direction = nextDirection;
			} else {
				if (nextDirection == directionAfterRotate(prevDirection)) {
					// 右折時
					direction = prevDirection;
				} else {
					// 左折時
					direction = directionAfterRotate(prevDirection);
				}
			}
		}
		coverView.setRotation(direction * 90);
		return coverView;
	}

	@Override
	public void showCoverViews(ArrayList<PLMSLandView> landViews) {
		if (landViews.size() <= 1) {
			// ルート表示なし
			return;
		}
		super.showCoverViews(landViews);
	}

	@Override
	public void hideCoverViews() {
		MYViewUtil.removeFromSuperView(mBeginImageView);
		MYViewUtil.removeFromSuperView(mEndImageView);
		for (View view : mStraightViewArray) {
			MYViewUtil.removeFromSuperView(view);
		}
		for (View view : mCurveViewArray) {
			MYViewUtil.removeFromSuperView(view);
		}
		getParentViewArray().clear();
	}

	private ImageView getRouteView(int index, boolean isStraight) {
		ArrayList<ImageView> imageViewArray = (isStraight) ? mStraightViewArray : mCurveViewArray;
		if (index < imageViewArray.size()) {
			return imageViewArray.get(index);
		}
		String imagePath = (isStraight) ? "cover/arrow_straight.png" : "cover/arrow_curve.png";
		ImageView imageView = MYImageUtil.getImageViewFromImagePath(imagePath, getContext());
		imageViewArray.add(imageView);
		return imageView;
	}

	private int directionOfLand(PLMSLandView fromLandView, PLMSLandView toLandView) {
		Point fromPoint = fromLandView.getPoint();
		Point toPoint = toLandView.getPoint();
		if (fromPoint.x == toPoint.x) {
			// 縦軸が同じ
			return (fromPoint.y < toPoint.y) ? DIRECTION_BOTTOM : DIRECTION_TOP;
		}
		// 横軸が同じ
		return (fromPoint.x < toPoint.x) ? DIRECTION_RIGHT : DIRECTION_LEFT;
	}

	// 右に90度回転後のdirection
	private int directionAfterRotate(int baseDirection) {
		if (baseDirection == DIRECTION_LEFT) {
			return DIRECTION_TOP;
		}
		return baseDirection + 1;
	}
}
