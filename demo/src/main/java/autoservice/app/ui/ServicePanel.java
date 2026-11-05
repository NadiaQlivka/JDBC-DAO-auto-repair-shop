package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import autoservice.app.dao.ServiceDAO;
import autoservice.app.model.Service;

public class ServicePanel extends JPanel {
  private final boolean admin;
  private final ServiceDAO dao = new ServiceDAO();
  private final DefaultTableModel model = new DefaultTableModel(
      new Object[] { "ID", "Название услуги", "Отечественные", "Иномарки" }, 0) {
    @Override
    public boolean isCellEditable(int row, int col) {
      return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
      if (col == 0)
        return Integer.class;
      if (col == 2 || col == 3)
        return BigDecimal.class;
      return String.class;
    }
  };
  private final JTable table = new JTable(model);
  private final JButton refresh = new JButton("Обновить");
  private final JButton add = new JButton("Добавить");
  private final JButton edit = new JButton("Изменить");
  private final JButton delete = new JButton("Удалить");

  public ServicePanel(boolean admin) {
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
        new int[] { 8, 46, 23, 23 },
        new int[] { SwingConstants.CENTER, SwingConstants.LEFT,
            SwingConstants.RIGHT, SwingConstants.RIGHT }),
        BorderLayout.CENTER);
    refresh.addActionListener(e -> loadServices());
    add.addActionListener(e -> addService());
    edit.addActionListener(e -> editService());
    delete.addActionListener(e -> deleteService());
    loadServices();
  }

  private Service selectedService() {
    int selected = table.getSelectedRow();
    if (selected < 0) {
      JOptionPane.showMessageDialog(this, "Выберите услугу в таблице");
      return null;
    }
    int row = table.convertRowIndexToModel(selected);
    return new Service((Integer) model.getValueAt(row, 0),
        (String) model.getValueAt(row, 1), (BigDecimal) model.getValueAt(row, 2),
        (BigDecimal) model.getValueAt(row, 3));
  }

  private Service showServiceForm(Service current) {
    JTextField name = new JTextField(22);
    JTextField domestic = new JTextField(15);
    JTextField foreign = new JTextField(15);
    if (current != null) {
      name.setText(current.getName());
      domestic.setText(current.getCostOur().toPlainString());
      foreign.setText(current.getCostForeign().toPlainString());
    }
    JPanel form = new JPanel(new GridLayout(3, 2, 12, 12));
    form.add(new JLabel("Название услуги:"));
    form.add(name);
    form.add(new JLabel("Цена для отечественных:"));
    form.add(domestic);
    form.add(new JLabel("Цена для иномарок:"));
    form.add(foreign);
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    int answer = JOptionPane.showConfirmDialog(this, form,
        current == null ? "Добавить услугу" : "Изменить услугу",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (answer != JOptionPane.OK_OPTION)
      return null;
    String title = name.getText().trim();
    if (title.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Укажите название услуги");
      return null;
    }
    try {
      BigDecimal our = new BigDecimal(domestic.getText().trim().replace(',', '.'));
      BigDecimal abroad = new BigDecimal(foreign.getText().trim().replace(',', '.'));
      if (our.signum() < 0 || abroad.signum() < 0) {
        JOptionPane.showMessageDialog(this, "Стоимость не может быть отрицательной");
        return null;
      }
      return current == null
          ? new Service(title, our, abroad)
          : new Service(current.getId(), title, our, abroad);
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "Введите корректные числа, например 2500.50");
      return null;
    }
  }

  private void addService() {
    if (!admin)
      return;
    Service service = showServiceForm(null);
    if (service != null)
      change(() -> {
        dao.addService(service);
        return true;
      }, "Услуга добавлена");
  }

  private void editService() {
    if (!admin)
      return;
    Service previous = selectedService();
    if (previous == null)
      return;
    Service updated = showServiceForm(previous);
    if (updated != null)
      change(() -> {
        dao.updateService(updated);
        return true;
      }, "Изменение выполнено");
  }

  private void deleteService() {
    if (!admin)
      return;
    Service service = selectedService();
    if (service == null)
      return;
    int confirm = JOptionPane.showConfirmDialog(this,
        "Удалить услугу «" + service.getName() + "»?\nВнимание: связанные работы могут удалиться автоматически.",
        "Подтвердите удаление", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION)
      change(() -> dao.deleteService(service.getId()), "Услуга удалена");
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
          JOptionPane.showMessageDialog(ServicePanel.this, changed ? message : "Запись не найдена");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          showError(e.getCause());
        } finally {
          setBusy(false);
        }
        if (changed)
          loadServices();
      }
    }.execute();
  }

  private void loadServices() {
    setBusy(true);
    new SwingWorker<List<Service>, Void>() {
      @Override
      protected List<Service> doInBackground() throws Exception {
        return dao.getAllServices();
      }

      @Override
      protected void done() {
        try {
          List<Service> services = get();
          model.setRowCount(0);
          for (Service s : services)
            model.addRow(new Object[] {
                s.getId(), s.getName(), s.getCostOur(), s.getCostForeign()
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
