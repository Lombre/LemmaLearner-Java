package LemmaLearner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class TreePriorityQueue<E> {
	TreeSet<SortablePair<List<E>, Double>> treeSet;
	//Primarily used for removing elements
	HashMap<E, SortablePair<List<E>, Double>> elementToPair = new HashMap<E, SortablePair<List<E>, Double>>();
	//Primarily used for adding elements
	HashMap<Double, SortablePair<List<E>, Double>> scoreToPair = new HashMap<Double, SortablePair<List<E>, Double>>();
	
	public TreePriorityQueue(Comparator<? super Double> comparator) {
		//The comparetor looks at the Double of the pairs in the treeset.
		treeSet = new TreeSet<SortablePair<List<E>, Double>>((pair1, pair2) -> comparator.compare(pair1.getSecond(), pair2.getSecond()));
	} 
	
	public E poll() {
		if (size() == 0) 
			return null;		
		E firstElement = peek();
		remove(firstElement);
		return firstElement;
	}

	
	public E element() {
		return peek();
	}

	
	public E peek() {
		List<E> elementsWithHighestScore = treeSet.first().getFirst();
		return elementsWithHighestScore.get(0);
	}

	
	public int size() {
		return elementToPair.size();
	}

	
	public boolean isEmpty() {
		int size = size();
		return size == 0;
	}

	
	public boolean contains(E o) {
		return elementToPair.containsKey(o);
	}

	
	public Iterator<E> iterator() {
		return elementToPair.keySet().iterator();
	}

	
	public boolean remove(E o) {
		E element = (E) o;
		SortablePair<List<E>, Double> listWithScore = elementToPair.get(element);
		treeSet.remove(listWithScore);
		scoreToPair.remove(listWithScore.getSecond());
		elementToPair.remove(element);
		listWithScore.getFirst().remove(element);
		if (0 < listWithScore.getFirst().size()) {
			//The list needs to get added back in:
			SortablePair<List<E>, Double> updatedListWithScore = listWithScore;
			scoreToPair.put(updatedListWithScore.getSecond(), updatedListWithScore);
			treeSet.add(updatedListWithScore);
		}				
		return true;
	}
	
	
	public void clear() {
		treeSet.clear();
		elementToPair.clear();
		scoreToPair.clear();
	}
	
	public boolean update(E e, Double score) {		
		  if (!elementToPair.containsKey(e)) 
			  throw new Error("Element " + e + " cannot be replaced, as it is not contained in the queue to start with.");
		  remove(e); 
		  add(e, score);
		  return true; 
		 
	}

	public boolean add(E e, Double score) {
		if (scoreToPair.containsKey(score)) {
			//The element needs to be added to the list that exists, which is associated with the given score.
			var listWithScore = scoreToPair.get(score);
			listWithScore.getFirst().add(e);
			elementToPair.put(e, listWithScore);
		} else {
			//A new list needs to be created.
			List<E> singeltonList = new ArrayList<E>();
			singeltonList.add(e);
			var listWithScore = new SortablePair<List<E>, Double>(singeltonList, score);
			treeSet.add(listWithScore);
			elementToPair.put(e, listWithScore);
			scoreToPair.put(score, listWithScore);
		}
		return true;
	}

	public ArrayList<E> toList(){
		return new ArrayList<E>(elementToPair.keySet());
	}
	
	public String toString() {
		return treeSet.toString();
	}

}
