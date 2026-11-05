package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import autoservice.app.model.Car;

public class CarDialog extends JDialog {
  private final JTextField numField = new JTextField(20);
  private final JTextField colorField = new JTextField(20);
  private final JTextField markField = new JTextField(20);
  private final JCheckBox foreignBox = new JCheckBox();
  private final Car originalCar;
  private Car result;

  public CarDialog(Window owner, Car car) {
    super(owner, car == null ? "Добавить автомобиль" : "Изменить автомобиль",
        Dialog.ModalityType.APPLICATION_MODAL);
    originalCar = car;
    JPanel form = new JPanel(new GridLayout(4, 2, 12, 12));
    form.setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));
    form.add(new JLabel("Номер:"));
    form.add(numField);
    form.add(new JLabel("Цвет:"));
    form.add(colorField);
    form.add(new JLabel("Марка:"));
    form.add(markField);
    form.add(new JLabel("Иномарка:"));
    form.add(foreignBox);
    if (car != null) {
      numField.setText(car.getNum());
      colorField.setText(car.getColor());
      markField.setText(car.getMark());
      foreignBox.setSelected(car.isForeign());
    }
    JButton save = new JButton("Сохранить");
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
    setMinimumSize(new Dimension(490, 315));
    setLocationRelativeTo(owner);
  }

  private void save() {
    String num = numField.getText().trim();
    String color = colorField.getText().trim();
    String mark = markField.getText().trim();
    if (num.isEmpty() || color.isEmpty() || mark.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Заполните все поля");
      return;
    }
    boolean foreign = foreignBox.isSelected();
    result = originalCar == null
        ? new Car(num, color, mark, foreign)
        : new Car(originalCar.getId(), num, color, mark, foreign);
    dispose();
  }

  public Car open() {
    setVisible(true);
    return result;
  }
}
