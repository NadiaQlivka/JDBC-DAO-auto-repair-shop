
package autoservice.app.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import autoservice.app.DatabaseConnection;

public class ReportDAO {

  public record CostReport(BigDecimal totalOur, BigDecimal totalForeign) {
  }

  public record MasterReport(int id, String name, long carsCount) {
  }

  public CostReport getTotalCosts(LocalDate startDate, LocalDate endDate) throws SQLException {

    String sql = "CALL public.get_the_total_cost_of_servicing_our_and_foreign_cars" + "(?, ?, NULL, NULL)";

    try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setDate(1, Date.valueOf(startDate));
      statement.setDate(2, Date.valueOf(endDate));

      try (ResultSet result = statement.executeQuery()) {

        if (result.next()) {

          BigDecimal totalOur = result.getBigDecimal("total_our");

          BigDecimal totalForeign = result.getBigDecimal("total_foreign");

          if (totalOur == null) {
            totalOur = BigDecimal.ZERO;
          }

          if (totalForeign == null) {
            totalForeign = BigDecimal.ZERO;
          }

          return new CostReport(totalOur, totalForeign);
        }
      }
    }

    throw new SQLException("Процедура не вернула результат по стоимости");
  }

  public List<MasterReport> getTopFiveMasters(LocalDate selectedMonth) throws SQLException {

    List<MasterReport> masters = new ArrayList<>();

    try (Connection connection = DatabaseConnection.getConnection()) {

      connection.setAutoCommit(false);

      try {
        String sql = "CALL public.top_five_masters(" + "?, 'autoservice_top5_cursor'::refcursor)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

          statement.setDate(1, Date.valueOf(selectedMonth));

          try (ResultSet result = statement.executeQuery()) {
          }
        }

        String fetchSql = "FETCH ALL FROM autoservice_top5_cursor";

        try (Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(fetchSql)) {

          while (result.next()) {

            MasterReport master = new MasterReport(
                result.getInt("master_id"),
                result.getString("master_name"),
                result.getLong("cars_count"));

            masters.add(master);
          }
        }

        connection.commit();

        return masters;

      } catch (SQLException e) {
        try {
          connection.rollback();
        } catch (SQLException rollbackError) {
          e.addSuppressed(rollbackError);
        }

        throw e;
      }
    }
  }
}
