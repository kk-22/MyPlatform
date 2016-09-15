package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;
import java.util.List;

@Table(database = PLDatabase.class)
public class PLNewsGroupModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;

	@Column
	private String color;
	@Column
	private String title;
	@Column
	private Calendar fetchedDate;			// 最終更新日
	@Column
	private Calendar readDate;				// 最終閲覧日

	private List<PLNewsSiteModel> siteArray;

	public PLNewsGroupModel() {
		super();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
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

	public List<PLNewsSiteModel> getSiteArray() {
		if (siteArray == null || siteArray.isEmpty()) {
			siteArray = SQLite.select()
					.from(PLNewsSiteModel.class)
					.where(PLNewsSiteModel_Table.groupNo.eq(no))
					.queryList();
		}
		return siteArray;
	}

	public void setSiteArray(List<PLNewsSiteModel> siteArray) {
		this.siteArray = siteArray;
	}
}
