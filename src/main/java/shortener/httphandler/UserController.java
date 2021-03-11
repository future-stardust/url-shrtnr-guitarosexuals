package shortener.httphandler;

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
import shortener.exceptions.database.UniqueViolation;
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
    if (userData.email() == null || userData.password() == null) {
      return HttpResponse.badRequest("Credentials should not be empty.");
    }

    final @Email String userEmail = userData.email();
    final String userPassword = userData.password();

    try {
      userData.validate();
    } catch (InvalidCredentials exc) {
      return HttpResponse.badRequest(exc.getMessage());
    }

    final  String hashedPassword = userRepository.hashOut(userPassword, userEmail);

    try {
      userRepository.create(userEmail, hashedPassword);
    } catch (UniqueViolation exc) {
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
