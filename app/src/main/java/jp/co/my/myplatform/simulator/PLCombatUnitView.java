package jp.co.my.myplatform.simulator;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import jp.co.my.common.util.MYMathUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;

public class PLCombatUnitView extends ConstraintLayout
		implements PLUnitEditContent.PLOnUpdateUnitListener, TextWatcher, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

	private static final int MAX_NUMBER_OF_PARAM = 5; // パラメータの数

	private PLUnitModel mUnitModel;
	private TextView[] mTextViews, mSavedParamTexts, mSumParamTexts;
	private EditText[] mAdditionalParamsEdits;
	private CheckBox mAdvantageCheck, mWeaknessCheck;
	private TextView mAttackCountText;
	private SeekBar mAttackCountSeek;

	private PLOnUpdateUnitViewListener mListener;

	public PLCombatUnitView(Context context) {
		this(context, null);
	}
	public PLCombatUnitView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public PLCombatUnitView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.view_combat_unit, this);

		loadViews();
		initEvents();
	}

	public void loadUnit(PLUnitModel unitModel) {
		mUnitModel = unitModel;
		int[] baseParams = unitModel.getBaseParams();
		int[] turnBuffs = unitModel.getTurnBuffs();
		int[] combatBuffs = unitModel.getCombatBuffs();
		for (int i = 0; i < MAX_NUMBER_OF_PARAM; i++) {
			int sumParam = baseParams[i] + turnBuffs[i] + combatBuffs[i];
			mSavedParamTexts[i].setText(String.valueOf(sumParam));
		}
		mTextViews[0].setText(unitModel.getName());
		mTextViews[1].setText(unitModel.getMemo());
		updateSumParams();
		updateAttackCount();
	}

	private void loadViews() {
		mTextViews = new TextView[2];
		mTextViews[0] = findViewById(R.id.name_text);
		mTextViews[1] = findViewById(R.id.memo_text);
		mSavedParamTexts = new TextView[MAX_NUMBER_OF_PARAM];
		mSavedParamTexts[0] = findViewById(R.id.saved_hp_text);
		mSavedParamTexts[1] = findViewById(R.id.saved_attack_text);
		mSavedParamTexts[2] = findViewById(R.id.saved_speed_text);
		mSavedParamTexts[3] = findViewById(R.id.saved_defense_text);
		mSavedParamTexts[4] = findViewById(R.id.saved_resist_text);
		mSumParamTexts = new TextView[MAX_NUMBER_OF_PARAM];
		mSumParamTexts[0] = findViewById(R.id.sum_hp_text);
		mSumParamTexts[1] = findViewById(R.id.sum_attack_text);
		mSumParamTexts[2] = findViewById(R.id.sum_speed_text);
		mSumParamTexts[3] = findViewById(R.id.sum_defense_text);
		mSumParamTexts[4] = findViewById(R.id.sum_resist_text);
		mAdditionalParamsEdits = new EditText[MAX_NUMBER_OF_PARAM];
		mAdditionalParamsEdits[0] = findViewById(R.id.hp_edit);
		mAdditionalParamsEdits[1] = findViewById(R.id.attack_edit);
		mAdditionalParamsEdits[2] = findViewById(R.id.speed_edit);
		mAdditionalParamsEdits[3] = findViewById(R.id.defense_edit);
		mAdditionalParamsEdits[4] = findViewById(R.id.resist_edit);
		mAdvantageCheck = findViewById(R.id.advantage_check);
		mWeaknessCheck = findViewById(R.id.weakness_check);
		mAttackCountSeek = findViewById(R.id.attack_count_seek);
		mAttackCountText = findViewById(R.id.attack_count_text);
	}

	private void updateSumParams() {
		for (int i = 0; i < MAX_NUMBER_OF_PARAM; i++) {
			int saved = MYMathUtil.integerFromTextView(mSavedParamTexts[i]);
			int additional = MYMathUtil.integerFromTextView(mAdditionalParamsEdits[i]);
			mSumParamTexts[i].setText(String.valueOf(saved + additional));
		}
	}

	private void initEvents() {
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLUnitEditContent content = PLCoreService.getNavigationController().pushView(PLUnitEditContent.class);
				content.editUnit(mUnitModel);
				content.setListener(PLCombatUnitView.this);
			}
		});
		for (EditText editText : mAdditionalParamsEdits) {
			editText.addTextChangedListener(this);
		}
		mAttackCountSeek.setOnSeekBarChangeListener(this);
		mAdvantageCheck.setOnCheckedChangeListener(this);
		mWeaknessCheck.setOnCheckedChangeListener(this);
	}

	private void updateAttackCount() {
		String text = getAttackCount() + "回攻撃";
		mAttackCountText.setText(text);
	}

	public void setListener(PLOnUpdateUnitViewListener listener) {
		mListener = listener;
	}

	public int getGiveDamage(PLCombatUnitView enemyUnitView, double advantageRatio) {
		int totalAttack = getSumAttack();
		if (hasAdvantage()) {
			totalAttack += Math.floor(advantageRatio * totalAttack);
		} else if (enemyUnitView.hasAdvantage()) {
			totalAttack -= Math.floor(advantageRatio * totalAttack);
		}
		if (mWeaknessCheck.isChecked()) {
			totalAttack += Math.floor(1.5 * totalAttack);
		}
		int protection = enemyUnitView.getSumDefenseOrResist(this);
		return totalAttack - protection;
	}

	public boolean hasAdvantage() {
		return mAdvantageCheck.isChecked();
	}

	public int getSumHp() {
		return MYMathUtil.integerFromTextView(mSumParamTexts[0]);
	}

	public int getSumAttack() {
		return MYMathUtil.integerFromTextView(mSumParamTexts[1]);
	}

	public int getSumDefenseOrResist(PLCombatUnitView enemyUnitView) {
		PLUnitModel enemyModel = enemyUnitView.getUnitModel();
		int defense = MYMathUtil.integerFromTextView(mSumParamTexts[3]);
		int resist = MYMathUtil.integerFromTextView(mSumParamTexts[4]);
		if (enemyModel.isUsingLower()) {
			return Math.min(defense, resist);
		}
		return (enemyModel.isPhysicalAttacker()) ? defense : resist;
	}

	public PLUnitModel getUnitModel() {
		return mUnitModel;
	}

	public int getAttackCount() {
		return mAttackCountSeek.getProgress();
	}

	public EditText[] getAdditionalParamsEdits() {
		return mAdditionalParamsEdits;
	}

	@Override
	public void onUpdateUnitModel(PLUnitModel unitModel) {
		loadUnit(mUnitModel);
		mListener.onUpdateUnitView();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	@Override
	public void afterTextChanged(Editable s) {
		updateSumParams();
		mListener.onUpdateUnitView();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateAttackCount();
		mListener.onUpdateUnitView();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mListener.onUpdateUnitView();
	}

	interface PLOnUpdateUnitViewListener {
		void onUpdateUnitView();
	}
}
