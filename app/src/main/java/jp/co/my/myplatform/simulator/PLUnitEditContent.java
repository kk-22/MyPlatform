package jp.co.my.myplatform.simulator;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;
import jp.co.my.myplatform.popover.PLConfirmationPopover;

public class PLUnitEditContent extends PLContentView {

	EditText[] mBaseEdits, mTurnBuffEdits, mCombatBuffEdits, mTextEdits;
	CheckBox mMineCheck, mPhysicalAttackCheck, mUsingLowerCheck;
	PLUnitModel mUnitModel;
	Button mContinueButton, mDeleteButton;
	PLOnUpdateUnitListener mListener;

	public PLUnitEditContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_unit_edit, this);

		loadViews();
		initViewEvent();
	}

	@Override
	public int getStatusBarVisibility() {
		return View.INVISIBLE;
	}

	public void editUnit(PLUnitModel unitModel) {
		mUnitModel = unitModel;
		mContinueButton.setEnabled(false);
		mDeleteButton.setEnabled(true);

		int[] baseParams = unitModel.getBaseParams();
		int[] turnBuffs = unitModel.getTurnBuffs();
		int[] combatBuffs = unitModel.getCombatBuffs();
		for (int i = 0; i < 5; i++) {
			mBaseEdits[i].setText(MYStringUtil.stringFromIntegerIfIsNoZero(baseParams[i]));
			mTurnBuffEdits[i].setText(MYStringUtil.stringFromIntegerIfIsNoZero(turnBuffs[i]));
			mCombatBuffEdits[i].setText(MYStringUtil.stringFromIntegerIfIsNoZero(combatBuffs[i]));
		}
		mTextEdits[0].setText(unitModel.getName());
		mTextEdits[1].setText(unitModel.getMemo());
		mMineCheck.setChecked(unitModel.isMine());
	}

	private void loadViews() {
		mBaseEdits = new EditText[5];
		mBaseEdits[0] = findViewById(R.id.hp_edit);
		mBaseEdits[1] = findViewById(R.id.attack_edit);
		mBaseEdits[2] = findViewById(R.id.speed_edit);
		mBaseEdits[3] = findViewById(R.id.defense_edit);
		mBaseEdits[4] = findViewById(R.id.resist_edit);
		mTurnBuffEdits = new EditText[5];
		mTurnBuffEdits[0] = findViewById(R.id.turn_hp_edit);
		mTurnBuffEdits[1] = findViewById(R.id.turn_attack_edit);
		mTurnBuffEdits[2] = findViewById(R.id.turn_speed_edit);
		mTurnBuffEdits[3] = findViewById(R.id.turn_defense_edit);
		mTurnBuffEdits[4] = findViewById(R.id.turn_resist_edit);
		mCombatBuffEdits = new EditText[5];
		mCombatBuffEdits[0] = findViewById(R.id.combat_hp_edit);
		mCombatBuffEdits[1] = findViewById(R.id.combat_attack_edit);
		mCombatBuffEdits[2] = findViewById(R.id.combat_speed_edit);
		mCombatBuffEdits[3] = findViewById(R.id.combat_defense_edit);
		mCombatBuffEdits[4] = findViewById(R.id.combat_resist_edit);
		mTextEdits = new EditText[2];
		mTextEdits[0] = findViewById(R.id.name_edit);
		mTextEdits[1] = findViewById(R.id.memo_edit);
		initNextFocus(mBaseEdits, mTurnBuffEdits, mCombatBuffEdits, mTextEdits);

		mMineCheck = findViewById(R.id.mine_check);
		mPhysicalAttackCheck = findViewById(R.id.physical_attack_check);
		mUsingLowerCheck = findViewById(R.id.using_lower_check);
	}

	private void initNextFocus(EditText[]... lists) {
		EditText prevLastEdit = null;
		for (EditText[] editTexts : lists) {
			for (EditText edit : editTexts) {
				if (!edit.isEnabled()) {
					continue;
				}
				if (prevLastEdit != null) {
					edit.setNextFocusLeftId(prevLastEdit.getId());
					prevLastEdit.setNextFocusDownId(edit.getId());
				}
				prevLastEdit = edit;
			}
		}
	}

	private void initViewEvent() {
		addNavigationButton("保存", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveUnit()) {
					PLCoreService.getNavigationController().popView();
				}
			}
		});
		mContinueButton = addNavigationButton("連続", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (saveUnit()) {
					clearAll();
				}
			}
		});
		mDeleteButton = addNavigationButton("削除", false, new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLConfirmationPopover(mUnitModel.getName() + "を削除", new PLConfirmationPopover.PLConfirmationListener() {
					@Override
					public void onClickButton(boolean isYes) {
						mUnitModel.delete();
						PLCoreService.getNavigationController().popView();
					}
				}, null);
			}
		});

		findViewById(R.id.clear_area_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLNavigationOverlay navigation = PLCoreService.getNavigationController();
				if (navigation.isHalf()) {
					navigation.resizeNavigation(false, false);
					v.setBackgroundColor(Color.alpha(0));
				} else {
					navigation.resizeNavigation(true, true);
					v.setBackgroundColor(Color.argb(128, 128, 128, 255));
				}
			}
		});
	}

	private boolean saveUnit() {
		if (mTextEdits[0].length() == 0) {
			MYLogUtil.showErrorToast("必須項目漏れ");
			return false;
		}

		PLUnitModel model = (mUnitModel != null) ? mUnitModel : new PLUnitModel();
		model.setBaseHp(MYMathUtil.integerFromTextView(mBaseEdits[0]));
		model.setBaseAttack(MYMathUtil.integerFromTextView(mBaseEdits[1]));
		model.setBaseSpeed(MYMathUtil.integerFromTextView(mBaseEdits[2]));
		model.setBaseDefense(MYMathUtil.integerFromTextView(mBaseEdits[3]));
		model.setBaseResist(MYMathUtil.integerFromTextView(mBaseEdits[4]));
		model.setTurnBuffHp(MYMathUtil.integerFromTextView(mTurnBuffEdits[0]));
		model.setTurnBuffAttack(MYMathUtil.integerFromTextView(mTurnBuffEdits[1]));
		model.setTurnBuffSpeed(MYMathUtil.integerFromTextView(mTurnBuffEdits[2]));
		model.setTurnBuffDefense(MYMathUtil.integerFromTextView(mTurnBuffEdits[3]));
		model.setTurnBuffResist(MYMathUtil.integerFromTextView(mTurnBuffEdits[4]));
		model.setCombatBuffHp(MYMathUtil.integerFromTextView(mCombatBuffEdits[0]));
		model.setCombatBuffAttack(MYMathUtil.integerFromTextView(mCombatBuffEdits[1]));
		model.setCombatBuffSpeed(MYMathUtil.integerFromTextView(mCombatBuffEdits[2]));
		model.setCombatBuffDefense(MYMathUtil.integerFromTextView(mCombatBuffEdits[3]));
		model.setCombatBuffResist(MYMathUtil.integerFromTextView(mCombatBuffEdits[4]));

		model.setName(mTextEdits[0].getText().toString());
		model.setMemo(mTextEdits[1].getText().toString());

		model.setPhysicalAttacker(mPhysicalAttackCheck.isChecked());
		model.setUsingLower(mUsingLowerCheck.isChecked());

		// TODO: 敵キャラは保存せずにListenerで返す？
		boolean isMine = mMineCheck.isChecked();
		if (isMine) {
			model.setMine(true);
			model.save();
		} else if (model.isMine()) {
			model.setMine(false);
			model.delete();
		}
		if (mListener != null) {
			mListener.onUpdateUnitModel(model);
		}
		return true;
	}

	private void clearAll() {
		for (EditText[] edits : new EditText[][]{mBaseEdits, mTurnBuffEdits, mCombatBuffEdits, mTextEdits}) {
			for (EditText text : edits) {
				text.getText().clear();
			}
		}
		mMineCheck.setChecked(false);
		mPhysicalAttackCheck.setChecked(false);
		mUsingLowerCheck.setChecked(false);
	}

	public void setListener(PLOnUpdateUnitListener listener) {
		mListener = listener;
	}

	public interface PLOnUpdateUnitListener {
		void onUpdateUnitModel(PLUnitModel unitModel);
	}
}
