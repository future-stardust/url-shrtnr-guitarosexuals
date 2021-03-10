package shortener.exceptions.auth;

/**
 * Exception for invalid user data (email and password).
 */
public class InvalidCredentials extends RuntimeException {
  public InvalidCredentials(String message) {
    super(message);
  }
}
