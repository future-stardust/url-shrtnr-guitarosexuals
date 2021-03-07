package shortener.httphandler;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;
import shortener.users.UserRepository;

/**
 * REST API Controller with entrypoints related to user.
 */
@Controller("/users")
public class UserController {

  @Inject
  UserRepository userRepository;

  record UserData(@JsonProperty("email") String email,
                  @JsonProperty("password") String password) {}

  /**
   * Sign Up entrypoint. Provides user registration in the system.
   *
   * @param userData json with credentials (email and password)
   * @return  201 Created - is user created<br>
   *          400 Bad Request - is credentials are wrong<br>
   *          409 Conflict - is user already exists
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signup", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<String> signup(@Body UserData userData) {
    // TODO: add verification
    if (userData.email == null || userData.password == null) {
      return HttpResponse.badRequest("Email and password should not be empty");
    }

    userRepository.create(userData.email, userData.password);
    return HttpResponse.created("User successfully created");
  }

}
