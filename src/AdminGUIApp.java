import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class AdminGUIApp extends JFrame {
    private final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "MajSQL-0293";

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField emailField;
    private JButton addUserButton;
    private Connection connection;

    public AdminGUIApp() {
        setTitle("Admin");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Name", "Email"}, 0);
        usersTable = new JTable(tableModel);

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        addUserButton = new JButton("Add User");
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String email = emailField.getText();
                if (!name.isEmpty() && !email.isEmpty()) {
                    addUser(name, email);
                    loadUsers();
                    nameField.setText("");
                    emailField.setText("");
                }
            }
        });


        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));
        panel.add(new JLabel(" Name:"));
        panel.add(nameField);
        panel.add(new JLabel(" Email:"));
        panel.add(emailField);
        panel.add(addUserButton);

        add(new JScrollPane(usersTable), BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        // Establish database connection
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        tableModel.setRowCount(0); // Clear existing rows
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, email FROM users")) {
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                tableModel.addRow(new Object[]{name, email});
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addUser(String name, String email) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
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
                new AdminGUIApp().setVisible(true);
            }
        });
    }
}
