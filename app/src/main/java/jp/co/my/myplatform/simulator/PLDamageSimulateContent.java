package jp.co.my.myplatform.simulator;

import android.view.LayoutInflater;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;

public class PLDamageSimulateContent extends PLContentView {

	public PLDamageSimulateContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_damage_simulate, this);
	}
}
