package jp.co.my.myplatform.puyo;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.view.LayoutInflater;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;

import static android.support.constraint.ConstraintLayout.LayoutParams.CHAIN_PACKED;
import static android.support.constraint.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;

public class PLPuyoGameContent extends PLContentView {

	private PLPuyoBlockView[][] mBlocks;

	public PLPuyoGameContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_puyo_game, this);

		layoutField();
	}

	private void layoutField() {
		int numberOfRow = 6;
		int numberOfColumn = 12;
		ConstraintLayout fieldConstraint = findViewById(R.id.field_constraint);
		ConstraintLayout.LayoutParams fieldParams = (ConstraintLayout.LayoutParams) fieldConstraint.getLayoutParams();
		fieldParams.dimensionRatio = "W," + numberOfRow + ":" + numberOfColumn;

		mBlocks = new PLPuyoBlockView[numberOfRow][numberOfColumn];
		for (int i = 0; i < numberOfRow; i++) {
			for (int j = 0; j < numberOfColumn; j++) {
				PLPuyoBlockView blockView = new PLPuyoBlockView(getContext());
				blockView.setId((i + 1) * 100 + j);
				mBlocks[i][j] = blockView;
			}
		}
		for (int i = 0; i < numberOfRow; i++) {
			for (int j = 0; j < numberOfColumn; j++) {
				ConstraintLayout.LayoutParams params = new Constraints.LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT);
				if (i == 0) {
					params.leftToLeft = PARENT_ID;
				} else {
					params.leftToRight = mBlocks[i - 1][j].getId();
				}
				if (i < numberOfRow - 1) {
					params.rightToLeft = mBlocks[i + 1][j].getId();
				} else {
					params.rightToRight = PARENT_ID;
				}
				if (j == 0) {
					params.topToTop = PARENT_ID;
				} else {
					params.topToBottom = mBlocks[i][j - 1].getId();
				}
				if (j < numberOfColumn - 1) {
					params.bottomToTop = mBlocks[i][j + 1].getId();
				} else {
					params.bottomToBottom = PARENT_ID;
				}
				if (i == 0 && j == 0) {
					params.verticalChainStyle = CHAIN_PACKED;
					params.horizontalChainStyle = CHAIN_PACKED;
				}
				fieldConstraint.addView(mBlocks[i][j], params);
			}
		}
	}
}
