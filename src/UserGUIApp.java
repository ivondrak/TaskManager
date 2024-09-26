import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class UserGUIApp extends JFrame {
    //private final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private final String DB_URL = "jdbc:mysql://vsrvfeia0h-64.vsb.cz:3306/user_tasks_db";
    //private final String DB_USER = "root";
    private final String DB_USER = "guest";
    //private final String DB_PASSWORD = "MajSQL-0293";
    private final String DB_PASSWORD = "guest_password";


    private JComboBox<String> emailComboBox;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JButton toggleStatusButton;
    private Connection connection;

    public UserGUIApp() {
        // Initialize the GUI components
        setTitle("Task Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        // Establish database connection
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            loadEmails();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if (selectedRow == -1) return;

        int taskId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);

        //int newStatus = currentStatus == 0 ? 1 : 0;
        int newStatus = currentStatus.equals("Pending") ? 1 : 0;

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
            stmt.setInt(1, newStatus);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            currentStatus = newStatus == 0 ? "Pending" : "Completed";
            tableModel.setValueAt(currentStatus, selectedRow, 3); // Update table model
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserGUIApp().setVisible(true);
            }
        });
    }
}
