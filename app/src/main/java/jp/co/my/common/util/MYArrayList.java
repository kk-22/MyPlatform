package jp.co.my.common.util;

import java.util.ArrayList;
import java.util.Collection;


public class MYArrayList<E> extends ArrayList<E> {

	public MYArrayList() {
		super();
	}

	public MYArrayList(Collection<? extends E> c) {
		super(c);
	}

	public MYArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	public E getFirst() {
		if (size() == 0) {
			return null;
		}
		return get(0);
	}

	public E getLast() {
		int size = size();
		if (size == 0) {
			return null;
		}
		return get(size - 1);
	}

	public boolean addIfNotNull(E object) {
		if (object != null) {
			add(object);
			return true;
		}
		return false;
	}

	public boolean addOrMoveLast(E object) {
		boolean exist = contains(object);
		if (exist) {
			remove(object);
		}
		add(object);
		return exist;
	}

	public E getNextOfObject(E object) {
		int index = indexOf(object);
		if (index == -1) {
			return null;
		}
		int nextIndex = index + 1;
		if (nextIndex < size()) {
			return get(nextIndex);
		}
		return get(0);
	}

	public int indexOfLast() {
		return size() - 1;
	}

	public int countObject(E object) {
		int count = 0;
		for (E obj : this) {
			if (obj.equals(object)) {
				count++;
			}
		}
		return count;
	}
}
