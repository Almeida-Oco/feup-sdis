package controller;



class Pair<A, B> {
  private A st;
  private B nd;

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

  public A getFirst() {
    return this.st;
  }

  public B getSecond() {
    return this.nd;
  }
}
