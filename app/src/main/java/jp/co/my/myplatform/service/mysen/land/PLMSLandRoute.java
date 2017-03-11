package jp.co.my.myplatform.service.mysen.land;

import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSLandView;

public class PLMSLandRoute extends MYArrayList<PLMSLandView> {

	public PLMSLandRoute(ArrayList<PLMSLandView> landArray) {
		super(landArray);
	}

	public PLMSLandRoute(PLMSLandView landView) {
		super();
		add(landView);
	}
}
