package jp.co.my.myplatform.news;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLBadWordModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;
	@ForeignKey
	PLNewsGroupModel groupForeign;

	@Column
	private String word;

	public PLBadWordModel() {
		super();
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

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}
