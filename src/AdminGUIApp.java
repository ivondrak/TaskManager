import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class AdminGUIApp extends JFrame {
    //private final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private final String DB_URL = "jdbc:mysql://vsrvfeia0h-64.vsb.cz:3306/user_tasks_db";
    //private final String DB_USER = "root";
    private final String DB_USER = "guest";
    //private final String DB_PASSWORD = "MajSQL-0293";
    private final String DB_PASSWORD = "guest_password";

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField emailField;
    private JButton addUserButton;
    private JButton removeUserButton;
    private Connection connection;

    public AdminGUIApp() {
        
        setTitle("Admin");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Name", "Email"}, 0);
        usersTable = new JTable(tableModel);

        usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow != -1) {
                String name = (String) tableModel.getValueAt(selectedRow, 0);
                String email = (String) tableModel.getValueAt(selectedRow, 1);
                nameField.setText(name);
                emailField.setText(email);
            }
            }
        });

        nameField = new JTextField(15);
        emailField = new JTextField(15);
        addUserButton = new JButton("Add User");
        removeUserButton = new JButton("Remove User");
        
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
        removeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    String email = (String) tableModel.getValueAt(selectedRow, 1);
                    removeUser(email);
                    loadUsers();
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
        panel.add(removeUserButton);

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
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?)")) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "User already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void removeUser(String email) {
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM users WHERE email = ?")) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "User cannot be deleted because he/she has tasks assigned!", "Error", JOptionPane.ERROR_MESSAGE);
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
