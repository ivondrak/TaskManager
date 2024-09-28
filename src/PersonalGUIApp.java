import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PersonalGUIApp extends GenericGUIApp {

    private JTextField emailField;
    private JTable tasksTable;
    private DefaultTableModel tableModel;

    public PersonalGUIApp(String title) {
        super(title);
    }

    @Override
    protected void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout());
        emailField = new JTextField(20);
        JButton submitButton = new JButton("Submit");

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(PersonalGUIApp.this, "Invalid email address!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
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
                else {
                    JOptionPane.showMessageDialog(PersonalGUIApp.this, "Select a task to update!", "Error", JOptionPane.ERROR_MESSAGE);
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
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(PersonalGUIApp.this, "Select a task to update!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        };

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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PersonalGUIApp("Personal Task Manager").setVisible(true);
            }
        });
    }
}
