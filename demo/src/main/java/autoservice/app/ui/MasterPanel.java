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
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import autoservice.app.dao.MasterDAO;
import autoservice.app.model.Master;

public class MasterPanel extends JPanel {
	private final boolean admin;
	private final MasterDAO dao = new MasterDAO();
	private final DefaultTableModel model = new DefaultTableModel(new Object[] { "ID", "Имя мастера" }, 0) {
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? Integer.class : String.class;
		}
	};
	private final JTable table = new JTable(model);
	private final JButton refresh = new JButton("Обновить");
	private final JButton add = new JButton("Добавить");
	private final JButton edit = new JButton("Изменить");
	private final JButton delete = new JButton("Удалить");

	public MasterPanel(boolean admin) {
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
				new int[] { 12, 88 }, new int[] { SwingConstants.CENTER, SwingConstants.LEFT }),
				BorderLayout.CENTER);
		refresh.addActionListener(e -> loadMasters());
		add.addActionListener(e -> addMaster());
		edit.addActionListener(e -> editMaster());
		delete.addActionListener(e -> deleteMaster());
		loadMasters();
	}

	private Integer selectedId() {
		int row = table.getSelectedRow();
		if (row < 0) {
			JOptionPane.showMessageDialog(this, "Выберите мастера в таблице");
			return null;
		}
		return (Integer) model.getValueAt(table.convertRowIndexToModel(row), 0);
	}

	private void addMaster() {
		if (!admin)
			return;
		String name = JOptionPane.showInputDialog(this, "Имя мастера:", "Добавление мастера", JOptionPane.PLAIN_MESSAGE);
		if (name == null)
			return;
		name = name.trim();
		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Укажите имя мастера");
			return;
		}
		String finalName = name;
		change(() -> {
			dao.addMaster(new Master(finalName));
			return true;
		}, "Мастер добавлен");
	}

	private void editMaster() {
		if (!admin)
			return;
		Integer id = selectedId();
		if (id == null)
			return;
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		String oldName = (String) model.getValueAt(row, 1);
		String newName = JOptionPane.showInputDialog(this, "Имя мастера:", oldName);
		if (newName == null)
			return;
		newName = newName.trim();
		if (newName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Укажите имя мастера");
			return;
		}
		String finalName = newName;
		change(() -> {
			dao.updateMaster(new Master(id, finalName));
			return true;
		}, "Изменение выполнено");
	}

	private void deleteMaster() {
		if (!admin)
			return;
		Integer id = selectedId();
		if (id == null)
			return;
		int confirm = JOptionPane.showConfirmDialog(this,
				"Удалить мастера ID " + id + "? Связанные работы могут быть затронуты.",
				"Подтвердите удаление", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION)
			change(() -> dao.deleteMaster(id), "Мастер удалён");
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
					JOptionPane.showMessageDialog(MasterPanel.this, changed ? message : "Запись не найдена");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					showError(e.getCause());
				} finally {
					setBusy(false);
				}
				if (changed)
					loadMasters();
			}
		}.execute();
	}

	private void loadMasters() {
		setBusy(true);
		new SwingWorker<List<Master>, Void>() {
			@Override
			protected List<Master> doInBackground() throws Exception {
				return dao.getAllMasters();
			}

			@Override
			protected void done() {
				try {
					List<Master> masters = get();
					model.setRowCount(0);
					for (Master m : masters)
						model.addRow(new Object[] { m.getId(), m.getName() });
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
