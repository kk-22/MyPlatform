package jp.co.my.myplatform.puyo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.R;

class PLPuyoBlockView extends ConstraintLayout {

	enum PuyoType {
		NONE,
		RED,
		GREEN,
		BLUE,
		YELLOW,
	}

	private View mPuyoView;
	private PuyoType mPuyoType;
	private int mCurrentPuyoColor;
	private Point mPoint;

	PLPuyoBlockView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_puyo_block, this);
		mPuyoView = findViewById(R.id.puyo_view);

		mPuyoType = PuyoType.NONE;
		mCurrentPuyoColor = Color.TRANSPARENT;
	}

	void updateBlock() {
		int color;
		switch (mPuyoType) {
			case NONE:
			default:
				color = Color.TRANSPARENT; break;
			case RED:
				color = Color.RED; break;
			case GREEN:
				color = Color.GREEN; break;
			case BLUE:
				color = Color.BLUE; break;
			case YELLOW:
				color = Color.YELLOW; break;
		}
		if (mCurrentPuyoColor != color) {
			mPuyoView.setBackgroundColor(color);
			mCurrentPuyoColor = color;
		}
	}

	boolean hasPuyo() {
		return (mPuyoType != PuyoType.NONE);
	}

	void setPuyoTypeByNumber(int number) {
		mPuyoType = MYOtherUtil.fromOrdinal(PuyoType.class, number);
	}

	void clearPuyo() {
		mPuyoType = PuyoType.NONE;
	}

	void setPuyoType(PuyoType puyoType) {
		mPuyoType = puyoType;
	}

	PuyoType getPuyoType() {
		return mPuyoType;
	}

	void setPoint(int x, int y) {
		mPoint = new Point(x, y);
	}

	Point getPoint() {
		return mPoint;
	}
}
