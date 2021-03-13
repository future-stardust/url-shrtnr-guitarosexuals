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
   *
   * @return stringified JSON-based response
   */
  public static String createError(Integer reasonCode, String reasonText) {
    final JSONObject jsonResponse = new JSONObject();

    jsonResponse.put("reason_code", reasonCode);
    jsonResponse.put("reason_text", reasonText);

    return jsonResponse.toJSONString();
  }
}
