package network;

import java.util.HashMap;

class HexToString {
  private static final HashMap<Character, Integer> hex_chars;

  static {
    hex_chars = new HashMap<Character, Integer>(16);
    hex_chars.put('0', 0);
    hex_chars.put('1', 1);
    hex_chars.put('2', 2);
    hex_chars.put('3', 3);
    hex_chars.put('4', 4);
    hex_chars.put('5', 5);
    hex_chars.put('6', 6);
    hex_chars.put('7', 7);
    hex_chars.put('8', 8);
    hex_chars.put('9', 9);
    hex_chars.put('A', 10);
    hex_chars.put('B', 11);
    hex_chars.put('C', 12);
    hex_chars.put('D', 13);
    hex_chars.put('E', 14);
    hex_chars.put('F', 15);
    hex_chars.put('a', 10);
    hex_chars.put('b', 11);
    hex_chars.put('c', 12);
    hex_chars.put('d', 13);
    hex_chars.put('e', 14);
    hex_chars.put('f', 15);
  }

  private static char charFromHex(char major, char minor) {
    System.out.println("Maj " + major + ", min " + minor);
    int maj = hex_chars.get(major),
        min = hex_chars.get(minor);

    return (char)(maj * 16 + min);
  }

  static String fromHex(byte[] hex, int size) {
    String txt = new String(new byte[size / 2 + 1]);

    for (int i = 0; i < size - 1; i += 2) {
      char major = (char)hex[i];
      char minor = (char)hex[i + 1];

      txt += "" + charFromHex(major, minor);
    }

    return txt;
  }
}
