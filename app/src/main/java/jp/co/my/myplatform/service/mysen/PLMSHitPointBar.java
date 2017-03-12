package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;


public class PLMSHitPointBar extends FrameLayout {

	private TextView mDamageText;
	private TextView mNumberText;
	private View mBarView;
	private FrameLayout mHPFrame;

	private PLMSUnitView mUnitView;

	public PLMSHitPointBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.mysen_hp_bar, this);
		mDamageText = (TextView) findViewById(R.id.damage_text);
		mNumberText = (TextView) findViewById(R.id.hp_text);
		mHPFrame = (FrameLayout) findViewById(R.id.hp_frame);
		mBarView = findViewById(R.id.hp_view);
	}

	public PLMSHitPointBar(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSHitPointBar(Context context) {
		this(context, null);
	}

	public void initWithUnitView(PLMSUnitView unitView) {
		mUnitView = unitView;

		PLMSArmyStrategy armyStrategy = unitView.getUnitData().getArmyStrategy();
		int color = armyStrategy.getHitPointColor();
		mNumberText.setTextColor(color);
		mBarView.setBackgroundColor(color);

		updateHitPoint(mUnitView.getUnitData().getCurrentHP(), 0);
	}

	public void updateHitPoint(int nextHP, int diffHP) {
		if (needDamageText(diffHP)) {
			// 初期設定時以外はダメージ表示
			mDamageText.setText(String.valueOf(diffHP * -1));
			mDamageText.setAlpha(1f);
		}
		mNumberText.setText(String.valueOf(nextHP));
	}

	private boolean needDamageText(int diffHP) {
		return (diffHP < 0);
	}

	public AnimatorSet getDamageAnimatorArray(boolean willRemoveUnit, int diffHP) {
		if (!needDamageText(diffHP)) {
			return null;
		}
		MYArrayList<Animator> animatorArray = new MYArrayList<>();
		float currentY = mDamageText.getY();
		float topY = currentY - getHeight() / 4;

		// 上へ
		ObjectAnimator upAnimation = ObjectAnimator.ofFloat(mDamageText, "y", currentY, topY);
		upAnimation.setDuration(100);
		animatorArray.add(upAnimation);

		// 元の位置へ下げる
		ObjectAnimator downAnimation = ObjectAnimator.ofFloat(mDamageText, "y", topY, currentY);
		downAnimation.setDuration(100);
		animatorArray.add(downAnimation);

		if (willRemoveUnit) {
			// 敗走
			ObjectAnimator removeAnimator = ObjectAnimator.ofFloat(mUnitView, "alpha", 1f, 0f);
			removeAnimator.setDuration(500);
			animatorArray.add(removeAnimator);
		} else {
			// ダメージ表示が消える
			ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mDamageText, "alpha", 1f, 0f);
			alphaAnimator.setDuration(300);
			animatorArray.add(alphaAnimator);
		}

		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playSequentially(animatorArray);
		return animatorSet;
	}
}
