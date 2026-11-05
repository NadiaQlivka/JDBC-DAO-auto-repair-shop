
package autoservice.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoservice.app.DatabaseConnection;
import autoservice.app.model.Service;

public class ServiceDAO {

  public List<Service> getAllServices() throws SQLException {
    List<Service> services = new ArrayList<>();

    String sql = "SELECT id, name, cost_our, cost_foreign FROM services ORDER BY id";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {

      while (result.next()) {
        Service service = new Service(
            result.getInt("id"),
            result.getString("name"),
            result.getBigDecimal("cost_our"),
            result.getBigDecimal("cost_foreign"));

        services.add(service);
      }
    }

    return services;
  }

  public Service getServiceById(int id) throws SQLException {
    String sql = "SELECT id, name, cost_our, cost_foreign FROM services WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return new Service(
              result.getInt("id"),
              result.getString("name"),
              result.getBigDecimal("cost_our"),
              result.getBigDecimal("cost_foreign"));
        }
      }
    }

    return null;
  }

  public void addService(Service service) throws SQLException {
    String sql = "INSERT INTO services (name, cost_our, cost_foreign) VALUES (?, ?, ?)";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, service.getName());
      statement.setBigDecimal(2, service.getCostOur());
      statement.setBigDecimal(3, service.getCostForeign());

      statement.executeUpdate();
    }
  }

  public void updateService(Service service) throws SQLException {
    String sql = "UPDATE services SET name = ?, cost_our = ?, cost_foreign = ? WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setString(1, service.getName());
      statement.setBigDecimal(2, service.getCostOur());
      statement.setBigDecimal(3, service.getCostForeign());
      statement.setInt(4, service.getId());

      statement.executeUpdate();
    }
  }

  public boolean deleteService(int id) throws SQLException {
    String sql = "DELETE FROM services WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      return statement.executeUpdate() > 0;
    }
  }
}
