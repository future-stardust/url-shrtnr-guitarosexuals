package shortener.httphandler;

import com.nimbusds.jose.shaded.json.JSONObject;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import javax.inject.Inject;
import shortener.database.entities.Alias;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.ShortenData;
import shortener.urls.UrlRepository;
import shortener.users.UserRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/urls")
public class UrlController {

  protected final String host;

  public UrlController(EmbeddedServer embeddedServer) {
    host = embeddedServer.getHost();
  }

  @Inject
  UrlRepository urlRepository;

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
      return HttpResponse.badRequest("Invalid data: \"url\" parameter should not be empty");
    }

    // URL Validation - general
    try {
      new URL(url);
    } catch (MalformedURLException exc) {
      return HttpResponse.badRequest("Invalid URL: " + url);
    }

    // URL Validation - local URLs are not allowed
    if (url.startsWith(host) || url.startsWith("http://" + host) || url.startsWith("https://" + host)) {
      return HttpResponse.badRequest("Invalid data: Local URLs are not allowed");
    }

    // User existing check
    Long userId;
    try {
      userId = userRepository.getByEmail(userEmail).id();
    } catch (NotFound exc) {
      return HttpResponse.unauthorized()
          .body(String.format("User %s is not registered", userEmail));
    }

    // Create alias records and check for its uniqueness
    if (alias == null || alias.isBlank()) {
      urlRepository.createRandomAlias(url, userId);
    } else {
      try {
        urlRepository.create(new Alias(alias, url, userId));
      } catch (UniqueViolation exc) {
        return HttpResponse.badRequest("Specified alias is taken");
      }
    }

    return HttpResponse.created("URL successfully shortened");
  }

  /**
   * Entrypoint for getting user's url array.
   *
   * @return user's url array
   */
  @Get
  public HttpResponse<Object> getUserUrls(Principal principal) {
    String userEmail = principal.getName();

    User user;
    try {
      user = userRepository.getByEmail(userEmail);
    } catch (NotFound exc) {
      return HttpResponse.unauthorized();
    }

    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put("urls", urlRepository.searchByUserId(user.id()));

    return HttpResponse.ok(jsonResponse);
  }

  /**
   * Entrypoint for deleting shortened links.
   *
   * @param alias alias of shortened link should be removed
   * @return OK/error
   */
  @Delete(value = "/{alias}")
  public HttpResponse<Object> deleteUrl(@QueryValue String alias, Principal principal) {
    String userEmail = principal.getName();

    User user;
    try {
      user = userRepository.getByEmail(userEmail);
    } catch (NotFound exc) {
      return HttpResponse.unauthorized();
    }

    try {
      Alias aliasToDelete = urlRepository.get(alias);

      if (!aliasToDelete.userId().equals(user.id())) {
        throw new NotFound("aliases", alias);
      }

      return HttpResponse.ok(urlRepository.delete(alias));
    } catch (NotFound exc) {
      return HttpResponse.notFound();
    }
  }
}
