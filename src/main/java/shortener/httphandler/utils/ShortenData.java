package shortener.httphandler.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Temporary container class for URL shorten data.
 */
public record ShortenData(@JsonProperty("url") String url,
                       @JsonProperty("alias") String alias) {}
