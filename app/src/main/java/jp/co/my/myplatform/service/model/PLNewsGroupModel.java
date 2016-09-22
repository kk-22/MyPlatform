package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Calendar;
import java.util.List;

@ModelContainer
@Table(database = PLDatabase.class)
public class PLNewsGroupModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;

	@Column
	private String color;
	@Column
	private String title;
	@Column
	private int updateInterval;				// 自動更新間隔分
	@Column
	private Calendar fetchedDate;			// 最終更新日
	@Column
	private Calendar readDate;				// 最終閲覧日

	public List<PLNewsSiteModel> siteArray;
	public List<PLNewsPageModel> pageArray;

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

	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
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

	@OneToMany(methods = {OneToMany.Method.ALL}, variableName = "siteArray")
	public List<PLNewsSiteModel> getSiteArray() {
		if (siteArray == null || siteArray.isEmpty()) {
			siteArray = SQLite.select()
					.from(PLNewsSiteModel.class)
					.where(PLNewsSiteModel_Table.groupForeign_no.eq(no))
					.queryList();
		}
		return siteArray;
	}

	public void setSiteArray(List<PLNewsSiteModel> siteArray) {
		this.siteArray = siteArray;
	}

	public List<PLNewsPageModel> getPageArray() {
		if (pageArray == null || pageArray.isEmpty()) {
			pageArray = SQLite.select()
					.from(PLNewsPageModel.class)
					.where(PLNewsPageModel_Table.groupForeign_no.eq(no))
					.queryList();
		}
		return pageArray;
	}

	public void setPageArray(List<PLNewsPageModel> pageArray) {
		this.pageArray = pageArray;
	}
}
