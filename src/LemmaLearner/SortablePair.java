package LemmaLearner;


public class SortablePair<A, B> implements Comparable<SortablePair<A, B>> {
    private A first;
    private B second;

    public SortablePair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
    }

    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
        if (other instanceof SortablePair) {
            SortablePair<A, B> otherPair = (SortablePair<A, B>) other;
            //Only discerned based on the first element.
            return 
            ((  this.first == otherPair.first ||
                ( this.first != null && otherPair.first != null &&
                  this.first.equals(otherPair.first)))) &&
            ((  this.second == otherPair.second ||
            ( this.second != null && otherPair.second != null &&
              this.second.equals(otherPair.second))))
            ;
        }

        return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(SortablePair<A, B> o) {
		if (o.first.equals(this.first)) return 0;
		else return ((Comparable<B>) second).compareTo(o.second);
	}

}
