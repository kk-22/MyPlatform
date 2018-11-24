package jp.co.my.myplatform.puyo;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.Random;

import jp.co.my.common.util.MYPointUtil;
import jp.co.my.common.util.MYPointUtil.Direction;
import jp.co.my.myplatform.puyo.PLPuyoBlockView.PuyoType;

import static android.support.constraint.ConstraintLayout.LayoutParams.CHAIN_PACKED;
import static android.support.constraint.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;

public class PLPuyoFieldView extends ConstraintLayout {

	private enum GameStatus {
		FINISHED, STARTED, SUSPENDING,
	}

	private PLPuyoFieldListener mListener;
	private GameStatus mGameStatus;

	// ゲーム中用
	private PLPuyoBlockView[][] mBlocks;
	private int mNumberOfRow, mNumberOfColumn;
	private Point mFocusPoint; // 操作中ぷよの座標。非操作時はnull
	private Point mSubPoint; // 操作中ぷよに繋がるぷよの座標
	private Handler mDownHandler; // 時間経過で1段下げる
	private Runnable mDownRunnable; // mDownHandler で実行する処理

	public PLPuyoFieldView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGameStatus = GameStatus.FINISHED;
		mDownHandler = new Handler();
		mDownRunnable = new Runnable() {
			@Override
			public void run() {
				timePassed();
			}
		};

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				loadBlocks();
				startGame();
			}
		});
	}

	void startGame() {
		mGameStatus = GameStatus.STARTED;
		goNextTurn();
	}

	private void goNextTurn() {
		int startX = mNumberOfRow / 2 - 1;
		mFocusPoint = new Point(startX, 1);
		mSubPoint = new Point(startX, 0);
		PLPuyoBlockView focusBlockView = getBlockOfPoint(mFocusPoint);
		PLPuyoBlockView subBlockView = getBlockOfPoint(mSubPoint);

		createRandomPuyo(focusBlockView, subBlockView);
		updateAllBlock();
		startDownHandler();
	}

	void finishTurn() {
		if (canNotOperationFocusPuyo()) {
			return;
		}
		mFocusPoint = null;
		mSubPoint = null;
		cancelDownHandler();

		deleteConnectedPuyo();

		int startX = mNumberOfRow / 2 - 1;
		if (mBlocks[startX][1].hasPuyo()) {
			mGameStatus = GameStatus.FINISHED;
			mListener.onGameOver();
			return;
		}
		goNextTurn();
	}

	private void createRandomPuyo(PLPuyoBlockView blockView1, PLPuyoBlockView blockView2) {
		Random random = new Random();
		// NONE 分を除く
		int numberOfType = PuyoType.values().length - 1;
		blockView1.setPuyoTypeByNumber(random.nextInt(numberOfType) + 1);
		blockView2.setPuyoTypeByNumber(random.nextInt(numberOfType) + 1);
	}

	// ぷよ操作
	void moveFocusPuyoToLeftOrRight(boolean toLeft) {
		if (canNotOperationFocusPuyo()) {
			return;
		}
		Point nextFocusPoint, nextSubPoint;
		if (toLeft) {
			nextFocusPoint = new Point(mFocusPoint.x - 1, mFocusPoint.y);
			nextSubPoint = new Point(mSubPoint.x - 1, mSubPoint.y);
		} else {
			nextFocusPoint = new Point(mFocusPoint.x + 1, mFocusPoint.y);
			nextSubPoint = new Point(mSubPoint.x + 1, mSubPoint.y);
		}

		if (isOutOfRange(nextFocusPoint) || isOutOfRange(nextSubPoint)) {
			// 移動先が画面外
			return;
		}
		PLPuyoBlockView nextFocusBlock = getBlockOfPoint(nextFocusPoint);
		PLPuyoBlockView nextSubBlock = getBlockOfPoint(nextSubPoint);
		if ((nextFocusBlock.hasPuyo() && !mSubPoint.equals(nextFocusPoint))
				|| (nextSubBlock.hasPuyo() && !mFocusPoint.equals(nextSubPoint))) {
			// 移動先にぷよがある
			return;
		}
		moveFocusPuyo(nextFocusBlock, nextSubBlock);
	}

	private void moveFocusPuyo(PLPuyoBlockView nextFocusBlock, PLPuyoBlockView nextSubBlock) {
		PLPuyoBlockView prevFocusBlock = getBlockOfPoint(mFocusPoint);
		PLPuyoBlockView prevSubBlock = getBlockOfPoint(mSubPoint);
		PuyoType focusType = prevFocusBlock.getPuyoType();
		PuyoType subType = prevSubBlock.getPuyoType();
		prevFocusBlock.clearPuyo();
		prevSubBlock.clearPuyo();
		nextFocusBlock.setPuyoType(focusType);
		nextSubBlock.setPuyoType(subType);
		updateBlocks(prevFocusBlock, nextFocusBlock, prevSubBlock, nextSubBlock);
		mFocusPoint = nextFocusBlock.getPoint();
		mSubPoint = nextSubBlock.getPoint();
	}

	private void moveSinglePuyo(PLPuyoBlockView from, PLPuyoBlockView to) {
		to.setPuyoType(from.getPuyoType());
		from.clearPuyo();
	}

	void rotateFocusPuyo(boolean toLeft) {
		if (canNotOperationFocusPuyo()) {
			return;
		}
		Point nextSubPoint;
		if (mFocusPoint.x == mSubPoint.x) {
			// 縦並び
			int diff = (toLeft) ? -1 : 1;
			if (mFocusPoint.y < mSubPoint.y) diff *= -1; // サブが下なら回転方向反転
			nextSubPoint = new Point(mSubPoint.x + diff, mFocusPoint.y);
		} else {
			// 横並び
			int diff = (toLeft) ? 1 : -1;
			if (mFocusPoint.x < mSubPoint.x) diff *= -1; // サブが右なら回転方向反転
			nextSubPoint = new Point(mFocusPoint.x, mSubPoint.y + diff);
		}
		PLPuyoBlockView nextFocusBlock;
		PLPuyoBlockView nextSubBlock = getBlockOfPointIfInRange(nextSubPoint);
		if (nextSubBlock != null && !nextSubBlock.hasPuyo()) {
			nextFocusBlock = getBlockOfPoint(mFocusPoint);
		} else {
			// 回転先が移動不可ならフォーカスを押し出す
			int diffX = mFocusPoint.x - nextSubPoint.x;
			int diffY = mFocusPoint.y - nextSubPoint.y;
			Point nextFocusPoint = MYPointUtil.createWithDiff(mFocusPoint, diffX, diffY);
			nextFocusBlock = getBlockOfPointIfInRange(nextFocusPoint);
			if (nextFocusBlock == null) {
				throw new RuntimeException();
			} else if (nextFocusBlock.hasPuyo()) {
				// フォーカス・サブが縦関係で左右が囲まれているケースなら位置入れ替え
				nextFocusBlock = getBlockOfPoint(mSubPoint);
				nextSubBlock = getBlockOfPoint(mFocusPoint);
			} else {
				// 押し出し移動のためサブはフォーカスがあった位置へ移動
				nextSubBlock = getBlockOfPoint(mFocusPoint);
			}
		}
		moveFocusPuyo(nextFocusBlock, nextSubBlock);
	}

	private void moveDownPuyo() {
		boolean needUpdate = false;
		for (int i = 0; i < mNumberOfRow; i++) {
			ArrayList<PLPuyoBlockView> spaceArray = new ArrayList<>(); // 現列内の空白ブロック
			for (int j = mNumberOfColumn - 1; 0 <= j; j--) {
				PLPuyoBlockView block = mBlocks[i][j];
				boolean hasPuyo = block.hasPuyo();
				if (hasPuyo && spaceArray.size() > 0) {
					PLPuyoBlockView space = spaceArray.get(0);
					spaceArray.remove(0);
					moveSinglePuyo(block, space);
					spaceArray.add(block); // 移動により空白ブロックに変わった
					needUpdate = true;
				} else if (!hasPuyo) {
					spaceArray.add(block);
				}
			}
		}
		if (needUpdate) {
			updateAllBlock();
		}
	}

	void deleteConnectedPuyo() {
		moveDownPuyo();

		ArrayList<PLPuyoBlockView> deleteArray = new ArrayList<>(); // 削除対象
		ArrayList<PLPuyoBlockView> checkedArray = new ArrayList<>(); // チェック済み領域
		for (int i = 0; i < mNumberOfRow; i++) {
			for (int j = mNumberOfColumn - 1; 0 <= j; j--) {
				Point rootPoint = new Point(i, j);
				PLPuyoBlockView rootBlock = getBlockOfPoint(rootPoint);
				if (checkedArray.contains(rootBlock)) {
					checkedArray.remove(rootBlock);
					continue;
				}
				if (!rootBlock.hasPuyo()) {
					// これより上にぷよは無い
					break;
				}
				ArrayList<PLPuyoBlockView> resultArray = new ArrayList<>();
				// 左下から順にチェックするため左と下は確認不要
				searchConnectedPuyo(rootBlock, resultArray, Direction.TOP, Direction.RIGHT);
				int count = resultArray.size();
				if (count == 1) {
					continue;
				}
				if (count >= 4) {
					deleteArray.addAll(resultArray);
				}
				resultArray.remove(rootBlock);
				checkedArray.addAll(resultArray);
			}
		}
		if (deleteArray.size() > 0) {
			for (PLPuyoBlockView blockView : deleteArray) {
				blockView.clearPuyo();
			}
			deleteConnectedPuyo();
		}
	}

	void searchConnectedPuyo(PLPuyoBlockView currentBlock,
							 ArrayList<PLPuyoBlockView> resultArray,
							 Direction... directions) {
		resultArray.add(currentBlock);
		Point currentPoint = currentBlock.getPoint();
		PuyoType currentType = currentBlock.getPuyoType();
		for (Direction direction : directions) {
			PLPuyoBlockView block = getBlockOfPointIfInRange(
					MYPointUtil.createWithDirection(currentPoint, direction));
			if (block != null && block.getPuyoType() == currentType && !resultArray.contains(block)) {
				// 右上から左に伸びるパターンがあるため全方向必要
				searchConnectedPuyo(block, resultArray, Direction.TOP, Direction.RIGHT, Direction.BOTTOM, Direction.LEFT);
			}
		}
	}

	// 時間操作
	void startDownHandler() {
		mDownHandler.postDelayed(mDownRunnable, 1250);
	}

	void cancelDownHandler() {
		mDownHandler.removeCallbacks(mDownRunnable);
	}

	private void timePassed() {
		Point focusBottomPoint = MYPointUtil.createWithDirection(mFocusPoint, Direction.BOTTOM);
		PLPuyoBlockView focusBottomBlock = getBlockOfPointIfInRange(focusBottomPoint);
		if (focusBottomBlock != null && (focusBottomPoint.equals(mSubPoint) || !focusBottomBlock.hasPuyo())) {
			Point subBottomPoint = MYPointUtil.createWithDirection(mSubPoint, Direction.BOTTOM);
			PLPuyoBlockView subBottomBlock = getBlockOfPointIfInRange(subBottomPoint);
			if (subBottomBlock != null && (subBottomPoint.equals(mFocusPoint) || !subBottomBlock.hasPuyo())) {
				// 1段だけ落下
				moveFocusPuyo(focusBottomBlock, subBottomBlock);
				startDownHandler();
				return;
			}
		}
		finishTurn();
	}

	// View操作
	private void updateAllBlock() {
		for (int i = 0; i < mNumberOfRow; i++) {
			for (int j = 0; j < mNumberOfColumn; j++) {
				mBlocks[i][j].updateBlock();
			}
		}
	}

	private void updateBlocks(PLPuyoBlockView... blocks) {
		for (PLPuyoBlockView block : blocks) {
			block.updateBlock();
		}
	}

	private void loadBlocks() {
		mNumberOfRow = 6;
		mNumberOfColumn = 12;
		ConstraintLayout.LayoutParams fieldParams = (ConstraintLayout.LayoutParams) getLayoutParams();
		fieldParams.dimensionRatio = "W," + mNumberOfRow + ":" + mNumberOfColumn;

		mBlocks = new PLPuyoBlockView[mNumberOfRow][mNumberOfColumn];
		for (int i = 0; i < mNumberOfRow; i++) {
			for (int j = 0; j < mNumberOfColumn; j++) {
				PLPuyoBlockView blockView = new PLPuyoBlockView(getContext());
				blockView.setId((i + 1) * 100 + j);
				blockView.setPoint(i, j);
				mBlocks[i][j] = blockView;
			}
		}
		for (int i = 0; i < mNumberOfRow; i++) {
			for (int j = 0; j < mNumberOfColumn; j++) {
				ConstraintLayout.LayoutParams params = new Constraints.LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT);
				if (i == 0) {
					params.leftToLeft = PARENT_ID;
				} else {
					params.leftToRight = mBlocks[i - 1][j].getId();
				}
				if (i < mNumberOfRow - 1) {
					params.rightToLeft = mBlocks[i + 1][j].getId();
				} else {
					params.rightToRight = PARENT_ID;
				}
				if (j == 0) {
					params.topToTop = PARENT_ID;
				} else {
					params.topToBottom = mBlocks[i][j - 1].getId();
				}
				if (j < mNumberOfColumn - 1) {
					params.bottomToTop = mBlocks[i][j + 1].getId();
				} else {
					params.bottomToBottom = PARENT_ID;
				}
				if (i == 0 && j == 0) {
					params.verticalChainStyle = CHAIN_PACKED;
					params.horizontalChainStyle = CHAIN_PACKED;
				}
				addView(mBlocks[i][j], params);
			}
		}
	}

	private boolean canNotOperationFocusPuyo() {
		return (mGameStatus != GameStatus.STARTED || mFocusPoint == null);
	}

	private boolean isOutOfRange(Point point) {
		return  (point.x < 0 || mNumberOfRow <= point.x || point.y < 0 || mNumberOfColumn <= point.y);
	}

	private PLPuyoBlockView getBlockOfPoint(Point point) {
		return mBlocks[point.x][point.y];
	}

	private PLPuyoBlockView getBlockOfPointIfInRange(Point point) {
		if (isOutOfRange(point)) {
			return null;
		}
		return getBlockOfPoint(point);
	}

	void setListener(PLPuyoFieldListener listener) {
		mListener = listener;
	}

	interface PLPuyoFieldListener {
		void onGameOver();
	}
}
