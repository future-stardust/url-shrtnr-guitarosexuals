package shortener.database;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Alias record type.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record Alias(String alias,
                    String url,
                    Integer userId,
                    Integer usages) {
}
