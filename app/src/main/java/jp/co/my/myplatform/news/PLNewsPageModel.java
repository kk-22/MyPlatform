package jp.co.my.myplatform.news;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLNewsPageModel extends BaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@ForeignKey
	PLNewsGroupModel groupForeign;
	@ForeignKey
	PLNewsSiteModel siteForeign;

	@Column
	private String title;
	@Column
	private String url;
	@Column
	private Calendar postedDate;		// RSSに記載された登録日
	@Column
	private int positionNo;
	@Column
	private boolean alreadyRead;

	public PLNewsPageModel() {
		super();
	}

	@Override
	public boolean equals(Object obj) {
		PLNewsPageModel page = MYOtherUtil.castObject(obj, this.getClass());
		if (page == null) {
			return false;
		}
		if (url == null) {
			return super.equals(obj);
		}
		return url.equals(page.getUrl());
	}

	public String getPostedString() {
		if (postedDate == null) {
			return "-";
		}
		SimpleDateFormat format = new SimpleDateFormat("MM/dd(E)HH:mm");
		return format.format(postedDate.getTime());
	}

	// 一覧に表示する際の境界線
	public boolean isPartitionCell() {
		return (url == null);
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public PLNewsGroupModel getGroupForeign() {
		return groupForeign;
	}

	public void setGroupForeign(PLNewsGroupModel groupForeign) {
		this.groupForeign = groupForeign;
	}

	public PLNewsSiteModel getSiteForeign() {
		return siteForeign;
	}

	public void setSiteForeign(PLNewsSiteModel siteForeign) {
		this.siteForeign = siteForeign;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Calendar getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Calendar postedDate) {
		this.postedDate = postedDate;
	}

	public int getPositionNo() {
		return positionNo;
	}

	public void setPositionNo(int positionNo) {
		this.positionNo = positionNo;
	}

	public boolean isAlreadyRead() {
		return alreadyRead;
	}

	public void setAlreadyRead(boolean alreadyRead) {
		this.alreadyRead = alreadyRead;
	}
}
