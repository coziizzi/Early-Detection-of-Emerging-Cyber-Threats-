
package cyberthreatdetection;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JFrame {

    public HomeScreen() {

        setTitle("Cyber Threat Intelligence System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        // HEADER
        JPanel header = new JPanel(new GridLayout(3, 1, 5, 5));
        header.setOpaque(false);

        JLabel title = new JLabel(
                "CYBER THREAT INTELLIGENCE SYSTEM",
                SwingConstants.CENTER);

        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 27));

        JLabel subtitle = new JLabel(
                "EARLY DETECTION OF EMERGING CYBER THREATS",
                SwingConstants.CENTER);

        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel instruction = new JLabel(
                "Select a module to get started",
                SwingConstants.CENTER);

        instruction.setForeground(new Color(56, 189, 248));
        instruction.setFont(new Font("Arial", Font.PLAIN, 15));

        header.add(title);
        header.add(subtitle);
        header.add(instruction);

        main.add(header, BorderLayout.NORTH);

        // MODULE BUTTONS
        JPanel modules = new JPanel(new GridLayout(4, 2, 20, 20));
        modules.setOpaque(false);

        addModule(modules, "Dashboard",
                "System overview", () -> openDashboard());

        addModule(modules, "Temporal Analysis",
                "Analyze monthly vulnerability trends",
                () -> openTemporalAnalysis());

        addModule(modules, "Emerging Threats",
                "Identify unusual vulnerability increases",
                () -> openEmergingThreats());

        addModule(modules, "Threat Forecasting",
                "Predict future vulnerability trends",
                () -> openForecasting());

        addModule(modules, "Early Warnings",
                "View generated security alerts",
                () -> openEarlyWarnings());

        addModule(modules, "Threat Management",
                "Manage and update alert statuses",
                () -> openThreatManagement());

        addModule(modules, "Data Management",
                "Import vulnerability information",
                () -> openDataManagement());

        addModule(modules, "About System",
                "Project information",
                () -> openAbout());

        main.add(modules, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "CYBERSECURITY  |  TEMPORAL INTELLIGENCE  |  EARLY WARNING",
                SwingConstants.CENTER);

        footer.setForeground(new Color(100, 116, 139));
        footer.setFont(new Font("Arial", Font.PLAIN, 11));

        main.add(footer, BorderLayout.SOUTH);

        add(main);
    }

    private void addModule(JPanel panel, String name,
            String description, Runnable action) {

        JButton button = new JButton(
                "<html><center><b>" + name
                + "</b><br><br><small>" + description
                + "</small></center></html>");

        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(30, 41, 59));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(
                new Color(51, 65, 85), 1));

        button.addActionListener(e -> action.run());

        panel.add(button);
    }

    // MODULE NAVIGATION

    private void openDashboard() {
        Dashboard.launch();
    }

    private void openTemporalAnalysis() {
        TemporalAnalysisScreen.launch();
    }

    private void openEmergingThreats() {
        EmergingThreatsScreen.launch();
    }

    private void openForecasting() {
        ThreatForecastingScreen.launch();
    }

    private void openEarlyWarnings() {
        EarlyWarningsScreen.launch();
    }

    private void openThreatManagement() {
        ThreatManagementScreen.launch();
    }

    private void openDataManagement() {
        DataManagementScreen.launch();
    }

    private void openAbout() {
        AboutScreen.launch();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            HomeScreen screen = new HomeScreen();
            screen.setVisible(true);
        });
    }
}
