
package autoservice.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

public class DatabaseConnection {

  private static String dbLogin;
  private static char[] dbPassword;

  public static synchronized void startSession(String login, char[] password) {

    endSession();

    dbLogin = login;
    dbPassword = password.clone();
  }

  public static synchronized Connection getConnection() throws SQLException {

    if (dbLogin == null || dbPassword == null) {
      throw new SQLException("Пользователь не авторизован");
    }

    String url = System.getenv("DB_URL");

    if (url == null || url.isBlank()) {
      throw new SQLException("Не задан DB_URL");
    }

    return DriverManager.getConnection(url, dbLogin, new String(dbPassword));
  }

  public static synchronized void endSession() {

    if (dbPassword != null) {
      Arrays.fill(dbPassword, '\0');
    }

    dbLogin = null;
    dbPassword = null;
  }
}
