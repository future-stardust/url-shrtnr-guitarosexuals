package shortener.database.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * UserAliases record type, reflects User-Alias relation.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserAlias(Long userId, String alias) {
}
