import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
//import java.util.stream.Collectors;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypingTutor extends JFrame implements KeyListener {
    private JTextPane promptPane;
    private JLabel statsLabel;
    private HashMap<Character, Integer> mistakeCounts;
    private ArrayList<String> exercises;
    private int level;
    private String currentExercise;
    private StringBuilder currentInput;
    private int correctPresses;
    private int totalPresses;
    private long startTime;
    private long endTime;

    public TypingTutor() {

        showLoadingScreen();

        mistakeCounts = new HashMap<>();
    }

    public void showLoadingScreen() {
        JFrame loadingFrame = new JFrame("Loading...");
        loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loadingFrame.setSize(800, 600);
        loadingFrame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Welcome! Place your hands on the keyboard as shown and press any key to continue.", SwingConstants.CENTER);
        label.setFont(new Font("Serif", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add some padding

        // Load the image
        ImageIcon icon = new ImageIcon(getClass().getResource("keyboard_hands.png")); // Adjust path as needed
        JLabel imageLabel = new JLabel(icon);

        // Adding components to the panel
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(label, BorderLayout.SOUTH);

        loadingFrame.add(panel);
        loadingFrame.setVisible(true);

        // Key listener to detect any key press
        loadingFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                loadingFrame.dispose();
                initUI();
                loadExercises();
                level = 0;
                startLevel();
                mistakeCounts = new HashMap<>();
                // Close the loading screen when any key is pressed

            }
        });

        loadingFrame.setFocusable(true); // To ensure the JFrame can capture key presses
    }
    private void initUI() {
        setTitle("Typing Tutor");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        promptPane = new JTextPane();
        promptPane.setContentType("text/html");
        promptPane.setEditable(false);
        promptPane.setFont(new Font("Serif", Font.BOLD, 24));
        JScrollPane scrollPane = new JScrollPane(promptPane);
        add(scrollPane, BorderLayout.CENTER);

        statsLabel = new JLabel("Accuracy: 0%, Words Per Minute (WPM): 0", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Serif", Font.BOLD, 18));
        add(statsLabel, BorderLayout.SOUTH);

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    private void loadExercises() {
        exercises = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream("exercises.txt");
        if (is == null) {
            JOptionPane.showMessageDialog(this, "Resource not found: " , "Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit the method as the resource could not be found
        }

        // Use getResourceAsStream to read from inside the JAR or from file system in development
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder exercise = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    if (line.startsWith("Level")) { // Start of a new level
                        if (!exercise.isEmpty()) {
                            exercises.add(exercise.toString().trim()); // Add the previous exercise to the list
                            exercise = new StringBuilder(); // Reset for the next exercise
                        }
                    } else {
                        exercise.append(line).append("\n"); // Append the line to the current exercise
                    }
                }
            }
            if (!exercise.isEmpty()) { // Don't forget to add the last exercise
                exercises.add(exercise.toString().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load exercises from file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void startLevel() {
        if (level < exercises.size()) {
            currentExercise = exercises.get(level);
            currentInput = new StringBuilder();
            correctPresses = 0;
            totalPresses = 0;
            startTime = System.currentTimeMillis();
            updateTextPane();
            this.requestFocus(); // Request focus for key events.
        } else {
            JOptionPane.showMessageDialog(this, "Congratulations, you have completed all levels!", "Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void updateStats() {
        endTime = System.currentTimeMillis();
        double accuracy = (totalPresses > 0) ? ((double) correctPresses / totalPresses) * 100 : 0;
        double timeTaken = (endTime - startTime) / 60000.0;
        double wpm = (timeTaken > 0) ? (correctPresses / 5.0) / timeTaken : 0;

        statsLabel.setText(String.format("Level:  %d , Accuracy: %.2f%%, Words Per Minute (WPM): %.2f",level + 1, accuracy, wpm));
    }


    private void updateTextPane() {
        StringBuilder htmlText = new StringBuilder("<html><body style='font-size: 24px;'>");
        for (int i = 0; i < currentExercise.length(); i++) {
            char c = currentExercise.charAt(i);
            if (i < currentInput.length()) {
                char inputChar = currentInput.charAt(i);
                if (c == inputChar) {
                    htmlText.append("<span style='color: green;'>").append(c).append("</span>");
                } else {
                    htmlText.append("<span style='color: red;'>").append(inputChar).append("</span>");
                }
            } else {
                htmlText.append("<span style='color: black;'>").append(c).append("</span>");
            }
        }
        htmlText.append("</body></html>");
        promptPane.setText(htmlText.toString());
    }

    private void showProblematicKeys() {
        StringBuilder message = new StringBuilder("Watch out for these keys:\n");
        mistakeCounts.entrySet().stream()
                .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
                .limit(5) // Show top 5 problematic keys
                .forEach(entry -> message.append(entry.getKey()).append(": ").append(entry.getValue()).append(" mistakes\n"));

        JOptionPane.showMessageDialog(this, message.toString(), "Problematic Keys", JOptionPane.INFORMATION_MESSAGE);
    }


    private void checkAndShowAchievements(double accuracy, double wpm) {
        ArrayList<String> achievements = new ArrayList<>();

        if (level == 1) {
            achievements.add("Completed the First Level!");
        }
        if (accuracy >= 90.0) {
            achievements.add("Achieved 90% Accuracy!");
        }
        if (wpm >= 60) {
            achievements.add("Reached 60 WPM!");
        }

        if (!achievements.isEmpty()) {
            StringBuilder message = new StringBuilder("Congratulations! You've unlocked new achievements:\n");
            for (String achievement : achievements) {
                message.append(achievement).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString(), "Achievements", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        // Ignore non-printable characters.
        if (Character.isISOControl(c)) {
            return;
        }

        if (currentInput.length() < currentExercise.length()) {
            currentInput.append(c);

            if (currentExercise.charAt(currentInput.length() - 1) != c) {
                Toolkit.getDefaultToolkit().beep();
                // Update the mistake count for the wrong character pressed.
                mistakeCounts.put(c, mistakeCounts.getOrDefault(c, 0) + 1);
            }

            if (currentExercise.charAt(currentInput.length() - 1) == c) {
                correctPresses++;
            }
            totalPresses++;
            updateTextPane();
            updateStats();
        }
    }
    private void showResults() {
        // Existing results calculation code...
        double accuracy = (totalPresses > 0) ? ((double) correctPresses / totalPresses) * 100 : 0;
        double timeTaken = (endTime - startTime) / 60000.0;
        double wpm = (timeTaken > 0) ? (correctPresses / 5.0) / timeTaken : 0;

        // New code to determine and display problematic keys
        String problematicKeys = getProblematicKeysString();

        int choice = JOptionPane.showOptionDialog(this,
                String.format("Your accuracy: %.2f%%\nWords per minute: %.2f\n\nProblematic keys: %s\n\nWould you like to proceed to the next level or repeat this one?", accuracy, wpm, problematicKeys),
                "Level Completed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Next Level", "Repeat"},
                "Next Level");

        if (choice == JOptionPane.YES_OPTION) {
            level++;
        }
        startLevel();
        requestFocusInWindow();
    }

    private String getProblematicKeysString() {
        if (mistakeCounts.isEmpty()) {
            return "None";
        }

        // Find the maximum value (most mistakes)
        int maxMistakes = Collections.max(mistakeCounts.values());
        StringBuilder keys = new StringBuilder();
        for (Map.Entry<Character, Integer> entry : mistakeCounts.entrySet()) {
            if (entry.getValue() == maxMistakes) { // This key has the most mistakes
                keys.append(entry.getKey()).append(" ");
            }
        }

        return keys.toString().trim(); // Returns the keys with most mistakes
    }


    @Override
    public void keyPressed(KeyEvent e) {
        // Handle control keys such as backspace and enter here.
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && level <= 10) {
            if (!currentInput.isEmpty()) {
                // Remove the last character from currentInput
                currentInput.deleteCharAt(currentInput.length() - 1);
                // Recalculate stats based on the corrected input
                correctPresses = 0; // Reset correct presses for recalculation
                for (int i = 0; i < currentInput.length(); i++) {
                    if (currentExercise.charAt(i) == currentInput.charAt(i)) {
                        correctPresses++;
                    }
                }
                totalPresses = Math.max(currentInput.length(), 0); // Adjust total presses
                updateTextPane();
                updateStats();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentInput.length() >= currentExercise.length()) {
                showResults();
            }
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            TypingTutor tt = new TypingTutor();
            tt.pack(); // Adjusts the frame to fit the preferred sizes of its components.
            tt.setVisible(true);
            tt.requestFocusInWindow(); // Make sure the window has focus to capture key events.
        });
    }
}