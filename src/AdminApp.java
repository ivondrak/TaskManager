import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class User {
    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "User{name='" + name + "', email='" + email + "'}";
    }
}

public class AdminApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_tasks_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MajSQL-0293";

    public static void main(String[] args) throws Exception {
        List<User> users = loadUsers();
        users.forEach(System.out::println);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter new user name:");
        String name = scanner.nextLine();
        System.out.println("Enter new user email:");
        String email = scanner.nextLine();
        scanner.close();

        addUser(new User(name, email));
        System.out.println("User added successfully!");

        users = loadUsers();
        users.forEach(System.out::println);
    }

    private static List<User> loadUsers() throws Exception {
        List<User> users = new ArrayList<>();
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT name, email FROM users");

        while (resultSet.next()) {
            String name = resultSet.getString("name");
            String email = resultSet.getString("email");
            users.add(new User(name, email));
        }

        resultSet.close();
        statement.close();
        connection.close();

        return users;
    }

    private static void addUser(User user) throws Exception {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        String query = "INSERT INTO users (name, email) VALUES (?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getEmail());
        preparedStatement.executeUpdate();

        preparedStatement.close();
        connection.close();
    }
}