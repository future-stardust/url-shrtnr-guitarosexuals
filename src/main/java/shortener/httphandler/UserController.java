package shortener.httphandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.constraints.Email;
import shortener.exceptions.auth.InvalidCredentials;
import shortener.users.UserRepository;
import shortener.users.UserSessionRepository;

/**
 * REST API Controller with entrypoints related to user.
 */
@Controller("/users")
public class UserController {

  @Inject
  UserRepository userRepository;
  @Inject
  UserSessionRepository userSessionRepository;

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  record UserData(@JsonProperty("email") String email, @JsonProperty("password") String password) {

    public void validate() {
      if (!email.matches(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
        throw new InvalidCredentials("Invalid email address.");
      }

      if (password.length() < 8) {
        throw new InvalidCredentials("Password must be at least 8 characters long.");
      }

      if (password.length() > 16) {
        throw new InvalidCredentials("Password must be no longer than 16 characters.");
      }

      if (!password.matches("(.*[A-Z].*)")) {
        throw new InvalidCredentials("Password must contain at least one uppercase character.");
      }

      if (!password.matches("(.*[a-z].*)")) {
        throw new InvalidCredentials("Password must contain at least one lowercase character.");
      }

      if (!password.matches("(.*[0-9].*)")) {
        throw new InvalidCredentials("Password must contain at least one number.");
      }
    }
  }

  /**
   * Sign Up entrypoint. Provides user registration in the system.
   *
   * @param userData json with credentials (email and password)
   * @return  201 Created - if user created<br>
   *          400 Bad Request - if credentials are wrong<br>
   *          409 Conflict - if user already exists
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signup", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<String> signup(@Body UserData userData) {
    if (userData.email == null || userData.password == null) {
      return HttpResponse.badRequest("Credentials should not be empty.");
    }

    final @Email String userEmail = userData.email;
    final String userPassword = userData.password;

    try {
      userData.validate();
    } catch (InvalidCredentials e) {
      return HttpResponse.badRequest(e.getMessage());
    }

    try {
      userRepository.create(userEmail, userPassword);
    } catch (IllegalArgumentException e) {
      return HttpResponse.status(HttpStatus.CONFLICT).body(
          String.format("User %s has already been registered", userEmail)
      );
    }

    return HttpResponse.created("User successfully registered");
  }

  /**
   * Sign out endpoint. Provides user logout from the system
   *
   * @param httpHeaders HTTP headers reference
   * @return  200 if user logged out<br>
   *          500 if something went wrong with token
   */
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/signout")
  public HttpResponse<String> signout(HttpHeaders httpHeaders) {
    Optional<String> authorizationHeaderOptional = httpHeaders.getAuthorization();

    if (authorizationHeaderOptional.isPresent()) {
      String accessToken = authorizationHeaderOptional.get().replace("Bearer ", "");
      userSessionRepository.delete(accessToken);
      return HttpResponse.ok("Successfully logged out");
    }

    return HttpResponse.serverError("An error occurred while trying to sign out.");
  }
}
