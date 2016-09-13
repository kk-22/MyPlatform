package jp.co.my.myplatform.service.model;

import java.util.Calendar;

public class PLNewsGroupModel {

	private int mTabNo;
	private int mTabColor;
	private String mTabName;
	private Calendar mFetchedDate;			// 最終更新日
	private Calendar mReadDate	;			// 最終閲覧日

	public int getTabNo() {
		return mTabNo;
	}

	public void setTabNo(int tabNo) {
		mTabNo = tabNo;
	}

	public int getTabColor() {
		return mTabColor;
	}

	public void setTabColor(int tabColor) {
		mTabColor = tabColor;
	}

	public String getTabName() {
		return mTabName;
	}

	public void setTabName(String tabName) {
		mTabName = tabName;
	}

	public Calendar getFetchedDate() {
		return mFetchedDate;
	}

	public void setFetchedDate(Calendar fetchedDate) {
		mFetchedDate = fetchedDate;
	}

	public Calendar getReadDate() {
		return mReadDate;
	}

	public void setReadDate(Calendar readDate) {
		mReadDate = readDate;
	}
}
