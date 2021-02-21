package edu.kpi.testcourse.rest;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import java.util.ArrayList;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;

/**
 * Micronaut authentication bean that contains authorization logic: ensures that a user is
 * registered in the system and password is right.
 */
@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<?> httpRequest,
      AuthenticationRequest<?, ?> authenticationRequest
  ) {
    // TODO Here you need to implement an actual authentication (ensure that the user is registered
    //  and password is OK)
    return Flowable.create(emitter -> {
      if (authenticationRequest.getIdentity().equals("sherlock")
          && authenticationRequest.getSecret().equals("password")) {
        emitter
          .onNext(new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>()));
        emitter.onComplete();
      } else {
        emitter.onError(new AuthenticationException(new AuthenticationFailed()));
      }
    }, BackpressureStrategy.ERROR);
  }
}
