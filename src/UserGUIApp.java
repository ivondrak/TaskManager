import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.TableRowSorter;

public class UserGUIApp extends GenericGUIApp {

    private JComboBox<String> emailComboBox;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JButton toggleStatusButton;

    public UserGUIApp(String title) {
        super(title);
    }

    @Override
    protected void initializeUI() {
        setLayout(new BorderLayout());

        emailComboBox = new JComboBox<>();
        emailComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTasksForSelectedEmail();
            }
        });

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Due Date", "Status"}, 0);
        tasksTable = new JTable(tableModel);
        tasksTable.setRowSorter(new TableRowSorter<>(tableModel));

        toggleStatusButton = new JButton("Toggle Status");
        toggleStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleTaskStatus();
            }
        });

        add(emailComboBox, BorderLayout.NORTH);
        add(new JScrollPane(tasksTable), BorderLayout.CENTER);
        add(toggleStatusButton, BorderLayout.SOUTH);
        loadEmails();     
    }

    private void loadEmails() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email FROM users")) {
            while (rs.next()) {
                emailComboBox.addItem(rs.getString("email"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksForSelectedEmail() {
        String selectedEmail = (String) emailComboBox.getSelectedItem();
        if (selectedEmail == null) return;

        tableModel.setRowCount(0); // Clear existing rows

        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, title, due_date, status FROM tasks WHERE user_email = ?")) {
            stmt.setString(1, selectedEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String dueDate = rs.getDate("due_date").toString();
                    String status = rs.getInt("status") == 0 ? "Pending" : "Completed";
                    tableModel.addRow(new Object[]{
                            id, title, dueDate, status
                    });
                }
                rs.close();
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleTaskStatus() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(UserGUIApp.this, "Select a task to update!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        };

        int modelRow = tasksTable.convertRowIndexToModel(selectedRow);
        int taskId = (int) tableModel.getValueAt(modelRow, 0);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 3);

        //int newStatus = currentStatus == 0 ? 1 : 0;
        int newStatus = currentStatus.equals("Pending") ? 1 : 0;

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
            stmt.setInt(1, newStatus);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            currentStatus = newStatus == 0 ? "Pending" : "Completed";
            tableModel.setValueAt(currentStatus, modelRow, 3); // Update table model
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserGUIApp("Assigments Overview").setVisible(true);
            }
        });
    }
}
