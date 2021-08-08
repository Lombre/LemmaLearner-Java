package LemmaLearner;
import java.awt.Component;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


import LemmaLearner.*;

public class TreePriorityQueue<E> {
	TreeSet<Pair<List<E>, Double>> treeSet;
	//Primarily used for removing elements
	HashMap<E, Pair<List<E>, Double>> elementToPair = new HashMap<E, Pair<List<E>, Double>>();
	//Primarily used for adding elements
	HashMap<Double, Pair<List<E>, Double>> scoreToPair = new HashMap<Double, Pair<List<E>, Double>>();
	
	public TreePriorityQueue(Comparator<? super Double> comparator) {
		//The comparetor looks at the Double of the pairs in the treeset.
		treeSet = new TreeSet<Pair<List<E>, Double>>((pair1, pair2) -> comparator.compare(pair1.getSecond(), pair2.getSecond()));
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
		Pair<List<E>, Double> listWithScore = elementToPair.get(element);
		treeSet.remove(listWithScore);
		scoreToPair.remove(listWithScore.getSecond());
		elementToPair.remove(element);
		listWithScore.getFirst().remove(element);
		if (0 < listWithScore.getFirst().size()) {
			//The list needs to get added back in:
			Pair<List<E>, Double> updatedListWithScore = listWithScore;
			scoreToPair.put(updatedListWithScore.getSecond(), updatedListWithScore);
			treeSet.add(updatedListWithScore);
		}				
		return true;
	}
	
	
	public void clear() {
		// TODO Auto-generated method stub
		treeSet.clear();
		elementToPair.clear();
		scoreToPair.clear();
	}
	
	public boolean update(E e, Double score) {		
		  if (!elementToPair.containsKey(e)) 
			  throw new Error("Element " + e + " cannot be replaced, as it is not contained in the queue to start with.");
		  remove(e); 
		  Pair<E, Double> newPair = new Pair<E, Double>(e, score); 		
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
			var listWithScore = new Pair<List<E>, Double>(singeltonList, score);
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
