package jp.co.my.myplatform.service.mysen;

public class PLMSLandData {

	private int mType;

	PLMSLandData(int landNumber) {
		mType = landNumber % 10;
	}
}
