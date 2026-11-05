
package autoservice.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoservice.app.DatabaseConnection;
import autoservice.app.model.Master;

public class MasterDAO {

  public List<Master> getAllMasters() throws SQLException {
    List<Master> masters = new ArrayList<>();

    String sql = "SELECT id, name FROM masters ORDER BY id";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {

      while (result.next()) {
        Master master = new Master(
            result.getInt("id"),
            result.getString("name"));

        masters.add(master);
      }
    }

    return masters;
  }

  public Master getMasterById(int id) throws SQLException {
    String sql = "SELECT id, name FROM masters WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return new Master(
              result.getInt("id"),
              result.getString("name"));
        }
      }
    }

    return null;
  }

  public void addMaster(Master master) throws SQLException {
    String sql = "INSERT INTO masters (name) VALUES (?)";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, master.getName());

      statement.executeUpdate();
    }
  }

  public void updateMaster(Master master) throws SQLException {
    String sql = "UPDATE masters SET name = ? WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, master.getName());
      statement.setInt(2, master.getId());

      statement.executeUpdate();
    }
  }

  public boolean deleteMaster(int id) throws SQLException {
    String sql = "DELETE FROM masters WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      return statement.executeUpdate() > 0;
    }
  }
}
