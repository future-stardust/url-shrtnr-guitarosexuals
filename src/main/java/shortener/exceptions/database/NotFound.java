package shortener.exceptions.database;

import java.util.NoSuchElementException;

/**
 * Exception for not found record.
 */
public class NotFound extends NoSuchElementException {
  private static final String message = "Record was not found!";

  public NotFound() {
    super(message);
  }
}
