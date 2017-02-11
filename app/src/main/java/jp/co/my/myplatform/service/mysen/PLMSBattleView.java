package jp.co.my.myplatform.service.mysen;

import android.view.LayoutInflater;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;


public class PLMSBattleView extends PLContentView {

	public PLMSBattleView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_battle, this);
	}
}
