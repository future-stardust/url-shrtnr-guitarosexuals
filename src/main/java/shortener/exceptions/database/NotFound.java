package shortener.exceptions.database;

import java.util.NoSuchElementException;

/**
 * Exception for not found record.
 */
public class NotFound extends NoSuchElementException {
  private static final String message = "%s(%s) was not found!";

  public NotFound(String table, Object pk) {
    super(String.format(message, table, pk));
  }
}
