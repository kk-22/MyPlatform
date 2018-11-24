package jp.co.my.myplatform.puyo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import jp.co.my.common.view.LongClickRepeatAdapter;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;


public class PLPuyoGameContent extends PLContentView {

	private PLPuyoFieldView mFieldView;
	private Button mLeftButton, mRightButton, mRotateLeftButton, mRotateRightButton;

	public PLPuyoGameContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_puyo_game, this);
		mFieldView = findViewById(R.id.field_view);

		mLeftButton = findViewById(R.id.left_button);
		mRightButton = findViewById(R.id.right_button);
		mRotateLeftButton = findViewById(R.id.rotate_left_button);
		mRotateRightButton = findViewById(R.id.rotate_right_button);
		setBarType(PLNavigationOverlay.BarType.TOP);

		initButtonEvent();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mFieldView.cancelDownHandler();
	}

	private void initButtonEvent() {
		LongClickRepeatAdapter.bless(mLeftButton, mRightButton, mRotateLeftButton, mRotateRightButton);
		mLeftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFieldView.moveFocusPuyoToLeftOrRight(true);
			}
		});
		mRightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFieldView.moveFocusPuyoToLeftOrRight(false);
			}
		});
		mRotateLeftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFieldView.rotateFocusPuyo(true);
			}
		});
		mRotateRightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFieldView.rotateFocusPuyo(false);
			}
		});
		findViewById(R.id.down_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mFieldView.moveDownPuyo();
				mFieldView.goNext();
			}
		});
	}
}
