import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public abstract class GenericGUIApp extends JFrame {
    //protected final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    protected final String DB_URL = "jdbc:mysql://vsrvfeia0h-64.vsb.cz:3306/user_tasks_db";
    //protected final String DB_USER = "root";
    protected final String DB_USER = "guest";
    //protected final String DB_PASSWORD = "MajSQL-0293";
    protected final String DB_PASSWORD = "guest_password";

    protected Connection connection; 

    public GenericGUIApp(String title) {
        setTitle(title);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeDatabaseConnection();
        initializeUI();
    }

    private void initializeDatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection "+ DB_URL +" failed", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    protected abstract void initializeUI();

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
}
