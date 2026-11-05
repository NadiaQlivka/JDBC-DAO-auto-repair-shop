package autoservice.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import autoservice.app.ui.LoginFrame;
import autoservice.app.ui.UiStyle;

public class App {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        System.err.println("Не удалось применить системный стиль: " + e.getMessage());
      }
      UiStyle.install();
      new LoginFrame().setVisible(true);
    });
  }
}
