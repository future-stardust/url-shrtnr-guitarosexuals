package shortener.urls.utils;

import java.util.Random;

/**
 * Url helper class for random string generation.
 */
public class RandomStringGenerator {

  private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String lower = "abcdefghijklmnopqrstuvwxyz";
  private static final String num = "1234567890";

  private static final String alphaNumeric = upper + lower + num;

  private static final Random random = new Random();

  /**
   * Generate random alpha numeric string with specified length.
   *
   * @param length length of string to be generated
   * @return random string
   */
  public static String generate(int length) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < length; i++) {
      sb.append(alphaNumeric.charAt(random.nextInt(alphaNumeric.length())));
    }

    return sb.toString();
  }
}
