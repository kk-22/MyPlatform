package jp.co.my.myplatform.puyo;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;

import jp.co.my.myplatform.R;

public class PLPuyoBlockView extends ConstraintLayout {

	private View mPuyoView;

	public PLPuyoBlockView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.view_puyo_block, this);
		mPuyoView = findViewById(R.id.puyo_view);

		updateBlock();
	}

	private void updateBlock() {
		mPuyoView.setBackgroundColor(Color.GREEN);
	}
}
