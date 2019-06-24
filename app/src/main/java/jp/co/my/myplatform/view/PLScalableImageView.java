package jp.co.my.myplatform.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/*
 拡大縮小・スクロールを可能にした画像
 参考：http://akihito104.hatenablog.com/entry/2017/07/25/220956
  */
public class PLScalableImageView extends PLLoadImageView {

	private final GestureDetectorCompat mGestureDetector;
	private final ScaleGestureDetector mScaleGestureDetector;
	private final float[] mImageMatrixValues = new float[9];
	private final Matrix mTransformMatrix = new Matrix();
	private float mScale = 1;
	private float mFocusX, mFocusY, mTransX, mTransY;
	private final RectF mViewRect = new RectF();
	private final RectF mDrawableRect = new RectF();
	private final Matrix mMatrixToFit = new Matrix();
	private final float[] mMatToFit = new float[9];

	public PLScalableImageView(Context context) {
		this(context, null);
	}
	public PLScalableImageView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	public PLScalableImageView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());
		mGestureDetector = new GestureDetectorCompat(getContext(), new GestureListener());
		mGestureDetector.setIsLongpressEnabled(false);
		setScaleType(ScaleType.MATRIX);
		setClickable(true);
	}

	// タップイベントはsuperで行うため警告を無効化
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleGestureDetector.onTouchEvent(event);
		final boolean scaling = mScaleGestureDetector.isInProgress();
		if (scaling) {
			mTransformMatrix.postScale(mScale, mScale, mFocusX, mFocusY);
		}
		final boolean scrolled = mGestureDetector.onTouchEvent(event);
		if (scrolled) {
			mTransformMatrix.postTranslate(mTransX, mTransY);
		}
		final boolean invalidated = scaling || scrolled;
		if (invalidated) {
			getParent().requestDisallowInterceptTouchEvent(true);
			invalidate();
		}
		return invalidated || super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!mTransformMatrix.isIdentity()) {
			final Matrix matrix = getImageMatrix();
			matrix.postConcat(mTransformMatrix);
			matrix.getValues(mImageMatrixValues);
			mImageMatrixValues[Matrix.MSCALE_X] = Math.max(mImageMatrixValues[Matrix.MSCALE_X], mMatToFit[Matrix.MSCALE_X]);
			mImageMatrixValues[Matrix.MSCALE_Y] = Math.max(mImageMatrixValues[Matrix.MSCALE_Y], mMatToFit[Matrix.MSCALE_Y]);
			final float maxTransX = getWidth() - getDrawable().getIntrinsicWidth() * mImageMatrixValues[Matrix.MSCALE_X];
			if (Math.abs(maxTransX) < Math.abs(mImageMatrixValues[Matrix.MTRANS_X])) {
				mImageMatrixValues[Matrix.MTRANS_X] = maxTransX;
			} else if (Math.signum(maxTransX) * mImageMatrixValues[Matrix.MTRANS_X] < 0) {
				mImageMatrixValues[Matrix.MTRANS_X] = 0;
			}
			final float maxTransY = getHeight() - getDrawable().getIntrinsicHeight() * mImageMatrixValues[Matrix.MSCALE_Y];
			if (Math.abs(maxTransY) < Math.abs(mImageMatrixValues[Matrix.MTRANS_Y])) {
				mImageMatrixValues[Matrix.MTRANS_Y] = maxTransY;
			} else if (Math.signum(maxTransY) * mImageMatrixValues[Matrix.MTRANS_Y] < 0) {
				mImageMatrixValues[Matrix.MTRANS_Y] = 0;
			}
			matrix.setValues(mImageMatrixValues);
			setImageMatrix(matrix);
			mTransformMatrix.reset();
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewRect.set(0, 0, w, h);
		updateMatrixToFit();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		if (drawable == null) {
			mDrawableRect.setEmpty();
		} else {
			mDrawableRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		updateMatrixToFit();
	}

	@Override
	public void setImageMatrix(Matrix matrix) {
		super.setImageMatrix(matrix);
		matrix.getValues(mImageMatrixValues);
	}

	private void updateMatrixToFit() {
		if (mDrawableRect.isEmpty() || mViewRect.isEmpty()) {
			mMatrixToFit.reset();
			mTransformMatrix.reset();
		} else {
			mMatrixToFit.setRectToRect(mDrawableRect, mViewRect, Matrix.ScaleToFit.CENTER);
			setImageMatrix(mMatrixToFit);
			invalidate();
		}
		mMatrixToFit.getValues(mMatToFit);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (e2.getEventTime() - e1.getEventTime() < 200
					&& shouldGoNextPage(distanceX)) {
				return false;
			}
			mTransX = -distanceX;
			mTransY = -distanceY;
			return true;
		}

		private boolean shouldGoNextPage(final float distX) {
			if (Math.abs(distX) < 0.1) {
				return false;
			}
			if (getHeight() == getDrawable().getIntrinsicHeight() * mImageMatrixValues[Matrix.MSCALE_Y]) {
				return true;
			}
			if (distX > 0) {
				final float maxTransX = getWidth() - getDrawable().getIntrinsicWidth() * mImageMatrixValues[Matrix.MSCALE_X];
				return maxTransX <= 0
						&& Math.abs(maxTransX - mImageMatrixValues[Matrix.MTRANS_X]) < 0.01;
			} else {
				return Math.abs(mImageMatrixValues[Matrix.MTRANS_X]) < 0.01;
			}
		}
	};

	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScale = detector.getScaleFactor();
			mFocusX = detector.getFocusX();
			mFocusY = detector.getFocusY();
			return true;
		}
	};
}
