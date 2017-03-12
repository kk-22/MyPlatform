package jp.co.my.myplatform.service.mysen.information;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleResult;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;

public class PLMSBattleInfoView extends LinearLayout {

	private TextView mNameTextView;
	private TextView mCurrentHPTextView;
	private TextView mResultHPTextView;
	private TextView mDamageTextView;
	private TextView mSecretCountTextView;

	public PLMSBattleInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_battle_info, this);
		mNameTextView = (TextView) findViewById(R.id.name_text);
		mCurrentHPTextView = (TextView) findViewById(R.id.current_hp_text);
		mResultHPTextView = (TextView) findViewById(R.id.result_hp_text);
		mDamageTextView = (TextView) findViewById(R.id.damage_text);
		mSecretCountTextView = (TextView) findViewById(R.id.secret_count_text);
	}

	public PLMSBattleInfoView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSBattleInfoView(Context context) {
		this(context, null);
	}

	public void initWithIsLeft(boolean isLeft) {
		if (isLeft) {
			mSecretCountTextView.setGravity(Gravity.CENTER_VERTICAL);
		} else {
			mSecretCountTextView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
		}
	}

	public void updateInfo(PLMSBattleResult battleResult, PLMSBattleUnit battleUnit) {
		mNameTextView.setText(battleUnit.getUnitView().getUnitData().getUnitModel().getName());
		setIntToText(battleUnit.getUnitView().getUnitData().getCurrentHP(), mCurrentHPTextView);
		setIntToText(battleUnit.getResultHP(), mResultHPTextView);
		setIntToText(0, mSecretCountTextView);

		// TODO: 実ダメージではなく基本ダメージをresultから受け取る。反撃不可なら"-"にする
		StringBuilder damageText = new StringBuilder("10");
		int attackCount = battleResult.getAttackerArray().countObject(battleUnit);
		if (attackCount > 1) {
			damageText.append("×");
			damageText.append(attackCount);
		}
		mDamageTextView.setText(damageText.toString());
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}
}
