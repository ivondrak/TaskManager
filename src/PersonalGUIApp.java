import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PersonalGUIApp extends JFrame {
    //private final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private final String DB_URL = "jdbc:mysql://vsrvfeia0h-64.vsb.cz:3306/user_tasks_db";
    //private final String DB_USER = "root";
    private final String DB_USER = "guest";
    //private final String DB_PASSWORD = "MajSQL-0293";
    private final String DB_PASSWORD = "guest_password";

    private JTextField emailField;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private Connection connection;

    public PersonalGUIApp() {
        setTitle("Personal Task Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeDatabaseConnection();
        initializeUI();
    }

    private void initializeDatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout());
        emailField = new JTextField(20);
        JButton submitButton = new JButton("Submit");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                if (isEmailRegistered(email)) {
                    loadTasks();
                } else {
                    JOptionPane.showMessageDialog(PersonalGUIApp.this, email+" not registered!", "Error", JOptionPane.ERROR_MESSAGE);
                    emailField.setText("");
                    loadTasks();
                }
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter Email:"));
        inputPanel.add(emailField);
        inputPanel.add(submitButton);

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Due Date", "Status"}, 0);
        tasksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tasksTable);

        JButton toggleStatusButton = new JButton("Toggle Status");
        toggleStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = tasksTable.getSelectedRow();
                if (selectedRow != -1) {
                    toggleTaskStatus();
                    loadTasks();
                }
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(toggleStatusButton, BorderLayout.SOUTH);

        add(panel);
    }

    private boolean isEmailRegistered(String email) {
        boolean isRegistered = false;
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                isRegistered = rs.getInt(1) > 0;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return isRegistered;
    }

    private void loadTasks() {
        String selectedEmail = emailField.getText();
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
                new PersonalGUIApp().setVisible(true);
            }
        });
    }
}
