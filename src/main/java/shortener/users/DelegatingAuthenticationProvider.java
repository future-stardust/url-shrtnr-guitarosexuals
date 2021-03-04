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
import javax.inject.Inject;
import org.reactivestreams.Publisher;

/**
 * Basic authentication provider.
 */
public class DelegatingAuthenticationProvider implements AuthenticationProvider {

  @Inject
  UserRepository userRepository;

  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<?> httpRequest,
      AuthenticationRequest<?, ?> req) {
    String email = req.getIdentity().toString();
    String password = req.getSecret().toString();
    if (password.equals(userRepository.getUserPassword(email))) {
      UserDetails details = new UserDetails(email, new ArrayList<>());
      return Flowable.just(details);
    } else {
      return Flowable.just(new AuthenticationFailed());
    }
  }
}
