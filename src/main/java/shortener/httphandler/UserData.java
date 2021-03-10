package shortener.httphandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import shortener.exceptions.auth.InvalidCredentials;

/**
 * Temporary container class for credentials. Provides auto json parsing and data validation
 */
public record UserData(@JsonProperty("email") String email,
                       @JsonProperty("password") String password) {

  private static final String EMAIL_ADDRESS_PATTERN =
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
  private static final String PASSWORD_UPPERCASE_PATTERN = "(.*[A-Z].*)";
  private static final String PASSWORD_LOWERCASE_PATTERN = "(.*[a-z].*)";
  private static final String PASSWORD_NUMBER_PATTERN = "(.*[0-9].*)";

  /**
   * Credentials validation method. Used to check contained fields for compliance with the
   * established rules.
   */
  public void validate() {
    if (!email.matches(EMAIL_ADDRESS_PATTERN)) {
      throw new InvalidCredentials("Invalid email address.");
    }

    if (password.length() < 8) {
      throw new InvalidCredentials("Password must be at least 8 characters long.");
    }

    if (password.length() > 16) {
      throw new InvalidCredentials("Password must be no longer than 16 characters.");
    }

    if (!password.matches(PASSWORD_UPPERCASE_PATTERN)) {
      throw new InvalidCredentials("Password must contain at least one uppercase character.");
    }

    if (!password.matches(PASSWORD_LOWERCASE_PATTERN)) {
      throw new InvalidCredentials("Password must contain at least one lowercase character.");
    }

    if (!password.matches(PASSWORD_NUMBER_PATTERN)) {
      throw new InvalidCredentials("Password must contain at least one number.");
    }
  }
}
