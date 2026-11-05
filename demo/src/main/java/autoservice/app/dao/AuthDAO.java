
package autoservice.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import autoservice.app.DatabaseConnection;
import autoservice.app.auth.LoginHasher;
import autoservice.app.auth.PasswordHasher;

public class AuthDAO {

  public String authenticate(String login, char[] password) throws Exception {

    char[] sessionPassword = password.clone();

    try {
      String loginHash = LoginHasher.hashLogin(login);

      String url = System.getenv("DB_URL");

      if (url == null || url.isBlank()) {
        throw new IllegalStateException("Не задан DB_URL");
      }

      String sql = "SELECT stored_password_hash, app_role "
          + "FROM public.get_own_auth_data(?)";

      try (Connection connection = DriverManager.getConnection(
          url, login, new String(password));
          PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, loginHash);

        try (ResultSet result = statement.executeQuery()) {

          if (result.next()) {

            String storedHash = result.getString("stored_password_hash");
            String role = result.getString("app_role");

            if (PasswordHasher.verifyPassword(password, storedHash)) {
              DatabaseConnection.startSession(login, sessionPassword);
              return role;
            }
          }
        }

      } catch (SQLException e) {
        String sqlState = e.getSQLState();

        if (sqlState != null && sqlState.startsWith("28")) {
          return null;
        }

        throw e;
      }

      return null;

    } finally {
      Arrays.fill(password, '\0');
      Arrays.fill(sessionPassword, '\0');
    }
  }
}
