package autoservice.app.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class UiStyle {
  public static final Color BG = new Color(245, 247, 251);
  public static final Color SURFACE = Color.WHITE;
  public static final Color BORDER = new Color(221, 227, 236);
  public static final Color TEXT = new Color(31, 42, 58);
  public static final Color MUTED = new Color(86, 100, 119);
  public static final Color BLUE = new Color(34, 99, 165);

  private UiStyle() {
  }

  public static void install() {
    FontUIResource base = new FontUIResource("Segoe UI", Font.PLAIN, 16);
    UIDefaults defaults = UIManager.getDefaults();
    Enumeration<Object> keys = defaults.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      if (key.toString().endsWith(".font") && defaults.get(key) instanceof Font) {
        UIManager.put(key, base);
      }
    }
    UIManager.put("TableHeader.font", new FontUIResource("Segoe UI", Font.BOLD, 16));
    UIManager.put("TabbedPane.font", new FontUIResource("Segoe UI", Font.BOLD, 16));
    UIManager.put("Button.font", new FontUIResource("Segoe UI", Font.BOLD, 15));
    UIManager.put("Table.rowHeight", 38);
  }

  public static JLabel title(String caption) {
    JLabel label = new JLabel(caption);
    label.setFont(new Font("Segoe UI", Font.BOLD, 22));
    label.setForeground(TEXT);
    return label;
  }

  public static JPanel card(String title) {
    JPanel panel = new JPanel(new BorderLayout(0, 12));
    panel.setBackground(SURFACE);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER, 1, true),
        BorderFactory.createEmptyBorder(15, 18, 15, 18)));
    JLabel heading = new JLabel(title);
    heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
    heading.setForeground(TEXT);
    panel.add(heading, BorderLayout.NORTH);
    return panel;
  }

  public static void actionButton(JButton button) {
    button.setFont(new Font("Segoe UI", Font.BOLD, 15));
    button.setMargin(new Insets(8, 16, 8, 16));
    button.setFocusPainted(false);
  }
  
  public static JScrollPane tableScroll(
      JTable table, int[] weights, int[] alignments) {
    int count = table.getColumnCount();
    if (weights.length != count || alignments.length != count) {
      throw new IllegalArgumentException("Число размеров не совпадает с числом столбцов");
    }
    table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    table.setRowHeight(39);
    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(true);
    table.setGridColor(BORDER);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setSelectionBackground(new Color(219, 234, 253));
    table.setSelectionForeground(TEXT);
    table.setFillsViewportHeight(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setPreferredSize(new Dimension(0, 42));
    table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));

    TableColumnModel cm = table.getColumnModel();
    for (int i = 0; i < count; i++) {
      TableColumn column = cm.getColumn(i);
      if (table.getColumnClass(i) != Boolean.class) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(alignments[i]);
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        column.setCellRenderer(renderer);
      }
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createLineBorder(BORDER));
    scroll.getViewport().setBackground(SURFACE);
    scroll.getViewport().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        resizeColumns();
      }

      private void resizeColumns() {
        int viewportWidth = scroll.getViewport().getWidth();
        if (viewportWidth <= 0)
          return;
        int totalWidth = Math.max(viewportWidth, 680);
        int sum = 0;
        for (int w : weights)
          sum += w;
        int used = 0;
        for (int i = 0; i < count; i++) {
          int width = (i == count - 1)
              ? totalWidth - used
              : (int) Math.round(totalWidth * (double) weights[i] / sum);
          width = Math.max(45, width);
          used += width;
          cm.getColumn(i).setPreferredWidth(width);
          cm.getColumn(i).setWidth(width);
        }
        table.revalidate();
      }
    });
    return scroll;
  }
}
