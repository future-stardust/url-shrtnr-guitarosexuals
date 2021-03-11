package shortener.users.protection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class to provide hash function protection.
 *
 */
public class HashFunction {

  /**
   * Method which is used to hash password.
   *
   * @param rawPassword not hashed password
   * @param emailAddress email address used as seed
   * @return hashed password
   */
  public static String hashOut(String rawPassword, String emailAddress) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      final String localSeed = "AmCDmG";
      final String passWithSalt = rawPassword + emailAddress + localSeed;

      byte[] passBytes = passWithSalt.getBytes();
      byte[] passHash = sha256.digest(passBytes);

      final StringBuilder sb = new StringBuilder();
      for (byte hash : passHash) {
        sb.append(Integer.toString((hash & 0xff) + 0x100, 16).substring(1));
      }

      return sb.toString();

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }
}
