package shortener.database.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Alias record type.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record Alias(String alias,
                    String url,
                    Long userId,
                    Integer usages) {

  public static final Integer ALIAS_LENGTH_DEFAULT = 5;

  public Alias(String alias, String url, Long userId) {
    this(alias, url, userId, 0);
  }
}
