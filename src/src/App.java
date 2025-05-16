import javax.swing.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.Timer;
import javax.swing.SpinnerDateModel;
import java.awt.*;

public class App {
    static int streak = 0;
    static int exp = 0;
    static int level = 1;
    static int levelThreshold = 100;
    static int streakFreezes = 0;
    static String currentUsername = "";

    static JLabel streakLabel = new JLabel("Streak: 0");
    static JLabel expLabel = new JLabel("EXP: 0 | Level: 1");
    static JLabel quoteLabel = new JLabel("Quote will appear here...");

    static JPanel taskListPanel = new JPanel();
    static java.util.List<task> tasks = new ArrayList<>();

    static String[] quotes = {
            "The only way to do great work is to love what you do. - Steve Jobs",
            "Be the change that you wish to see in the world. - Mahatma Gandhi",
            "Believe you can and you're halfway there. - Theodore Roosevelt",
            "Success is not final, failure is not fatal: It is the courage to continue that counts. - Winston Churchill",
            "You are braver than you believe, stronger than you seem, and smarter than you think. - A. A. Milne",
            "The journey of a thousand miles begins with one step. - Lao Tzu",
            "We are what we repeatedly do. Excellence, then, is not an act, but a habit. - Aristotle",
            "The only limit to our realization of tomorrow is our doubts of today. - Franklin D. Roosevelt"
    };

    public static void main(String[] args) {
        login();
        loadTasks();

        JFrame frame = new JFrame("Motivo");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JTextField taskField = new JTextField();
        taskField.setBounds(20, 20, 300, 30);
        panel.add(taskField);

        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
        dateSpinner.setBounds(330, 20, 100, 30);
        panel.add(dateSpinner);

        JButton addButton = new JButton("Add Task");
        addButton.setBounds(440, 20, 100, 30);
        panel.add(addButton);

        streakLabel.setBounds(20, 60, 200, 30);
        expLabel.setBounds(20, 90, 300, 30);
        quoteLabel.setBounds(20, 120, 750, 30);
        panel.add(streakLabel);
        panel.add(expLabel);
        panel.add(quoteLabel);

        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBounds(20, 160, 750, 380);
        panel.add(scrollPane);

        Timer quoteTimer = new Timer(30000, e -> {
            quoteLabel.setText(quotes[new Random().nextInt(quotes.length)]);
        });
        quoteTimer.start();

        addButton.addActionListener(e -> {
            String taskName = taskField.getText();
            Date selectedDate = (Date) dateSpinner.getValue();
            String dateStr = new SimpleDateFormat("MM/dd/yyyy").format(selectedDate);
            if (!taskName.isEmpty()) {
                task task = new task(taskName, dateStr);
                addTask(task);
                saveTasks();
                taskField.setText("");
            }
        });

        frame.add(panel);
        frame.setVisible(true);

        // Update UI labels after loading user data
        streakLabel.setText("Streak: " + streak + (streakFreezes > 0 ? " (" + streakFreezes + " Freeze(s))" : ""));
        expLabel.setText("EXP: " + exp + " | Level: " + level);
    }

    static void addTask(task task) {
        tasks.add(task);

        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0)); // tighter spacing
        JLabel label = new JLabel(task.getDescription() + " (Due: " + task.getDueDate() + ")");
        JButton completeButton = new JButton("Complete");

        completeButton.addActionListener(e -> {
            task.markComplete();
            tasks.remove(task);
            taskListPanel.remove(row);
            checkDate(task);
            taskListPanel.revalidate();
            taskListPanel.repaint();
            saveTasks();
        });

        row.add(label);
        row.add(Box.createHorizontalStrut(10));
        row.add(completeButton);
        taskListPanel.add(row);
        taskListPanel.revalidate();
    }

    static void checkDate(task task) {
        try {
            Date dueDate = new SimpleDateFormat("MM/dd/yyyy").parse(task.getDueDate());
            if (new Date().after(dueDate)) {
                if (streakFreezes > 0) {
                    streakFreezes--;
                } else {
                    streak = 0;
                }
            } else {
                streak++;
                exp += 50;
                if (exp >= levelThreshold) {
                    level++;
                    exp = 0;
                    levelThreshold += 100;
                    if (level % 10 == 0) {
                        streakFreezes++;
                    }
                }
            }
            streakLabel.setText("Streak: " + streak + (streakFreezes > 0 ? " (" + streakFreezes + " Freeze(s))" : ""));
            expLabel.setText("EXP: " + exp + " | Level: " + level);
            saveUserData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static void login() {
        String username = JOptionPane.showInputDialog("Enter username:");
        String password = JOptionPane.showInputDialog("Enter password:");
        currentUsername = username;
        String hashedPass = hash(password);

        File file = new File("users.txt");
        boolean found = false;

        try {
            if (!file.exists()) file.createNewFile();
            ArrayList<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(hashedPass)) {
                    exp = Integer.parseInt(parts[2]);
                    level = Integer.parseInt(parts.length > 3 ? parts[3] : String.valueOf(1));
                    levelThreshold = 100 + (level - 1) * 100;
                    found = true;
                    break;
                }
            }
            if (!found) {
                PrintWriter out = new PrintWriter(new FileWriter(file, true));
                out.println(username + "," + hashedPass + ",0,1");
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveUserData() {
        try {
            File file = new File("users.txt");
            ArrayList<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts[0].equals(currentUsername)) {
                    out.println(currentUsername + "," + parts[1] + "," + exp + "," + level);
                } else {
                    out.println(line);
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveTasks() {
        try {
            FileOutputStream fos = new FileOutputStream(currentUsername + "_tasks.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(tasks);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    static void loadTasks() {
        File file = new File(currentUsername + "_tasks.dat");
        if (!file.exists()) return;
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            tasks = (ArrayList<task>) ois.readObject();
            ois.close();
            for (task task : tasks) {
                addTask(task);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
