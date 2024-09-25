import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ManagerApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MajSQL-0293";

    public static void main(String[] args) {
        List<Task> tasks = loadTasks();
        displayTasks(tasks);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter task title:");
        String title = scanner.nextLine();
        System.out.println("Enter due date (YYYY-MM-DD):");
        String dueDate = scanner.nextLine();
        System.out.println("Enter status:");
        String status = scanner.nextLine();
        System.out.println("Enter user email:");
        String email = scanner.nextLine();
        scanner.close();

        addTask(title, dueDate, status, email);
        tasks = loadTasks();
        displayTasks(tasks);
    }

    private static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM tasks");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String dueDate = resultSet.getString("due_date");
                String status = resultSet.getString("status");
                String userEmail = resultSet.getString("user_email");
                tasks.add(new Task(id, title, dueDate, status, userEmail));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private static void displayTasks(List<Task> tasks) {
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    private static void addTask(String title, String dueDate, String status, String email) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO tasks (title, due_date, status, user_email) VALUES (?, ?, ?, ?)")) {

            statement.setString(1, title);
            statement.setString(2, dueDate);
            statement.setString(3, status);
            statement.setString(4, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class Task {
    private int id;
    private String title;
    private String dueDate;
    private String status;
    private String userEmail;

    public Task(int id, String title, String dueDate, String status, String userEmail) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.status = status;
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", status='" + status + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
