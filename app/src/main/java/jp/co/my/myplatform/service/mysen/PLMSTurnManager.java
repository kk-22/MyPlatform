package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public class PLMSTurnManager {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private PLMSUserInterface mUserInterface;

	private int mNumberOfTurn;								// 現在のターン数
	private PLMSArmyStrategy mCurrentArmy;					// 現ターンのArmy
	private MYArrayList<PLMSArmyStrategy> mArmyArray;

	public PLMSTurnManager(PLMSInformationView information, PLMSFieldView field) {
		mInformation = information;
		mField = field;

		mArmyArray = new MYArrayList<>();
		// TODO: 別の場所にArmyArrayを作り、このクラスはそれを引数で受け取る
		for (PLMSUnitView unitView : mField.getUnitViewArray()) {
			PLMSArmyStrategy armyStrategy = unitView.getUnitData().getArmyStrategy();
			if (!mArmyArray.contains(armyStrategy)) {
				mArmyArray.add(armyStrategy);
			}
			armyStrategy.addUnitView(unitView);
		}

		mUserInterface = new PLMSUserInterface(mInformation, mField, mField.getUnitViewArray());
		startNextTurn();
	}

	public void startNextTurn() {
		if (mCurrentArmy == null) {
			// 最初のターン
			mNumberOfTurn = 1;
			mCurrentArmy = mArmyArray.getFirst();
		} else {
			mCurrentArmy = mArmyArray.getNextOfObject(mCurrentArmy);
			if (mCurrentArmy.equals(mArmyArray.getFirst())) {
				// 次のターン
				mNumberOfTurn++;
			}
		}

		for (PLMSUnitView unitView : mCurrentArmy.getUnitViewArray()) {
			unitView.resetForNewTurn(mNumberOfTurn);
		}
		mUserInterface.enableInterfaceForArmy(mCurrentArmy);
	}
}