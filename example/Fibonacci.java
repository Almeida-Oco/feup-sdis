public class Fibonacci {
  public static void main(String[] args) {
    long f = 0;
    long g = 1;

    for (int i = 1; i <= 101; i++) {
      System.out.print(f + " ");
      f = f + g;
      g = f - g;
    }

    System.out.println();
  }
}
