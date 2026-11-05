package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import autoservice.app.dao.ReportDAO;

public class ReportPanel extends JPanel {
  private final ReportDAO reportDAO = new ReportDAO();

  private final JTextField startField = new JTextField(
      LocalDate.now().withDayOfMonth(1).toString(), 12);
  private final JTextField endField = new JTextField(LocalDate.now().toString(), 12);
  private final JTextField monthField = new JTextField(YearMonth.now().toString(), 9);

  private final JButton costButton = new JButton("Рассчитать стоимость");
  private final JButton topButton = new JButton("Показать топ-5");

  private final JLabel ourValue = metricValue();
  private final JLabel foreignValue = metricValue();
  private final JLabel totalValue = metricValue();

  private final DefaultTableModel topModel = new DefaultTableModel(
      new Object[] { "ID мастера", "Имя мастера", "Автомобилей" }, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }

    @Override
    public Class<?> getColumnClass(int column) {
      return switch (column) {
        case 0 -> Integer.class;
        case 2 -> Long.class;
        default -> String.class;
      };
    }
  };

  public ReportPanel() {
    super(new BorderLayout(0, 15));
    setBackground(UiStyle.BG);
    setBorder(BorderFactory.createEmptyBorder(15, 18, 18, 18));

    JPanel sections = new JPanel(new BorderLayout(0, 14));
    sections.setOpaque(false);
    sections.add(createCostSection(), BorderLayout.NORTH);
    sections.add(createTopSection(), BorderLayout.CENTER);
    add(sections, BorderLayout.CENTER);
  }

  private JPanel createCostSection() {
    JPanel card = UiStyle.card("Стоимость обслуживания за период");
    JPanel content = new JPanel(new BorderLayout(0, 14));
    content.setOpaque(false);

    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    filters.setOpaque(false);
    filters.add(new JLabel("С:"));
    filters.add(startField);
    filters.add(new JLabel("По:"));
    filters.add(endField);
    UiStyle.actionButton(costButton);
    filters.add(costButton);

    JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
    metrics.setOpaque(false);
    metrics.add(metricCard("Отечественные", ourValue));
    metrics.add(metricCard("Иномарки", foreignValue));
    metrics.add(metricCard("Общая сумма", totalValue));

    content.add(filters, BorderLayout.NORTH);
    content.add(metrics, BorderLayout.CENTER);
    card.add(content, BorderLayout.CENTER);
    costButton.addActionListener(e -> loadCosts());
    return card;
  }

  private JPanel createTopSection() {
    JPanel card = UiStyle.card("Пять лучших мастеров за месяц");
    JPanel content = new JPanel(new BorderLayout(0, 10));
    content.setOpaque(false);

    JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    filters.setOpaque(false);
    filters.add(new JLabel("Месяц (ГГГГ-ММ):"));
    filters.add(monthField);
    UiStyle.actionButton(topButton);
    filters.add(topButton);

    JTable table = new JTable(topModel);
    table.setAutoCreateRowSorter(true);
    JScrollPane scroll = UiStyle.tableScroll(table,
        new int[] { 15, 55, 30 },
        new int[] { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.CENTER });
    content.add(filters, BorderLayout.NORTH);
    content.add(scroll, BorderLayout.CENTER);
    card.add(content, BorderLayout.CENTER);
    topButton.addActionListener(e -> loadTopMasters());
    return card;
  }

  private static JLabel metricValue() {
    JLabel label = new JLabel("—");
    label.setFont(new Font("Segoe UI", Font.BOLD, 24));
    label.setForeground(UiStyle.TEXT);
    return label;
  }

  private static JPanel metricCard(String caption, JLabel value) {
    JPanel card = new JPanel(new BorderLayout(0, 5));
    card.setBackground(UiStyle.BG);
    card.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
    JLabel title = new JLabel(caption);
    title.setForeground(UiStyle.MUTED);
    card.add(title, BorderLayout.NORTH);
    card.add(value, BorderLayout.CENTER);
    return card;
  }

  private void loadCosts() {
    LocalDate startDate;
    LocalDate endDate;
    try {
      startDate = LocalDate.parse(startField.getText().trim());
      endDate = LocalDate.parse(endField.getText().trim());
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(this, "Введите даты в формате ГГГГ-ММ-ДД");
      return;
    }
    if (endDate.isBefore(startDate)) {
      JOptionPane.showMessageDialog(this, "Дата окончания раньше даты начала");
      return;
    }
    costButton.setEnabled(false);
    new SwingWorker<ReportDAO.CostReport, Void>() {
      @Override
      protected ReportDAO.CostReport doInBackground() throws Exception {
        return reportDAO.getTotalCosts(startDate, endDate);
      }

      @Override
      protected void done() {
        try {
          ReportDAO.CostReport result = get();
          ourValue.setText(result.totalOur().toPlainString());
          foreignValue.setText(result.totalForeign().toPlainString());
          totalValue.setText(result.totalOur().add(result.totalForeign()).toPlainString());
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          costButton.setEnabled(true);
        }
      }
    }.execute();
  }

  private void loadTopMasters() {
    LocalDate selectedMonth;
    try {
      selectedMonth = YearMonth.parse(monthField.getText().trim()).atDay(1);
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(this, "Введите месяц в формате ГГГГ-ММ");
      return;
    }
    topButton.setEnabled(false);
    new SwingWorker<List<ReportDAO.MasterReport>, Void>() {
      @Override
      protected List<ReportDAO.MasterReport> doInBackground() throws Exception {
        return reportDAO.getTopFiveMasters(selectedMonth);
      }

      @Override
      protected void done() {
        try {
          List<ReportDAO.MasterReport> rows = get();
          topModel.setRowCount(0);
          for (ReportDAO.MasterReport master : rows) {
            topModel.addRow(new Object[] {
                master.id(), master.name(), master.carsCount()
            });
          }
          if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(ReportPanel.this,
                "За выбранный месяц нет выполненных работ");
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          topButton.setEnabled(true);
        }
      }
    }.execute();
  }

  private void showError(Throwable cause) {
    if (cause != null)
      cause.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Не удалось построить отчёт. Подробности в консоли.",
        "Ошибка отчёта", JOptionPane.ERROR_MESSAGE);
  }
}
