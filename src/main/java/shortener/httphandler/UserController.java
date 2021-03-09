package shortener.httphandler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
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
  record UserData(String email, String password) {

  }

  /**
   * Sign Up entrypoint. Provides user registration in the system.
   *
   * @param userData json with credentials (email and password)
   * @return 201 Created - is user created<br> 400 Bad Request - is credentials are wrong<br> 409
   *             Conflict - is user already exists
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signup", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<String> signup(@Body UserData userData) {
    if (userData.email == null || userData.password == null) {
      return HttpResponse.badRequest("Credentials should not be empty.");
    }

    final @Email String userEmail = userData.email;
    final String userPassword = userData.password;

    if (!userEmail.matches(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
      return HttpResponse.badRequest("Invalid email address.");
    }

    if (userPassword.length() < 8) {
      return HttpResponse.badRequest("Password must be at least 8 characters long.");
    }

    if (userPassword.length() > 16) {
      return HttpResponse.badRequest("Password must be no longer than 16 characters.");
    }

    if (!userPassword.matches("(.*[A-Z].*)")) {
      return HttpResponse.badRequest("Password must contain at least one uppercase character.");
    }

    if (!userPassword.matches("(.*[a-z].*)")) {
      return HttpResponse.badRequest("Password must contain at least one lowercase character.");
    }

    if (!userPassword.matches("(.*[0-9].*)")) {
      return HttpResponse.badRequest("Password must contain at least one number.");
    }

    userRepository.create(userEmail, userPassword);
    return HttpResponse.created("User successfully created");
  }

  /**
   * Sign out endpoint. Provides user logout from the system
   *
   * @param httpHeaders HTTP headers reference
   * @return TODO
   */
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/signout")
  public HttpResponse<String> signOut(HttpHeaders httpHeaders) {
    Optional<String> authorizationHeaderOptional = httpHeaders.getAuthorization();

    if (authorizationHeaderOptional.isPresent()) {
      String accessToken = authorizationHeaderOptional.get().replace("Bearer ", "");
      userSessionRepository.delete(accessToken);
      return HttpResponse.ok("Successfully logged out");
    }

    return HttpResponse.serverError("An error occurred while trying to sign out.");
  }
}
