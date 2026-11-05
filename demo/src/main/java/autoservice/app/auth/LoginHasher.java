package autoservice.app.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class LoginHasher {

  public static String hashLogin(String login) throws Exception {
    String key = System.getenv("LOGIN_HASH_KEY");

    if (key == null || key.isBlank()) {
      throw new IllegalStateException("Не задан LOGIN_HASH_KEY");
    }

    byte[] keyBytes = Base64.getDecoder().decode(key);

    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

    Mac algorithm = Mac.getInstance("HmacSHA256");

    algorithm.init(secretKey);

    byte[] loginBytes = login.getBytes(StandardCharsets.UTF_8);

    byte[] hashBytes = algorithm.doFinal(loginBytes);

    return Base64.getEncoder().encodeToString(hashBytes);
  }
}