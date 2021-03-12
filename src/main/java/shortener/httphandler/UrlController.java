package shortener.httphandler;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.stream.Collectors;
import javax.inject.Inject;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.ShortenData;
import shortener.urls.UrlsRepository;
import shortener.users.UserRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/urls")
public class UrlController {

  @Inject
  UrlsRepository urlsRepository;

  @Inject
  UserRepository userRepository;

  /**
   * Entrypoint for shortening urls.
   *
   * @param shortenData json (url, alias: optional)
   * @return OK/error
   */
  @Post(value = "/shorten", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<Object> shortenUrl(@Body ShortenData shortenData, Principal principal) {
    final String url = shortenData.url();
    final String alias = shortenData.alias();
    final String userEmail = principal.getName();

    // JSON content validation
    if (url == null || url.isBlank()) {
      return HttpResponse.badRequest("Invalid data: url parameter should not be empty");
    }

    // URL validation
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      return HttpResponse.badRequest("Invalid url: " + url);
    }

    // User existing check
    Long userId;
    try {
      userId = userRepository.get(userEmail).id();
    } catch (NotFound e) {
      return HttpResponse.unauthorized().body(String.format("User %s not registered", userEmail));
    }

    // Create alias records and check for its uniqueness
    if (alias == null || alias.isBlank()) {
      try {
        urlsRepository.create(shortenData.url(), userId);
      } catch (UniqueViolation e) {
        return HttpResponse.serverError(
            "Server failed to generate unique alias. Please, contact the administrator");
      }
    } else {
      try {
        urlsRepository.create(shortenData.url(), userId, shortenData.alias());
      } catch (UniqueViolation e) {
        return HttpResponse.badRequest(String.format("Specified alias is taken: %s", alias));
      }
    }

    return HttpResponse.created("Url successfully shortened");
  }

  // TODO: temporary implementation
  /**
   * Entrypoint for getting user's url array.
   *
   * @return user's url array
   */
  @Get
  public HttpResponse<String> getUserUrls(Principal principal) {
    Long userId = userRepository.get(principal.getName()).id();
    return HttpResponse
        .ok(urlsRepository.search().stream().filter(el -> el.userId().equals(userId)).collect(
            Collectors.toList()).toString());
  }

  /**
   * Entrypoint for deleting shortened links.
   *
   * @param alias alias of shortened link should be removed
   * @return OK/error
   */
  @Delete(value = "/{alias}")
  public HttpResponse<Object> deleteUrl(@QueryValue String alias) {
    return HttpResponse.ok();
  }
}
