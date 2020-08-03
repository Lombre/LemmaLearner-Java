package LemmaLearner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;

public class ListSet<E> implements Set<E>, List<E>, Serializable{

	private List<E> internalList = new ArrayList<E>();
	//private Set<E> internalSet = new HashSet<E>();
	
	public ListSet() {
		// TODO Auto-generated constructor stub
	}
	

	public ListSet(Collection<E> collection) {
		for (E e : collection) {
			this.add(e);
		}
	}
	
	@Override
	public int size() {
		return internalList.size();
	}

	@Override
	public boolean isEmpty() {
		return internalList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return internalList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return internalList.iterator();
	}

	@Override
	public Object[] toArray() {
		return internalList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return internalList.toArray(a);
	}

	@Override
	public boolean add(E e) {
		if (internalList.contains(e)) 
			return false;
		return internalList.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return internalList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internalList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean hasChanged = false;
		for (E e : c) {
			boolean currentAddition = this.add(e);
			if (currentAddition) 
				hasChanged = true;
		}
		return hasChanged;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return internalList.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return internalList.removeAll(c);
	}

	@Override
	public void clear() {
		internalList.clear();		
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		throw new Error("Not implemented yet");
	}

	@Override
	public E get(int index) {
		return internalList.get(index);
	}

	@Override
	public E set(int index, E element) {
		return internalList.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		throw new Error("Not implemented yet");		
	}

	@Override
	public E remove(int index) {
		return internalList.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return internalList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return internalList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return internalList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return internalList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return internalList.subList(fromIndex, toIndex);
	}
	@Override
	public Spliterator<E> spliterator() {
		return internalList.spliterator();
	}

}
