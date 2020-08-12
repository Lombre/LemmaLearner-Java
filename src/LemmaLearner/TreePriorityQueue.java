package LemmaLearner;
import java.util.*;

public class LazyPriorityQueue<E> extends PriorityQueue<E> {
	HashSet<E> removedElements = new HashSet<E>();
	
	
	@Override
	public boolean remove(Object o) {
		removedElements.add((E) o);
		return true;
	}
	

	@Override
	public E poll() {
		while (true) {
			E polledElement = super.poll();		
			if (removedElements.contains(polledElement)) {
				removedElements.remove(polledElement);
			} else 
				return polledElement;
		}		
	}

	@Override
	public int size() {
		return super.size() - removedElements.size();
	}
	
}
