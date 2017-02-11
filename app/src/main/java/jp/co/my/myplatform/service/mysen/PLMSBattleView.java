package jp.co.my.myplatform.service.mysen;

import android.view.LayoutInflater;

import java.util.ArrayList;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;


public class PLMSBattleView extends PLContentView {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;

	private ArrayList<PLMSUnitView> mUnitArray;

	public PLMSBattleView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_battle, this);
		mInformation = (PLMSInformationView) findViewById(R.id.information_view);
		mField = (PLMSFieldView) findViewById(R.id.field_view);

		mUnitArray = new ArrayList<>();

		createUnitView();
		mField.setUnitArray(mUnitArray);
	}

	public void createUnitView() {
		PLMSUnitView unitView = new PLMSUnitView(getContext());
		mUnitArray.add(unitView);
	}
}
