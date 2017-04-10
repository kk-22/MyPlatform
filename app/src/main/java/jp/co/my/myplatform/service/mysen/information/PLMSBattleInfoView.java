package jp.co.my.myplatform.service.mysen.information;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportUnit;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

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

	public void updateInfoForSupport(PLMSSupportForecast supportForecast, PLMSSupportUnit supportUnit) {
		updateInfo(supportUnit);
		// TODO: move to updateInfo method.
		mResultHPTextView.setTextColor(Color.parseColor("#ffffff"));
		mDamageTextView.setText("");
	}

	public void updateInfoForBattle(PLMSBattleForecast battleForecast, PLMSBattleUnit battleUnit) {
		updateInfo(battleUnit);
		if (battleUnit.isAlive()) {
			mResultHPTextView.setTextColor(Color.parseColor("#ffffff"));
		} else {
			mResultHPTextView.setTextColor(Color.parseColor("#ff0000"));
		}

		StringBuilder damageText = new StringBuilder();
		int attackCount = battleForecast.getAttackerArray().countObject(battleUnit);
		if (attackCount == 0) {
			damageText.append("-");
		} else {
			damageText.append(battleForecast.givingDamageOfBattleUnit(battleUnit));
			if (attackCount > 1) {
				damageText.append("×");
				damageText.append(attackCount);
			}
		}
		mDamageTextView.setText(damageText.toString());
	}

	private void updateInfo(PLMSUnitInterface unit) {
		mNameTextView.setText(unit.getUnitView().getUnitData().getUnitModel().getName());
		setIntToText(unit.getUnitView().getUnitData().getCurrentHP(), mCurrentHPTextView);
		setIntToText(unit.getRemainingHP(), mResultHPTextView);

		setIntToText(0, mSecretCountTextView);
	}

	private void setIntToText(int number, TextView textView) {
		textView.setText(Integer.toString(number));
	}
}
