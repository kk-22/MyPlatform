package jp.co.my.myplatform.puyo;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.co.my.common.util.MYPointUtil;
import jp.co.my.common.util.MYPointUtil.Direction;
import jp.co.my.myplatform.puyo.PLPuyoBlockView.PuyoType;

import static android.support.constraint.ConstraintLayout.LayoutParams.CHAIN_PACKED;
import static android.support.constraint.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PLPuyoFieldView extends ConstraintLayout {

	private enum GameStatus {
		FINISHED, STARTED, SUSPENDING,
	}

	private PLPuyoFieldListener mListener;
	private GameStatus mGameStatus;
	private float mSpeedRatio;
	private PLPuyoBlockView[] mNextBlocks; // 次のぷよを表示する領域

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
		mSpeedRatio = 1.0f;
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
		createRandomPuyo(mNextBlocks);
		goNextTurn();
	}

	private void goNextTurn() {
		int startX = mNumberOfRow / 2 - 1;
		Point tempFocusPoint = new Point(startX, 1);
		PLPuyoBlockView focusBlockView = getBlockOfPoint(tempFocusPoint);
		if (focusBlockView.hasPuyo()) {
			mGameStatus = GameStatus.FINISHED;
			mListener.onGameOver();
			return;
		}

		mFocusPoint = tempFocusPoint;
		mSubPoint = new Point(startX, 0);
		PLPuyoBlockView subBlockView = getBlockOfPoint(mSubPoint);

		int numberOfNext = mNextBlocks.length;
		moveSinglePuyo(mNextBlocks[0], subBlockView);
		moveSinglePuyo(mNextBlocks[1], focusBlockView);
		for (int i = 0; i < numberOfNext - 2; i++) {
			moveSinglePuyo(mNextBlocks[i + 2], mNextBlocks[i]);
		}
		createRandomPuyo(mNextBlocks[numberOfNext - 1], mNextBlocks[numberOfNext - 2]);
		updateBlocks(mNextBlocks);

		updateBlocks(focusBlockView, subBlockView);
		startDownHandler();
	}

	void finishTurn() {
		if (canNotOperationFocusPuyo()) {
			return;
		}
		mFocusPoint = null;
		mSubPoint = null;
		cancelDownHandler();

		moveDownPuyo();
		deleteConnectedPuyoWithDelay();
	}

	private void createRandomPuyo(PLPuyoBlockView... blockViews) {
		Random random = new Random();
		// NONE 分を除く
		int numberOfType = PuyoType.values().length - 1;
		for (PLPuyoBlockView blockView : blockViews) {
			blockView.setPuyoTypeByNumber(random.nextInt(numberOfType) + 1);
		}
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
		ArrayList<PLPuyoBlockView> updateArray = new ArrayList<>();
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
					updateArray.add(space);
					updateArray.add(block);
				} else if (!hasPuyo) {
					spaceArray.add(block);
				}
			}
		}
		updateBlocks(updateArray);
	}

	// 連鎖テスト用
	void loadDebugPuyo() {
		int bottomColumn = mNumberOfColumn - 1;
		for (int i = 0; i < mNumberOfRow - 1; i++) {
			PuyoType type1 = (i % 2 == 0) ? PuyoType.RED : PuyoType.GREEN;
			PuyoType type2 = (i % 2 == 0) ? PuyoType.GREEN : PuyoType.RED;
			mBlocks[i][bottomColumn - 3].setPuyoType(type2);
			mBlocks[i][bottomColumn - 2].setPuyoType(type1);
			mBlocks[i][bottomColumn - 1].setPuyoType(type1);
			mBlocks[i][bottomColumn].setPuyoType(type1);
		}
		updateAllBlock();
	}

	void deleteConnectedPuyoWithDelay() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				deleteConnectedPuyo();
			}
		}, (long) (250 * mSpeedRatio));
	}

	void deleteConnectedPuyo() {
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
		if (deleteArray.size() == 0) {
			goNextTurn();
		} else {
			for (PLPuyoBlockView blockView : deleteArray) {
				blockView.clearPuyo();
			}
			updateBlocks(deleteArray);
			moveDownPuyo();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					deleteConnectedPuyoWithDelay();
				}
			}, (long) (250 * mSpeedRatio));
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
		mDownHandler.postDelayed(mDownRunnable, (long) (1250 * mSpeedRatio));
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

	private void updateBlocks(List<PLPuyoBlockView> blockList) {
		for (PLPuyoBlockView block : blockList) {
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

	void loadNextBlock(LinearLayout... linearLayouts) {
		int numberOfNext = linearLayouts.length;
		mNextBlocks = new PLPuyoBlockView[numberOfNext * 2];
		for (int i = 0; i < numberOfNext; i++) {
			LinearLayout linear = linearLayouts[i];
			for (int j = 0; j < 2; j++) {
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 0);
				params.weight = 1;
				PLPuyoBlockView blockView = new PLPuyoBlockView(getContext());
				linear.addView(blockView, params);
				mNextBlocks[i * 2 + j] = blockView;
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
