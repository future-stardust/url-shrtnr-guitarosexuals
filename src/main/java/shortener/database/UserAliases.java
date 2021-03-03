package shortener.database;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * UserAliases record type, reflects User-Alias relation.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserAliases(Integer userId, String[] aliases) {
}
