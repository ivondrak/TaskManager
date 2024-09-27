import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ManagerGUIApp extends JFrame {
    //private final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private final String DB_URL = "jdbc:mysql://vsrvfeia0h-64.vsb.cz:3306/user_tasks_db";
    //private final String DB_USER = "root";
    private final String DB_USER = "guest";
    //private final String DB_PASSWORD = "MajSQL-0293";
    private final String DB_PASSWORD = "guest_password";

    private Connection connection;
    private JComboBox<String> emailComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField titleField, dueDateField;
    private JButton addButton, deleteButton, updateButton;

    public ManagerGUIApp() {
        
        setTitle("Task Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Due Date", "Status", "User Email"}, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String title = (String) tableModel.getValueAt(selectedRow, 1);
                String due_date = (String) tableModel.getValueAt(selectedRow, 2).toString();
                String email = (String) tableModel.getValueAt(selectedRow, 4);
                titleField.setText(title);
                dueDateField.setText(due_date);
                emailComboBox.setSelectedItem(email);
            }
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel(" Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel(" Due Date:"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel(" User Email:"));
        emailComboBox = new JComboBox<>();
        inputPanel.add(emailComboBox);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        addButton = new JButton("Add Task");
        deleteButton = new JButton("Delete Task");
        updateButton = new JButton("Update Task");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTask();
            }
        });

        // Establish database connection
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        loadTasks();
    }

    private void loadTasks() {
        tableModel.setRowCount(0); // Clear existing rows
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT tasks.id, tasks.title, tasks.due_date, tasks.status, users.email FROM tasks JOIN users ON tasks.user_email = users.email")) {

            while (rs.next()) {
                String status = rs.getInt("status") == 0 ? "Pending" : "Completed";
                tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), rs.getDate("due_date"), status, rs.getString("email")});
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTask() {
        String title = titleField.getText();
        String dueDate = dueDateField.getText();
        String email = emailComboBox.getItemAt(emailComboBox.getSelectedIndex());

        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO tasks (title, due_date, status, user_email) VALUES (?, ?, 0, ?)")) {

            pstmt.setString(1, title);
            pstmt.setString(2, dueDate);
            pstmt.setString(3, email);
            pstmt.executeUpdate();

            loadTasks();
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadTasks();
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateTask() {
        
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            String newTitle = titleField.getText();
            Date newDueDate = Date.valueOf(dueDateField.getText());
            String newEmail = emailComboBox.getItemAt(emailComboBox.getSelectedIndex());

            try (PreparedStatement pstmt = connection.prepareStatement("UPDATE tasks SET title = ?, due_date = ?, user_email = ? WHERE id = ?")) {
                pstmt.setString(1, newTitle);
                pstmt.setDate(2, newDueDate);
                pstmt.setString(3, newEmail);
                pstmt.setInt(4, id);
                pstmt.executeUpdate();

                loadTasks();
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                new ManagerGUIApp().setVisible(true);
            }
        });
    }
}
    
