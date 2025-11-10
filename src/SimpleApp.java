import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.table.TableRowSorter;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class SimpleApp extends JFrame {

    private static final String TASKS_FILE = "Tasks.ser";
    
    private JComboBox<String> statusComboBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField titleField, dueDateField, emailField;
    private JButton addButton, deleteButton, updateButton, reloadButton, exitButton;
    private List<SimpleTask> tasks;

    public SimpleApp(String title) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Přidat WindowListener pro zachytávání uzavření okna
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
                System.exit(0);
            }
        });
        
        tasks = new ArrayList<>();
        loadTasks();
        initializeUI();
    }

    protected void initializeUI() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Due Date", "Status", "User Email"}, 0);
        table = new JTable(tableModel);
        table.setRowSorter(new TableRowSorter<>(tableModel));

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    String title = (String) tableModel.getValueAt(modelRow, 1);
                    String due_date = (String) tableModel.getValueAt(modelRow, 2).toString();
                    String email = (String) tableModel.getValueAt(modelRow, 4);
                    titleField.setText(title);
                    dueDateField.setText(due_date);
                    emailField.setText(email);
                    statusComboBox.setSelectedItem(tableModel.getValueAt(modelRow, 3));
                }
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        inputPanel.add(new JLabel(" Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel(" Due Date (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        inputPanel.add(new JLabel(" Status:"));
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("Pending");
        statusComboBox.addItem("Completed");
        inputPanel.add(statusComboBox);

        inputPanel.add(new JLabel(" User Email:"));
        emailField = new JTextField();
        inputPanel.add(emailField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        updateButton = new JButton("Update");
        reloadButton = new JButton("Reload");
        exitButton = new JButton("Exit");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(reloadButton);
        buttonPanel.add(exitButton);
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

        reloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTasks();
                System.exit(0);
            }
        });

        refreshTable();
    }

    private void loadTasks() {
        File file = new File(TASKS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                @SuppressWarnings("unchecked")
                List<SimpleTask> loadedTasks = (List<SimpleTask>) ois.readObject();
                tasks = loadedTasks;
                System.out.println("Loaded " + tasks.size() + " tasks from " + TASKS_FILE);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading tasks: " + e.getMessage());
                tasks = new ArrayList<>();
            }
        } else {
            System.out.println("File " + TASKS_FILE + " not found, creating empty task list.");
            tasks = new ArrayList<>();
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TASKS_FILE))) {
            oos.writeObject(tasks);
            System.out.println("Saved " + tasks.size() + " tasks to " + TASKS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        // Vymaž výběr v tabulce
        table.clearSelection();
        
        // Vymaž obsah všech polí
        titleField.setText("");
        dueDateField.setText("");
        emailField.setText("");
        statusComboBox.setSelectedItem("Pending"); // Nastav výchozí hodnotu

        // Vyčisti a znovu naplň tabulku
        tableModel.setRowCount(0);
        
        for (SimpleTask task : tasks) {
            tableModel.addRow(new Object[]{
                task.getId(), 
                task.getTitle(), 
                task.getDueDate(), 
                task.getStatus(), 
                task.getUserEmail()
            });
        }
    }

    private void addTask() {
        String title = titleField.getText();
        String dueDate = dueDateField.getText();
        String status = (String) statusComboBox.getSelectedItem();
        String email = emailField.getText();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Missing task title!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Missing user email!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidDate(dueDate)) {
            JOptionPane.showMessageDialog(this, "Date must be in the format YYYY-MM-DD!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Najdi nejvyšší ID a přidej 1
        int newId = 1;
        for (SimpleTask task : tasks) {
            if (task.getId() >= newId) {
                newId = task.getId() + 1;
            }
        }

        SimpleTask newTask = new SimpleTask(newId, title, dueDate, status, email);
        tasks.add(newTask);
        
        refreshTable();
        
        // Vyčistit pole
        titleField.setText("");
        dueDateField.setText("");
        emailField.setText("");
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int id = (int) tableModel.getValueAt(modelRow, 0);
            
            // Najdi a odstraň task s daným ID
            tasks.removeIf(task -> task.getId() == id);
            
            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Select a task to remove!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int id = (int) tableModel.getValueAt(modelRow, 0);

            String newTitle = titleField.getText();
            String newDueDate = dueDateField.getText();
            String newStatus = (String) statusComboBox.getSelectedItem();
            String newEmail = emailField.getText();

            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Missing task title!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newEmail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Missing user email!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidDate(newDueDate)) {
                JOptionPane.showMessageDialog(this, "Date must be in the format YYYY-MM-DD!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Najdi a aktualizuj task s daným ID
            for (SimpleTask task : tasks) {
                if (task.getId() == id) {
                    task.setTitle(newTitle);
                    task.setDueDate(newDueDate);
                    task.setStatus(newStatus);
                    task.setUserEmail(newEmail);
                    break;
                }
            }

            refreshTable();
        } else {
            JOptionPane.showMessageDialog(this, "Select a task to update!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidDate(String dateStr) {
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleApp("Simple Task Manager").setVisible(true);
            }
        });
    }
}

// Serializovatelná třída SimpleTask
class SimpleTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String title;
    private String dueDate;
    private String status;
    private String userEmail;

    public SimpleTask(int id, String title, String dueDate, String status, String userEmail) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.status = status;
        this.userEmail = userEmail;
    }

    // Gettery
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public String getUserEmail() { return userEmail; }

    // Settery
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setStatus(String status) { this.status = status; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

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