package jp.co.my.myplatform.service.calculator;

import android.view.LayoutInflater;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;


public class PLCACalculatorContent extends PLContentView {

	public PLCACalculatorContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_calculator, this);
	}
}
