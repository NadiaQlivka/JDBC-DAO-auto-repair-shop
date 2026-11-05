package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.concurrent.Callable;
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
import autoservice.app.model.Car;

public class CarPanel extends JPanel {
  private final boolean admin;
  private final CarDAO dao = new CarDAO();
  private final DefaultTableModel model = new DefaultTableModel(
      new Object[] { "ID", "Номер", "Цвет", "Марка", "Иномарка" }, 0) {
    @Override
    public boolean isCellEditable(int row, int col) {
      return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
      if (col == 0)
        return Integer.class;
      if (col == 4)
        return Boolean.class;
      return String.class;
    }
  };
  private final JTable table = new JTable(model);
  private final JButton refresh = new JButton("Обновить");
  private final JButton add = new JButton("Добавить");
  private final JButton edit = new JButton("Изменить");
  private final JButton delete = new JButton("Удалить");

  public CarPanel(boolean admin) {
    super(new BorderLayout());
    this.admin = admin;
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);
    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    for (JButton b : new JButton[] { refresh, add, edit, delete })
      UiStyle.actionButton(b);
    toolbar.add(refresh);
    if (admin) {
      toolbar.add(add);
      toolbar.add(edit);
      toolbar.add(delete);
    }
    add(toolbar, BorderLayout.NORTH);
    add(UiStyle.tableScroll(table,
        new int[] { 8, 20, 18, 40, 14 },
        new int[] { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT,
            SwingConstants.LEFT, SwingConstants.CENTER }),
        BorderLayout.CENTER);
    refresh.addActionListener(e -> loadCars());
    add.addActionListener(e -> addCar());
    edit.addActionListener(e -> editCar());
    delete.addActionListener(e -> deleteCar());
    loadCars();
  }

  private Car selectedCar() {
    int vRow = table.getSelectedRow();
    if (vRow < 0) {
      JOptionPane.showMessageDialog(this, "Выберите автомобиль в таблице");
      return null;
    }
    int row = table.convertRowIndexToModel(vRow);
    return new Car((Integer) model.getValueAt(row, 0),
        (String) model.getValueAt(row, 1), (String) model.getValueAt(row, 2),
        (String) model.getValueAt(row, 3), (Boolean) model.getValueAt(row, 4));
  }

  private void addCar() {
    if (!admin)
      return;
    Car car = new CarDialog(SwingUtilities.getWindowAncestor(this), null).open();
    if (car != null)
      change(() -> {
        dao.addCar(car);
        return true;
      }, "Автомобиль добавлен");
  }

  private void editCar() {
    if (!admin)
      return;
    Car current = selectedCar();
    if (current == null)
      return;
    Car updated = new CarDialog(SwingUtilities.getWindowAncestor(this), current).open();
    if (updated != null)
      change(() -> {
        dao.updateCar(updated);
        return true;
      }, "Изменение выполнено");
  }

  private void deleteCar() {
    if (!admin)
      return;
    Car car = selectedCar();
    if (car == null)
      return;
    int confirm = JOptionPane.showConfirmDialog(this,
        "Удалить автомобиль «" + car.getNum() + "»?", "Подтвердите удаление",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION)
      change(() -> dao.deleteCar(car.getId()), "Автомобиль удалён");
  }

  private void change(Callable<Boolean> action, String message) {
    if (!admin)
      return;
    setBusy(true);
    new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() throws Exception {
        return action.call();
      }

      @Override
      protected void done() {
        boolean changed = false;
        try {
          changed = get();
          JOptionPane.showMessageDialog(CarPanel.this, changed ? message : "Запись не найдена");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          setBusy(false);
        }
        if (changed)
          loadCars();
      }
    }.execute();
  }

  private void loadCars() {
    setBusy(true);
    new SwingWorker<List<Car>, Void>() {
      @Override
      protected List<Car> doInBackground() throws Exception {
        return dao.getAllCars();
      }

      @Override
      protected void done() {
        try {
          List<Car> cars = get();
          model.setRowCount(0);
          for (Car car : cars)
            model.addRow(new Object[] {
                car.getId(), car.getNum(), car.getColor(), car.getMark(), car.isForeign()
            });
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

  private void setBusy(boolean busy) {
    refresh.setEnabled(!busy);
    if (admin) {
      add.setEnabled(!busy);
      edit.setEnabled(!busy);
      delete.setEnabled(!busy);
    }
  }

  private void showError(Throwable cause) {
    if (cause != null)
      cause.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Операция не выполнена: " + (cause == null ? "Неизвестная ошибка" : cause.getMessage()),
        "Ошибка", JOptionPane.ERROR_MESSAGE);
  }
}
