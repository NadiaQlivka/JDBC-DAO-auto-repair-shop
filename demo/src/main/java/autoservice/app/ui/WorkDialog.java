package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import autoservice.app.model.Car;
import autoservice.app.model.Master;
import autoservice.app.model.Service;
import autoservice.app.model.Work;

public class WorkDialog extends JDialog {
  private final JTextField dateField = new JTextField(15);
  private final JComboBox<Choice> masterBox = new JComboBox<>();
  private final JComboBox<Choice> carBox = new JComboBox<>();
  private final JComboBox<Choice> serviceBox = new JComboBox<>();
  private Work result;

  public WorkDialog(Window owner, List<Master> masters, List<Car> cars, List<Service> services) {
    super(owner, "Назначить работу", Dialog.ModalityType.APPLICATION_MODAL);
    dateField.setText(LocalDate.now().toString());
    for (Master m : masters)
      masterBox.addItem(new Choice(m.getId(), m.getName()));
    for (Car c : cars)
      carBox.addItem(new Choice(c.getId(), c.getNum() + " — " + c.getMark()));
    for (Service s : services)
      serviceBox.addItem(new Choice(s.getId(), s.getName()));
    JPanel form = new JPanel(new GridLayout(4, 2, 12, 12));
    form.setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));
    form.add(new JLabel("Дата (ГГГГ-ММ-ДД):"));
    form.add(dateField);
    form.add(new JLabel("Мастер:"));
    form.add(masterBox);
    form.add(new JLabel("Автомобиль:"));
    form.add(carBox);
    form.add(new JLabel("Услуга:"));
    form.add(serviceBox);
    JButton save = new JButton("Назначить");
    JButton cancel = new JButton("Отмена");
    UiStyle.actionButton(save);
    UiStyle.actionButton(cancel);
    save.addActionListener(e -> save());
    cancel.addActionListener(e -> dispose());
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    buttons.add(save);
    buttons.add(cancel);
    add(form, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);
    getRootPane().setDefaultButton(save);
    pack();
    setMinimumSize(new Dimension(600, 325));
    setLocationRelativeTo(owner);
  }

  private void save() {
    Choice master = (Choice) masterBox.getSelectedItem();
    Choice car = (Choice) carBox.getSelectedItem();
    Choice service = (Choice) serviceBox.getSelectedItem();
    if (master == null || car == null || service == null) {
      JOptionPane.showMessageDialog(this, "Выберите мастера, автомобиль и услугу");
      return;
    }
    try {
      LocalDate date = LocalDate.parse(dateField.getText().trim());
      result = new Work(date, master.id, car.id, service.id);
      dispose();
    } catch (DateTimeParseException e) {
      JOptionPane.showMessageDialog(this, "Введите дату в формате ГГГГ-ММ-ДД");
    }
  }

  public Work open() {
    setVisible(true);
    return result;
  }

  private static class Choice {
    private final int id;
    private final String label;

    private Choice(int id, String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
