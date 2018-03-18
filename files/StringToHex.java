package files;

import java.util.HashMap;

class StringToHex {
  private static final HashMap<Integer, Character> chars_hex;
  static {
    chars_hex = new HashMap<Integer, Character>(16);
    chars_hex.put(0, '0');
    chars_hex.put(1, '1');
    chars_hex.put(2, '2');
    chars_hex.put(3, '3');
    chars_hex.put(4, '4');
    chars_hex.put(5, '5');
    chars_hex.put(6, '6');
    chars_hex.put(7, '7');
    chars_hex.put(8, '8');
    chars_hex.put(9, '9');
    chars_hex.put(10, 'A');
    chars_hex.put(11, 'B');
    chars_hex.put(12, 'C');
    chars_hex.put(13, 'D');
    chars_hex.put(14, 'E');
    chars_hex.put(15, 'F');
  }

  static String toHex(byte[] text, int size) {
    String chars = new String(new byte[size * 2]);

    for (int txt_i = 0; txt_i < size; txt_i++) {
      char[] hex = charToHex(text[txt_i]);
      chars += hex[0];
      chars += hex[1];
    }
    return chars;
  }

  private static char[] charToHex(byte chr) {
    char[] ret   = new char[2];
    int    major = chr / 16;
    int    minor = chr % 16;

    ret[0] = chars_hex.get(chr / 16);
    ret[1] = chars_hex.get(chr % 16);
    return ret;
  }
}
