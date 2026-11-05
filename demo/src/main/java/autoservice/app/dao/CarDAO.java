package autoservice.app.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import autoservice.app.DatabaseConnection;
import autoservice.app.model.Car;

public class CarDAO {
  public List<Car> getAllCars() throws SQLException {

    List<Car> cars = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection()) {

      String sql = "SELECT id, num, color, mark, is_foreign FROM cars ORDER BY id";
      try (PreparedStatement statement = connection.prepareStatement(sql);
          ResultSet result = statement.executeQuery()) {

        while (result.next()) {
          int id = result.getInt("id");
          String num = result.getString("num");
          String color = result.getString("color");
          String mark = result.getString("mark");
          boolean isForeign = result.getInt("is_foreign") == 1;
          Car car = new Car(id, num, color, mark, isForeign);
          cars.add(car);
        }
      }
    }
    return cars;
  }

  public void addCar(Car car) throws SQLException {

    try (Connection connectoin = DatabaseConnection.getConnection()) {

      String sql = "INSERT INTO cars (num, color, mark, is_foreign) \n VALUES (?, ?, ?, ?)";
      try (PreparedStatement statement = connectoin.prepareStatement(sql)) {
        statement.setString(1, car.getNum());
        statement.setString(2, car.getColor());
        statement.setString(3, car.getMark());
        statement.setInt(4, car.isForeign() ? 1 : 0);

        statement.executeUpdate();
      }
    }
  }

  public Car getCarById(int id) throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {

      String sql = "SELECT id, num, color, mark, is_foreign FROM cars WHERE id = ?";

      try (PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        try (ResultSet result = statement.executeQuery()) {

          if (result.next()) {
            String num = result.getString("num");
            String color = result.getString("color");
            String mark = result.getString("mark");
            boolean isForeign = result.getInt("is_foreign") == 1;
            Car car = new Car(id, num, color, mark, isForeign);
            return car;
          }
        }
      }
    }
    return null;
  }

  public boolean updateCar(Car car) throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {

      String sql = "UPDATE cars SET num = ?, color = ?, mark = ?, is_foreign = ? WHERE id = ?;";

      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, car.getNum());
        statement.setString(2, car.getColor());
        statement.setString(3, car.getMark());
        statement.setInt(4, car.isForeign() ? 1 : 0);
        statement.setInt(5, car.getId());

        int updatedRows = statement.executeUpdate();
        return updatedRows > 0;
      }
    }
  }

  public boolean deleteCar(int id) throws SQLException{

    try(Connection connection = DatabaseConnection.getConnection()){

      String sql = "DELETE FROM cars WHERE id = ?;";

      try(PreparedStatement statement = connection.prepareStatement(sql)){
        statement.setInt(1, id);

        int updatedRows = statement.executeUpdate();
        return updatedRows > 0;
      }
    }
  }
}
