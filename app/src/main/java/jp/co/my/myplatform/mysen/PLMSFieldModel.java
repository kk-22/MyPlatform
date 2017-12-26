package jp.co.my.myplatform.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.database.PLBaseModel;
import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLMSFieldModel extends PLBaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;
	@Column
	private String name;
	@Column
	private String landsText; // 地形情報
	@Column
	private String attackerInitPointsText; // 攻め手の初期位置
	@Column
	private String defenderInitPointsText; // 防ぎ手の初期位置

	@Override
	public void initFromJson(JSONObject jsonObject) throws JSONException {
		no = jsonObject.getInt("no");
		name = jsonObject.getString("名前");
		landsText = jsonObject.getString("地形");
		attackerInitPointsText = jsonObject.getString("攻め手配置");
		defenderInitPointsText = jsonObject.getString("防ぎ手配置");
	}

	@Override
	public String getSheetUrl() {
		return "https://script.google.com/macros/s/AKfycby42Gh5M6Qkc30-KFfsHEncNAUhG-f2B3EhoIj44T--u7hbUoti/exec?sheet=field";
	}

	// getter and setter
	public int[][] getLandNumbers() {
		int[][] result = null;
		String[] landLines = landsText.split("\n");
		int i = 0, j;
		for (String line : landLines) {
			String[] numbers = line.split(",");
			j = 0;
			for (String number : numbers) {
				if (result == null) {
					result = new int[landLines.length][numbers.length];
				}
				result[i][j] = Integer.valueOf(number);
				j++;
			}
			i++;
		}
		return result;
	}

	public MYArrayList<Point> getAttackerInitPointArray() {
		return getInitPointArrayOfText(attackerInitPointsText);
	}

	public MYArrayList<Point> getDefenderInitPointArray() {
		return getInitPointArrayOfText(defenderInitPointsText);
	}

	private MYArrayList<Point> getInitPointArrayOfText(String arrayText) {
		String[] splitTexts = arrayText.split(",");
		MYArrayList<Point> result = new MYArrayList<>(splitTexts.length);
		for (String text : splitTexts) {
			String[] pointTexts = text.split("-");
			if (pointTexts.length != 2) {
				MYLogUtil.showErrorToast("getInitPointArrayOfText initPointArray error : " +name +" " +arrayText);
				return null;
			}
			int x = Integer.valueOf(pointTexts[0]);
			int y = Integer.valueOf(pointTexts[1]);
			result.add(new Point(x, y));
		}
		return result;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLandsText() {
		return landsText;
	}

	public void setLandsText(String landsText) {
		this.landsText = landsText;
	}

	public String getAttackerInitPointsText() {
		return attackerInitPointsText;
	}

	public void setAttackerInitPointsText(String attackerInitPointsText) {
		this.attackerInitPointsText = attackerInitPointsText;
	}

	public String getDefenderInitPointsText() {
		return defenderInitPointsText;
	}

	public void setDefenderInitPointsText(String defenderInitPointsText) {
		this.defenderInitPointsText = defenderInitPointsText;
	}
}
