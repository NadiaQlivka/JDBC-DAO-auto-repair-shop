
package autoservice.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import autoservice.app.DatabaseConnection;
import autoservice.app.model.Work;

public class WorkDAO {

  public List<Work> getAllWorks() throws SQLException {
    List<Work> works = new ArrayList<>();

    String sql = "SELECT id, date_work, master_id, car_id, service_id FROM works ORDER BY date_work DESC, id DESC";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet result = statement.executeQuery()) {

      while (result.next()) {
        Work work = new Work(
            result.getInt("id"),
            result.getObject("date_work", LocalDate.class),
            result.getInt("master_id"),
            result.getInt("car_id"),
            result.getInt("service_id"));

        works.add(work);
      }
    }

    return works;
  }

  public Work getWorkById(int id) throws SQLException {
    String sql = "SELECT id, date_work, master_id, car_id, service_id FROM works WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) {
          return new Work(
              result.getInt("id"),
              result.getObject("date_work", LocalDate.class),
              result.getInt("master_id"),
              result.getInt("car_id"),
              result.getInt("service_id"));
        }
      }
    }

    return null;
  }

  public void updateWork(Work work) throws SQLException {
    String sql = "UPDATE works SET date_work = ?, master_id = ?, car_id = ?, service_id = ? WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setObject(1, work.getDateWork());
      statement.setInt(2, work.getMasterId());
      statement.setInt(3, work.getCarId());
      statement.setInt(4, work.getServiceId());
      statement.setInt(5, work.getId());

      statement.executeUpdate();
    }
  }

  public boolean deleteWork(int id) throws SQLException {
    String sql = "DELETE FROM works WHERE id = ?";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, id);

      return statement.executeUpdate() > 0;
    }
  }

  public boolean addWork(Work work) throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {

      String sql = "INSERT INTO works (date_work, master_id, car_id, service_id) VALUES (?, ?, ?, ?)";

      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setObject(1, work.getDateWork());
        statement.setInt(2, work.getMasterId());
        statement.setInt(3, work.getCarId());
        statement.setInt(4, work.getServiceId());

        int updatedRows = statement.executeUpdate();
        return updatedRows > 0;
      }
    }
  }
}
