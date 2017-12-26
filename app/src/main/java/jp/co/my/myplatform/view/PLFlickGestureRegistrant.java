package jp.co.my.myplatform.view;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class PLFlickGestureRegistrant {

	private PLFlickGestureListener mListener;
	private GestureDetector mGestureDetector;

	public PLFlickGestureRegistrant(Context context, View view, PLFlickGestureListener listener) {
		super();
		mListener = listener;

		mGestureDetector = new GestureDetector(context, mOnGestureListener);
		if (view != null) {
			view.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return transmitTapEvent(event);
				}
			});
		}
	}

	public boolean transmitTapEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	public static abstract class PLFlickGestureListener {
		public abstract void flickToRight();
		public abstract void flickToLeft();
		public abstract boolean cancelFlickEvent(int direction);
	}

	private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
		private static final int DIRECTION_RIGHT = -1;
		private static final int DIRECTION_LEFT = 1;
		// 右スワイプ
		private static final int MIN_DISTANCE_RIGHT = 250;
		private static final int MIN_SPEED_RIGHT = 3000;
		// 左スワイプ
		private static final int MIN_DISTANCE_LEFT = 150;
		private static final int MIN_SPEED_LEFT = 3000;
		// Y軸の許容移動距離
		private static final int MAX_Y_DISTANCE = 150;

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
			if (Math.abs(event1.getY() - event2.getY()) > MAX_Y_DISTANCE) {
				// 縦のスクロール
				return false;
			}

			int direction, minDistance, minSpeed;
			if ((event2.getX() - event1.getX()) > 0) {
				direction = DIRECTION_RIGHT;
				minDistance = MIN_DISTANCE_RIGHT;
				minSpeed = MIN_SPEED_RIGHT;
			} else {
				direction = DIRECTION_LEFT;
				minDistance = MIN_DISTANCE_LEFT;
				minSpeed = MIN_SPEED_LEFT;
			}

			if (mListener.cancelFlickEvent(direction)) {
				return false;
			}
			float distance = Math.abs((event1.getX() - event2.getX()));
			float speed = Math.abs(velocityX);
			//MYLogUtil.outputLog("横の移動距離：" + distance + " 横の移動スピード：" + speed);
			if (distance < minDistance || speed < minSpeed) {
				// 移動量・速度が足りない
				return false;
			}

			if (direction == DIRECTION_RIGHT) {
				mListener.flickToRight();
			} else {
				mListener.flickToLeft();
			}
			return false;
		}
	};
}
