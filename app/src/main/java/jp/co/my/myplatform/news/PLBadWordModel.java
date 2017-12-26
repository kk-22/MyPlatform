package jp.co.my.myplatform.news;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.myplatform.database.PLDatabase;

@ModelContainer
@Table(database = PLDatabase.class)
public class PLBadWordModel extends BaseModel {

	@PrimaryKey(autoincrement = false)
	private int no;
	@ForeignKey
	ForeignKeyContainer<PLNewsGroupModel> groupForeign;

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

	public ForeignKeyContainer<PLNewsGroupModel> getGroupForeign() {
		return groupForeign;
	}

	public void associateGroup(PLNewsGroupModel model) {
		this.groupForeign = FlowManager.getContainerAdapter(PLNewsGroupModel.class)
				.toForeignKeyContainer(model);
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
}
