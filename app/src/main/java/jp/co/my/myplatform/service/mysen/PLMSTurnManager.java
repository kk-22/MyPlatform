package jp.co.my.myplatform.service.mysen;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

public class PLMSTurnManager {

	private PLMSArgument mArgument;
	private int mNumberOfTurn;								// 現在のターン数
	private PLMSArmyStrategy mCurrentArmy;					// 現ターンのArmy
	private MYArrayList<PLMSArmyStrategy> mArmyArray;

	public PLMSTurnManager(PLMSArgument argument) {
		mArgument = argument;
		mArmyArray = new MYArrayList<>();
		// TODO: 別の場所にArmyArrayを作り、このクラスはそれを引数で受け取る
		for (PLMSUnitView unitView : argument.getFieldView().getUnitViewArray()) {
			PLMSArmyStrategy armyStrategy = unitView.getUnitData().getArmyStrategy();
			if (!mArmyArray.contains(armyStrategy)) {
				mArmyArray.add(armyStrategy);
			}
		}

		for (PLMSArmyStrategy army : mArmyArray) {
			PLMSUserInterface userInterface
					= new PLMSUserInterface(argument, army);
			army.setUnitInterface(userInterface);
		}
		startNextTurn();
	}

	// ターン終了
	public void finishTurn() {
		mArgument.getInformationView().clearInformation();

		for (PLMSUnitView unitView : mCurrentArmy.getUnitViewArray()) {
			unitView.resetForFinishTurn(mNumberOfTurn);
		}
		mCurrentArmy.getUnitInterface().disableInterface();
		startNextTurn();
	}

	// 次のターンへ
	private void startNextTurn() {
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
		mArgument.getAnimationManager().sendTogetherAnimator();

		mCurrentArmy.getUnitInterface().enableInterface();
	}
}
