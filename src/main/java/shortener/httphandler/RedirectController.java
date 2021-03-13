package shortener.httphandler;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.exceptions.database.NotFound;
import shortener.urls.UrlRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Controller("/r")
@Secured(SecurityRule.IS_ANONYMOUS)
public class RedirectController {

  @Inject
  UrlRepository urlRepository;

  /**
   * Entrypoint for redirecting to a Alias.
   *
   * @param alias alias
   * @return OK/error
   */
  @Get(value = "/{alias}")
  public HttpResponse<Object> redirect(@QueryValue String alias) throws URISyntaxException {
    try {
      Alias aliasRecord = urlRepository.get(alias);

      return HttpResponse.redirect(new URI(aliasRecord.url()));
    } catch (NotFound exc) {
      return HttpResponse.notFound("Alias not found.");
    }
  }
}
