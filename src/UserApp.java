import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserApp {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MajSQL-0293";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your email address: ");
        String email = scanner.nextLine();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String selectTasksQuery = "SELECT id, title, status FROM tasks WHERE user_email = ?";
            PreparedStatement selectTasksStmt = connection.prepareStatement(selectTasksQuery);
            selectTasksStmt.setString(1, email);

            ResultSet tasksResultSet = selectTasksStmt.executeQuery();

            System.out.println("Tasks for " + email + ":");
            while (tasksResultSet.next()) {
                int id = tasksResultSet.getInt("id");
                String taskName = tasksResultSet.getString("title");
                String status = tasksResultSet.getString("status");
                System.out.println(id + ": " + taskName + " (Status: " + status + ")");
            }

            System.out.print("Enter the ID of the task you want to update: ");
            int taskId = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            System.out.print("Enter the new status (0|1): ");
            String newStatus = scanner.nextLine();

            String updateTaskQuery = "UPDATE tasks SET status = ? WHERE id = ?";
            PreparedStatement updateTaskStmt = connection.prepareStatement(updateTaskQuery);
            updateTaskStmt.setInt(1, Integer.parseInt(newStatus));
            updateTaskStmt.setInt(2, taskId);

            int rowsUpdated = updateTaskStmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Task status updated successfully.");
            } else {
                System.out.println("Task not found or status not updated.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }
}