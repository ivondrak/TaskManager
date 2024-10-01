import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.TableRowSorter;

public class AdminGUIApp extends GenericGUIApp {

    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField emailField;
    private JButton addUserButton;
    private JButton removeUserButton;

    public AdminGUIApp(String title) {
        super(title);
    }

    @Override
    protected void initializeUI() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"Name", "Email"}, 0);
        usersTable = new JTable(tableModel);
        usersTable.setRowSorter(new TableRowSorter<>(tableModel));

        usersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = usersTable.convertRowIndexToModel(selectedRow);
                    String name = (String) tableModel.getValueAt(modelRow, 0);
                    String email = (String) tableModel.getValueAt(modelRow, 1);
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
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(AdminGUIApp.this, "Invalid email address!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
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
                    int modelRow = usersTable.convertRowIndexToModel(selectedRow);
                    String email = (String) tableModel.getValueAt(modelRow, 1);
                    removeUser(email);
                    loadUsers();
                }
                else {
                    JOptionPane.showMessageDialog(AdminGUIApp.this, "Select a user to remove!", "Error", JOptionPane.ERROR_MESSAGE);
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
        loadUsers();

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

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AdminGUIApp("User Administration").setVisible(true);
            }
        });
    }
}
