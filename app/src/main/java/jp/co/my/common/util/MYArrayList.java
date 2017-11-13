package jp.co.my.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public class MYArrayList<E> extends ArrayList<E> {

	public MYArrayList() {
		super();
	}

	public MYArrayList(Collection<? extends E> c) {
		super(c);
	}

	// https://docs.oracle.com/javase/jp/8/docs/technotes/guides/language/non-reifiable-varargs.html
	@SuppressWarnings({"unchecked", "varargs"})
	public MYArrayList(E... objects) {
		super(Arrays.asList(objects));
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

	public void addIfNoContain(E object) {
		if (!contains((object))) {
			add(object);
		}
	}

	public void addAllOnlyNoContain(Collection<? extends E> c) {
		for (E object : c) {
			if (!contains(object)) {
				add(object);
			}
		}
	}

	public void removeLast() {
		remove(getLast());
	}

	// 指定した要素とそれ以降の要素を全て削除
	public void removeToLastFromIndex(int index) {
		int numberOfRemove = size() - index;
		for (int i = 0; i < numberOfRemove; i++) {
			removeLast();
		}
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

	// 自身と引数の配列の双方に含まれる要素のみを持つ配列を返す
	public MYArrayList<E> filterByArray(MYArrayList<E> array) {
		MYArrayList<E> result = new MYArrayList<>(size());
		for (E obj : array) {
			if (contains(obj)) {
				result.add(obj);
			}
		}
		return result;
	}
}
