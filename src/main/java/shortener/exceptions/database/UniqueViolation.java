package shortener.exceptions.database;

/**
 * Exception for violation of uniqueness.
 */
public class UniqueViolation extends IllegalArgumentException {
  private static final String message = "Given record already exists in %s!";

  public UniqueViolation(String table) {
    super(String.format(message, table));
  }
}
