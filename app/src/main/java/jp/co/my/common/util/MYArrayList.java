package jp.co.my.common.util;

import java.util.ArrayList;


public class MYArrayList<E> extends ArrayList<E> {

	public E getLast() {
		int size = size();
		if (size == 0) {
			return null;
		}
		return get(size - 1);
	}

	public boolean addOrMoveLast(E object) {
		boolean exist = contains(object);
		if (exist) {
			remove(object);
		}
		add(object);
		return exist;
	}
}
