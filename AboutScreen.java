
package cyberthreatdetection;

import javax.swing.*;
import java.awt.*;

public class AboutScreen extends JFrame {

    public AboutScreen() {

        setTitle("About System");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBackground(new Color(15, 23, 42));
        main.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));

        JLabel title = new JLabel(
                "ABOUT THE PROJECT",
                SwingConstants.CENTER);

        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 27));

        JTextArea information = new JTextArea();

        information.setText(
                "EARLY DETECTION OF EMERGING CYBER THREATS\n"
                + "USING TEMPORAL INTELLIGENCE\n\n"
                + "PROJECT OVERVIEW\n\n"
                + "This project analyzes historical cybersecurity "
                + "vulnerability data to identify unusual patterns "
                + "and emerging threat trends.\n\n"
                + "The system uses temporal analysis, anomaly "
                + "detection, forecasting techniques and an "
                + "early warning mechanism to support "
                + "proactive cybersecurity monitoring.\n\n"
                + "CORE MODULES\n\n"
                + "1. Temporal Analysis\n"
                + "2. Emerging Threat Identification\n"
                + "3. Threat Forecasting\n"
                + "4. Early Warning System\n"
                + "5. Threat Management\n"
                + "6. Dataset Management\n\n"
                + "TECHNOLOGIES USED\n\n"
                + "Programming Language: Java\n"
                + "Database: SQLite\n"
                + "Data Source: CISA Known Exploited "
                + "Vulnerabilities Catalog\n"
                + "Interface: Java Swing\n\n"
                + "PURPOSE\n\n"
                + "To identify unusual vulnerability activity "
                + "and provide early indications of potential "
                + "cybersecurity concerns."
        );

        information.setEditable(false);
        information.setLineWrap(true);
        information.setWrapStyleWord(true);
        information.setFont(new Font("Arial", Font.PLAIN, 15));
        information.setForeground(Color.WHITE);
        information.setBackground(new Color(30, 41, 59));
        information.setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(information);

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton back = new JButton("Back to Home");
        back.addActionListener(e -> dispose());

        bottom.add(back);

        main.add(title, BorderLayout.NORTH);
        main.add(scroll, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        add(main);
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            AboutScreen screen = new AboutScreen();
            screen.setVisible(true);
        });
    }
}
