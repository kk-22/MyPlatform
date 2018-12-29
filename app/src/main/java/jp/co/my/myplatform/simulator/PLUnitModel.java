package jp.co.my.myplatform.simulator;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import jp.co.my.myplatform.database.PLDatabase;

@Table(database = PLDatabase.class)
public class PLUnitModel extends BaseModel {

	@PrimaryKey(autoincrement = true)
	private int no;

	@Column
	private boolean isMine;
	@Column
	private String name, memo;
	@Column
	private int baseHp, baseAttack, baseSpeed, baseDefense, baseResist;
	@Column
	private int turnBuffHp, turnBuffAttack, turnBuffSpeed, turnBuffDefense, turnBuffResist;
	@Column
	private int combatBuffHp, combatBuffAttack, combatBuffSpeed, combatBuffDefense, combatBuffResist;


	public int[] getBaseParams() {
		return new int[]{baseHp, baseAttack, baseSpeed, baseDefense, baseResist};
	}

	public int[] getTurnBuffs() {
		return new int[]{turnBuffHp, turnBuffAttack, turnBuffSpeed, turnBuffDefense, turnBuffResist};
	}

	public int[] getCombatBuffs() {
		return new int[]{combatBuffHp, combatBuffAttack, combatBuffSpeed, combatBuffDefense, combatBuffResist};
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public boolean isMine() {
		return isMine;
	}

	public void setMine(boolean mine) {
		isMine = mine;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getBaseHp() {
		return baseHp;
	}

	public void setBaseHp(int baseHp) {
		this.baseHp = baseHp;
	}

	public int getBaseAttack() {
		return baseAttack;
	}

	public void setBaseAttack(int baseAttack) {
		this.baseAttack = baseAttack;
	}

	public int getBaseSpeed() {
		return baseSpeed;
	}

	public void setBaseSpeed(int baseSpeed) {
		this.baseSpeed = baseSpeed;
	}

	public int getBaseDefense() {
		return baseDefense;
	}

	public void setBaseDefense(int baseDefense) {
		this.baseDefense = baseDefense;
	}

	public int getBaseResist() {
		return baseResist;
	}

	public void setBaseResist(int baseResist) {
		this.baseResist = baseResist;
	}

	public int getTurnBuffHp() {
		return turnBuffHp;
	}

	public void setTurnBuffHp(int turnBuffHp) {
		this.turnBuffHp = turnBuffHp;
	}

	public int getTurnBuffAttack() {
		return turnBuffAttack;
	}

	public void setTurnBuffAttack(int turnBuffAttack) {
		this.turnBuffAttack = turnBuffAttack;
	}

	public int getTurnBuffSpeed() {
		return turnBuffSpeed;
	}

	public void setTurnBuffSpeed(int turnBuffSpeed) {
		this.turnBuffSpeed = turnBuffSpeed;
	}

	public int getTurnBuffDefense() {
		return turnBuffDefense;
	}

	public void setTurnBuffDefense(int turnBuffDefense) {
		this.turnBuffDefense = turnBuffDefense;
	}

	public int getTurnBuffResist() {
		return turnBuffResist;
	}

	public void setTurnBuffResist(int turnBuffResist) {
		this.turnBuffResist = turnBuffResist;
	}

	public int getCombatBuffHp() {
		return combatBuffHp;
	}

	public void setCombatBuffHp(int combatBuffHp) {
		this.combatBuffHp = combatBuffHp;
	}

	public int getCombatBuffAttack() {
		return combatBuffAttack;
	}

	public void setCombatBuffAttack(int combatBuffAttack) {
		this.combatBuffAttack = combatBuffAttack;
	}

	public int getCombatBuffSpeed() {
		return combatBuffSpeed;
	}

	public void setCombatBuffSpeed(int combatBuffSpeed) {
		this.combatBuffSpeed = combatBuffSpeed;
	}

	public int getCombatBuffDefense() {
		return combatBuffDefense;
	}

	public void setCombatBuffDefense(int combatBuffDefense) {
		this.combatBuffDefense = combatBuffDefense;
	}

	public int getCombatBuffResist() {
		return combatBuffResist;
	}

	public void setCombatBuffResist(int combatBuffResist) {
		this.combatBuffResist = combatBuffResist;
	}
}
