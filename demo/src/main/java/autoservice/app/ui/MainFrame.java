package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import autoservice.app.DatabaseConnection;

public class MainFrame extends JFrame {
  public MainFrame(String role) {
    setTitle("Автосервис — управление и отчёты");
    setSize(1150, 720);
    setMinimumSize(new Dimension(840, 570));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(UiStyle.SURFACE);
    header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
    JLabel title = UiStyle.title("Автосервис");
    JLabel roleLabel = new JLabel("Роль: " + ("admin".equals(role) ? "Администратор" : "Пользователь"));
    roleLabel.setForeground(UiStyle.MUTED);
    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
    left.setOpaque(false);
    left.add(title);
    left.add(roleLabel);

    JButton logoutButton = new JButton("Выйти");
    UiStyle.actionButton(logoutButton);
    logoutButton.addActionListener(e -> {
      DatabaseConnection.endSession();
      new LoginFrame().setVisible(true);
      dispose();
    });
    header.add(left, BorderLayout.WEST);
    header.add(logoutButton, BorderLayout.EAST);
    add(header, BorderLayout.NORTH);

    JTabbedPane tabs = new JTabbedPane();
    tabs.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
    boolean admin = "admin".equals(role);
    tabs.addTab("Автомобили", new CarPanel(admin));
    tabs.addTab("Мастера", new MasterPanel(admin));
    tabs.addTab("Услуги", new ServicePanel(admin));
    tabs.addTab("Работы", new WorkPanel(admin));
    tabs.addTab("Отчёты", new ReportPanel());
    add(tabs, BorderLayout.CENTER);
  }
}
