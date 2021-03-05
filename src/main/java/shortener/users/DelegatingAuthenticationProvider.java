package shortener.users;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.Flowable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import org.reactivestreams.Publisher;

/**
 * Basic authentication provider.
 */
public class DelegatingAuthenticationProvider implements AuthenticationProvider {

  @Inject
  UserRepository userRepository;

  /**
   * Authentication method with basic validation: password check and user existing check.
   *
   * @param httpRequest TODO
   * @param authRequest TODO
   * @return UserDetails with token / auth exception
   */
  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<?> httpRequest,
      AuthenticationRequest<?, ?> authRequest) {
    String enteredEmail = authRequest.getIdentity().toString();
    String enteredPassword = authRequest.getSecret().toString();

    try {
      String userPassword = userRepository.getUserPassword(enteredEmail);
      if (enteredPassword.equals(userPassword)) {
        UserDetails details = new UserDetails(enteredEmail, new ArrayList<>());
        return Flowable.just(details);
      } else {
        return Flowable.just(new AuthenticationFailed("Wrong password"));
      }
    } catch (NoSuchElementException ignored) {
      return Flowable.just(new AuthenticationFailed("No such user"));
    }
  }
}
