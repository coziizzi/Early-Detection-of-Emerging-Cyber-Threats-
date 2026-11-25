
package cyberthreatdetection;

public class CyberThreatDetection {

    public static void main(String[] args) {

        System.out.println("Initializing Cyber Threat Detection System...");

        Database.createTables();

        CISADataImporter.importData();

        EarlyWarning.generateAlerts();

        System.out.println("Launching Dashboard...");

        HomeScreen.launch();
    }
}

