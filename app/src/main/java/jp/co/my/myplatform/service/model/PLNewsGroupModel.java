package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;

@Table(database = PLDatabase.class)
public class PLNewsGroupModel extends BaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;

	@Column
	private int color;
	@Column
	private String title;
	@Column
	private Calendar fetchedDate;			// 最終更新日
	@Column
	private Calendar readDate;				// 最終閲覧日

	public PLNewsGroupModel() {
		super();
	}

	private PLNewsGroupModel(int color, String title) {
		this();
		this.color = color;
		this.title = title;
	}

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
