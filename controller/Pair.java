package controller;


/**
 * Basic class representing a pair of objects
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class Pair<A, B> {
  /**
   * First object
   */
  private A st;

  /**
   * Second object
   */
  private B nd;

  /**
   * Initializes the {@link Pair}
   * @param first  First object to store
   * @param second Second object to store
   */
  public Pair(A first, B second) {
    this.st = first;
    this.nd = second;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Pair) {
      Pair object = (Pair)obj;
      return object.st.equals(this.st) && object.nd.equals(this.nd);
    }
    return false;
  }

  /**
   * Gets the first object of the Pair
   * @return {@link Pair#first}
   */
  public A first() {
    return this.st;
  }

  /**
   * Gets the second object of the Pair
   * @return {@link Pair#second}
   */
  public B second() {
    return this.nd;
  }
}
