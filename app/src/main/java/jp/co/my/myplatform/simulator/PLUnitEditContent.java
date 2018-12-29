package jp.co.my.myplatform.simulator;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.overlay.PLNavigationOverlay;

public class PLUnitEditContent extends PLContentView {

	EditText[] mBaseEdits, mTurnBuffEdits, mCombatBuffEdits, mTextEdits;
	CheckBox[] mCheckBoxes;

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
		mCheckBoxes = new CheckBox[1];
		mCheckBoxes[0] = findViewById(R.id.mine_check);
		initNextFocus(mBaseEdits, mTurnBuffEdits, mCombatBuffEdits, mTextEdits);
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
		ViewGroup naviBar = (ViewGroup) View.inflate(getContext(), R.layout.navibar_unit_edit, null);
		setNavigationBar(naviBar);
		naviBar.findViewById(R.id.save_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveUnit();
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

	private void saveUnit() {
		if (mTextEdits[0].length() == 0) {
			MYLogUtil.showErrorToast("必須項目漏れ");
			return;
		}

		PLUnitModel model = new PLUnitModel();
		model.setBaseHp(MYMathUtil.integerFromEditText(mBaseEdits[0]));
		model.setBaseAttack(MYMathUtil.integerFromEditText(mBaseEdits[1]));
		model.setBaseSpeed(MYMathUtil.integerFromEditText(mBaseEdits[2]));
		model.setBaseDefense(MYMathUtil.integerFromEditText(mBaseEdits[3]));
		model.setBaseResist(MYMathUtil.integerFromEditText(mBaseEdits[4]));
		model.setTurnBuffHp(MYMathUtil.integerFromEditText(mTurnBuffEdits[0]));
		model.setTurnBuffAttack(MYMathUtil.integerFromEditText(mTurnBuffEdits[1]));
		model.setTurnBuffSpeed(MYMathUtil.integerFromEditText(mTurnBuffEdits[2]));
		model.setTurnBuffDefense(MYMathUtil.integerFromEditText(mTurnBuffEdits[3]));
		model.setTurnBuffResist(MYMathUtil.integerFromEditText(mTurnBuffEdits[4]));
		model.setCombatBuffHp(MYMathUtil.integerFromEditText(mCombatBuffEdits[0]));
		model.setCombatBuffAttack(MYMathUtil.integerFromEditText(mCombatBuffEdits[1]));
		model.setCombatBuffSpeed(MYMathUtil.integerFromEditText(mCombatBuffEdits[2]));
		model.setCombatBuffDefense(MYMathUtil.integerFromEditText(mCombatBuffEdits[3]));
		model.setCombatBuffResist(MYMathUtil.integerFromEditText(mCombatBuffEdits[4]));

		model.setName(mTextEdits[0].getText().toString());
		model.setMemo(mTextEdits[1].getText().toString());

		model.setMine(mCheckBoxes[0].isChecked());
		model.save();

		PLCoreService.getNavigationController().popView();
	}
}
