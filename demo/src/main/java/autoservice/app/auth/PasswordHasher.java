package autoservice.app.auth;

import java.util.Arrays;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordHasher {

  public static String hashPassword(char[] password) {
    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    try {
      return argon2.hash(3, 65536, 1, password);
    } finally {
      Arrays.fill(password, '\0');
    }
  }

  public static boolean verifyPassword(char[] password, String storedHash) {
    Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    try {
    return argon2.verify(storedHash, password);
    } finally {
      Arrays.fill(password, '\0');
    }
  }
}