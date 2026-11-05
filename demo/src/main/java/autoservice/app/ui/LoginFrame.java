package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import autoservice.app.dao.AuthDAO;

public class LoginFrame extends JFrame {
  private final JTextField loginField = new JTextField(22);
  private final JPasswordField passwordField = new JPasswordField(22);
  private final JButton loginButton = new JButton("Войти");

  public LoginFrame() {
    setTitle("Автосервис — вход");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(510, 310);
    setMinimumSize(new Dimension(460, 290));
    setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout(0, 16));
    root.setBackground(UiStyle.BG);
    root.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
    root.add(UiStyle.title("Вход в систему"), BorderLayout.NORTH);

    JPanel form = new JPanel(new GridLayout(2, 2, 12, 14));
    form.setOpaque(false);
    form.add(new JLabel("Логин:"));
    form.add(loginField);
    form.add(new JLabel("Пароль:"));
    form.add(passwordField);
    root.add(form, BorderLayout.CENTER);

    UiStyle.actionButton(loginButton);
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottom.setOpaque(false);
    bottom.add(loginButton);
    root.add(bottom, BorderLayout.SOUTH);
    setContentPane(root);

    loginButton.addActionListener(e -> login());
    getRootPane().setDefaultButton(loginButton);
  }

  private void login() {
    String login = loginField.getText().trim();
    char[] password = passwordField.getPassword();
    passwordField.setText("");
    if (login.isEmpty() || password.length == 0) {
      Arrays.fill(password, '\0');
      JOptionPane.showMessageDialog(this, "Введите логин и пароль");
      return;
    }
    loginButton.setEnabled(false);
    new SwingWorker<String, Void>() {
      @Override
      protected String doInBackground() throws Exception {
        return new AuthDAO().authenticate(login, password);
      }

      @Override
      protected void done() {
        loginButton.setEnabled(true);
        try {
          String role = get();
          if (role == null) {
            JOptionPane.showMessageDialog(LoginFrame.this, "Неверный логин или пароль");
            return;
          }
          new MainFrame(role).setVisible(true);
          dispose();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          JOptionPane.showMessageDialog(LoginFrame.this,
              "Не удалось войти. Проверьте подключение и учётные данные.",
              "Ошибка входа", JOptionPane.ERROR_MESSAGE);
          if (e.getCause() != null)
            e.getCause().printStackTrace();
        }
      }
    }.execute();
  }
}
