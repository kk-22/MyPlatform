package jp.co.my.myplatform.service.model;

import java.util.Calendar;

public class PLNewsGroupModel {

	private int no;
	private int color;
	private String title;
	private Calendar fetchedDate;			// 最終更新日
	private Calendar readDate;			// 最終閲覧日

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Calendar getFetchedDate() {
		return fetchedDate;
	}

	public void setFetchedDate(Calendar fetchedDate) {
		this.fetchedDate = fetchedDate;
	}

	public Calendar getReadDate() {
		return readDate;
	}

	public void setReadDate(Calendar readDate) {
		this.readDate = readDate;
	}
}
