package shortener.httphandler.utils;

import com.nimbusds.jose.shaded.json.JSONObject;

/**
 * Helper class that provides methods for creating HTTP request JSON-based response.
 */
public class JsonResponse {

  /**
   * Method that provides stringified JSON based on the specification response structure.
   *
   * @param reasonCode Error reason code
   * @param reasonText Error reason text message
   * @return stringified JSON-based response
   */
  public static String getErrorMessage(Integer reasonCode, String reasonText) {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put("reason_code", reasonCode);
    jsonResponse.put("reason_text", reasonText);

    return jsonResponse.toJSONString();
  }

  /**
   * Method to generate JSON string with token.
   *
   * @param token token to be placed in json
   * @return json with token
   */
  public static String getTokenMessage(String token) {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put("token", token);

    return jsonResponse.toJSONString();
  }

  /**
   * Method to generate JSON string with shortened url.
   *
   * @param shortenedUrl url to be placed in json
   * @return json with shortened url
   */
  public static String getShortenSuccessMessage(String shortenedUrl) {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put("shortened_url", shortenedUrl);

    return jsonResponse.toJSONString();
  }
}
