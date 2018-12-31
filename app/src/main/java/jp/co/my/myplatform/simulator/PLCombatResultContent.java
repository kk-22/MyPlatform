package jp.co.my.myplatform.simulator;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;

import jp.co.my.common.util.MYMathUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;

public class PLCombatResultContent extends PLContentView implements PLCombatUnitView.PLOnUpdateUnitViewListener, SeekBar.OnSeekBarChangeListener, TextWatcher {

	private SeekBar mAdvantageSeek; // 3すくみ効果の倍率
	private TextView mAdvantageText;
	private TextView mMineGiveDamageText, mEnemyGiveDamageText;
	private TextView mMineRemainingHpText, mEnemyRemainingHpText;
	private EditText mMineAddDamageEdit, mEnemyAddDamageEdit;

	private PLUnitModel mMineUnit, mEnemyUnit;
	private PLCombatUnitView mMineView, mEnemyView;

	public PLCombatResultContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_combat_result, this);
		mMineView = findViewById(R.id.mine_unit_view);
		mMineView.setListener(this);
		mEnemyView = findViewById(R.id.enemy_unit_view);
		mEnemyView.setListener(this);

		loadViews();
		initEvents();
	}

	@Override
	public void onUpdateUnitView() {
		updateResult();
	}

	public void setUnits(PLUnitModel mineUnit, PLUnitModel enemyUnit) {
		mMineUnit = mineUnit;
		mMineView.loadUnit(mineUnit);
		mEnemyUnit = enemyUnit;
		mEnemyView.loadUnit(enemyUnit);
		updateAdvantageText();
		updateResult();
	}

	private void loadViews() {
		mAdvantageSeek = findViewById(R.id.advantage_ratio_seek);
		mAdvantageText = findViewById(R.id.advantage_ratio_text);
		mMineGiveDamageText = findViewById(R.id.mine_give_damage_text);
		mEnemyGiveDamageText = findViewById(R.id.enemy_give_damage_text);
		mMineRemainingHpText = findViewById(R.id.mine_hp_text);
		mEnemyRemainingHpText = findViewById(R.id.enemy_hp_text);
		mMineAddDamageEdit = findViewById(R.id.mine_add_damage_edit);
		mEnemyAddDamageEdit = findViewById(R.id.enemy_add_damage_edit);
	}

	private void initEvents() {
		mAdvantageSeek.setOnSeekBarChangeListener(this);
		mMineAddDamageEdit.addTextChangedListener(this);
		mEnemyAddDamageEdit.addTextChangedListener(this);
	}

	public double getAdvantageRatio() {
		BigDecimal decimal = new BigDecimal("0.05");
		decimal = decimal.multiply(BigDecimal.valueOf(mAdvantageSeek.getProgress()));
		decimal = decimal.add(new BigDecimal("0.2"));
		return decimal.doubleValue();
	}

	private void updateResult() {
		calculateDamageOfAttacker(mMineView, mEnemyView, mMineGiveDamageText, mMineAddDamageEdit, mEnemyRemainingHpText);
		calculateDamageOfAttacker(mEnemyView, mMineView, mEnemyGiveDamageText, mEnemyAddDamageEdit, mMineRemainingHpText);
	}

	private void calculateDamageOfAttacker(PLCombatUnitView attackerView, PLCombatUnitView defenderView,
										   TextView giveDamageText, EditText addDamageEdit,
										   TextView remainingHpText) {
		double advantage = getAdvantageRatio();
		int singleDamage = attackerView.getGiveDamage(defenderView, advantage);
		int attackCount = attackerView.getAttackCount();
		int sumDamage = Math.max(0, singleDamage * attackCount);
		String giveText = singleDamage +" * " +attackCount +" = " +sumDamage;
		giveDamageText.setText(giveText);

		int addDamage = MYMathUtil.integerFromTextView(addDamageEdit);
		int totalDamage = sumDamage + addDamage;
		int sumHp = defenderView.getSumHp();
		int remainingHp = sumHp - totalDamage;
		String hpText = sumHp +" - " +totalDamage +" = " +remainingHp;
		remainingHpText.setText(hpText);
		if (remainingHp <= 0) {
			remainingHpText.setTextColor(Color.RED);
		} else {
			remainingHpText.setTextColor(Color.BLACK);
		}
	}

	private void updateAdvantageText() {
		String text = "3すくみ補正" + String.valueOf(getAdvantageRatio());
		mAdvantageText.setText(text);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		updateAdvantageText();
		updateResult();
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	@Override
	public void afterTextChanged(Editable s) {
		updateResult();
	}
}
