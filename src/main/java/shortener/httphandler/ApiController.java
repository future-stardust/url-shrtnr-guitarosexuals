package shortener.httphandler;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;
import shortener.database.Alias;
import shortener.urls.UrlsRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_ANONYMOUS) // TODO: should be setup in the future
@Controller
public class ApiController {

  @Inject
  UrlsRepository urlsRepository;

  /**
  * Example entrypoint.
  *
  * @return status of running
  */
  @Get(value = "/hello", produces = MediaType.APPLICATION_JSON)
  public Alias[] hello() {
    return urlsRepository.search();
  }

}
