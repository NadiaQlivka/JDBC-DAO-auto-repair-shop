package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import autoservice.app.dao.CarDAO;
import autoservice.app.dao.MasterDAO;
import autoservice.app.dao.ServiceDAO;
import autoservice.app.dao.WorkDAO;
import autoservice.app.model.Car;
import autoservice.app.model.Master;
import autoservice.app.model.Service;
import autoservice.app.model.Work;

public class WorkPanel extends JPanel {
  private final boolean admin;
  private final WorkDAO dao = new WorkDAO();
  private final DefaultTableModel model = new DefaultTableModel(
      new String[] { "ID", "Дата", "Мастер", "Автомобиль", "Услуга" }, 0) {
    @Override
    public boolean isCellEditable(int row, int col) {
      return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
      if (col == 0)
        return Integer.class;
      if (col == 1)
        return LocalDate.class;
      return String.class;
    }
  };
  private final JTable table = new JTable(model);
  private final JButton refresh = new JButton("Обновить");
  private final JButton assign = new JButton("Назначить работу");

  public WorkPanel(boolean admin) {
    super(new BorderLayout());
    this.admin = admin;
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);
    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    UiStyle.actionButton(refresh);
    UiStyle.actionButton(assign);
    toolbar.add(refresh);
    if (admin)
      toolbar.add(assign);
    add(toolbar, BorderLayout.NORTH);
    add(UiStyle.tableScroll(table,
        new int[] { 7, 14, 22, 30, 27 },
        new int[] { SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT,
            SwingConstants.LEFT, SwingConstants.LEFT }),
        BorderLayout.CENTER);
    refresh.addActionListener(e -> loadWorks());
    assign.addActionListener(e -> assignWork());
    loadWorks();
  }

  private List<Object[]> fetchRows() throws SQLException {
    Map<Integer, String> masterNames = new HashMap<>();
    Map<Integer, String> carNames = new HashMap<>();
    Map<Integer, String> serviceNames = new HashMap<>();
    for (Master m : new MasterDAO().getAllMasters())
      masterNames.put(m.getId(), m.getName());
    for (Car c : new CarDAO().getAllCars())
      carNames.put(c.getId(), c.getNum() + " — " + c.getMark());
    for (Service s : new ServiceDAO().getAllServices())
      serviceNames.put(s.getId(), s.getName());
    List<Object[]> rows = new ArrayList<>();
    for (Work w : dao.getAllWorks())
      rows.add(new Object[] {
          w.getId(), w.getDateWork(),
          masterNames.getOrDefault(w.getMasterId(), "ID " + w.getMasterId()),
          carNames.getOrDefault(w.getCarId(), "ID " + w.getCarId()),
          serviceNames.getOrDefault(w.getServiceId(), "ID " + w.getServiceId())
      });
    return rows;
  }

  private void loadWorks() {
    setBusy(true);
    new SwingWorker<List<Object[]>, Void>() {
      @Override
      protected List<Object[]> doInBackground() throws Exception {
        return fetchRows();
      }

      @Override
      protected void done() {
        try {
          List<Object[]> rows = get();
          model.setRowCount(0);
          for (Object[] r : rows)
            model.addRow(r);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          setBusy(false);
        }
      }
    }.execute();
  }

  private void assignWork() {
    if (!admin)
      return;
    setBusy(true);
    new SwingWorker<ReferenceData, Void>() {
      @Override
      protected ReferenceData doInBackground() throws Exception {
        return new ReferenceData(new MasterDAO().getAllMasters(),
            new CarDAO().getAllCars(), new ServiceDAO().getAllServices());
      }

      @Override
      protected void done() {
        ReferenceData data;
        try {
          data = get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        } catch (ExecutionException e) {
          showError(e.getCause());
          return;
        } finally {
          setBusy(false);
        }
        if (data.masters.isEmpty() || data.cars.isEmpty() || data.services.isEmpty()) {
          JOptionPane.showMessageDialog(WorkPanel.this,
              "Добавьте мастера, автомобиль и услугу перед назначением работы");
          return;
        }
        Work work = new WorkDialog(SwingUtilities.getWindowAncestor(WorkPanel.this),
            data.masters, data.cars, data.services).open();
        if (work != null)
          saveWork(work);
      }
    }.execute();
  }

  private void saveWork(Work work) {
    if (!admin)
      return;
    setBusy(true);
    new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() throws Exception {
        return dao.addWork(work);
      }

      @Override
      protected void done() {
        boolean saved = false;
        try {
          saved = get();
          JOptionPane.showMessageDialog(WorkPanel.this,
              saved ? "Работа назначена" : "Не удалось добавить работу");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          setBusy(false);
        }
        if (saved)
          loadWorks();
      }
    }.execute();
  }

  private void setBusy(boolean busy) {
    refresh.setEnabled(!busy);
    if (admin)
      assign.setEnabled(!busy);
  }

  private void showError(Throwable cause) {
    if (cause != null)
      cause.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Операция не выполнена: " + (cause == null ? "Неизвестная ошибка" : cause.getMessage()),
        "Ошибка", JOptionPane.ERROR_MESSAGE);
  }

  private static class ReferenceData {
    final List<Master> masters;
    final List<Car> cars;
    final List<Service> services;

    ReferenceData(List<Master> m, List<Car> c, List<Service> s) {
      masters = m;
      cars = c;
      services = s;
    }
  }
}
