package jp.co.my.myplatform.service.mysen.Land;

import java.util.ArrayList;

import jp.co.my.myplatform.service.mysen.PLMSLandView;

public class PLMSLandRoute extends ArrayList<PLMSLandView> {

	public PLMSLandRoute(ArrayList<PLMSLandView> landArray) {
		super(landArray);
	}

	public PLMSLandRoute(PLMSLandView landView) {
		super();
		add(landView);
	}

	@Override
	public PLMSLandRoute clone() {
		return new PLMSLandRoute(this);
	}

	public PLMSLandView getLastLandView() {
		int size = size();
		if (0 < size) {
			return get(size - 1);
		}
		return null;
	}
}
