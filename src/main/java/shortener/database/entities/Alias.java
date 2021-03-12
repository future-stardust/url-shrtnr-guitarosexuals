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

  public static final Integer DEFAULT_RANDOM_ALIAS_LENGTH = 5;
}
