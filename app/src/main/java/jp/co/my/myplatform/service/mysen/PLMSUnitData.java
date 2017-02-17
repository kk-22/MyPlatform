package jp.co.my.myplatform.service.mysen;

public class PLMSUnitData {

	private PLMSUnitModel mUnitModel;
	private PLMSABranchData mBranch;

	public PLMSUnitData() {
		mUnitModel = new PLMSUnitModel();
		mBranch = new PLMSABranchData();
	}

	public PLMSABranchData getBranch() {
		return mBranch;
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getSmallImageName();
	}
}
