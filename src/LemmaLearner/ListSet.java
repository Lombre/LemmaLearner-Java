package LemmaLearner;

import java.io.Serializable;
import java.util.*;


public class ListSet<E> implements List<E>, Serializable, Set<E>{

	private List<E> internalList = new LinkedList<E>();
	private Set<E> internalSet = new TreeSet<E>();
	
	public ListSet() {
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
		return internalSet.contains(o);
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
		if (internalSet.contains(e)) 
			return false;
		internalSet.add(e);
		internalList.add(e);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		internalSet.remove(o);
		return internalList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internalSet.containsAll(c);
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
		throw new Error("Not implemented yet.");
		//return internalSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		internalSet.removeAll(c);
		return internalList.removeAll(c);
	}

	@Override
	public void clear() {
		internalSet.clear();
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
		internalSet.remove(internalList.get(index));
		internalSet.add(element);		
		return internalList.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		throw new Error("Not implemented yet");		
	}

	@Override
	public E remove(int index) {
		var removedObject = internalList.remove(index);
		internalSet.remove(removedObject);
		return removedObject;
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


	public boolean replaceWith(E element, Collection<? extends E> collection) {
		if (!internalSet.contains(element)) {
			throw new Error("Cannot replace something that does not exist in the set to start with.");
		}
		internalSet.remove(element);
		internalSet.addAll(collection);
		int index = internalList.indexOf(element);
		internalList.remove(index);
		internalList.addAll(index, collection);
		return true;
	}
	

	@Override
	public String toString() {
		return internalList.toString();
	}

}
