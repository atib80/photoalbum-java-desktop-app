package net.fotoalbum;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import javax.swing.*;

class Main {

    // private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final String OS_NAME_LC = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) {

        Main.setAppropriateLookAndFeelSettings();

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new LaunchWindow();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                System.err.printf("Exception occurred in Main.class with message: %s\n", ex.getMessage());
            }


        });

    }

    private static void setAppropriateLookAndFeelSettings() {

        String lafInfo = "javax.swing.plaf.metal.MetalLookAndFeel";
        try {
            if (Main.isOSWindows()) {
                lafInfo = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
            } else {
                lafInfo = UIManager.getSystemLookAndFeelClassName();
            }

            UIManager.setLookAndFeel(lafInfo);

        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ex1) {
            final String errorMessage1 = String.format(
                    "Exception: %s%nThe specified LookAndFeel Swing UIManager class [%s] is not supported for %s",
                    ex1.getMessage(), lafInfo, System.getProperty("os.name"));
            System.err.println(errorMessage1);
            // Main.LOG.error(errorMessage1);
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException ex2) {
                final String errorMessage2 = String.format(
                        "Exception: %s%nThe specified LookAndFeel Swing UIManager class [%s] is not supported for %s%n",
                        ex2.getMessage(), lafInfo, System.getProperty("os.name"));
                System.err.println(errorMessage2);
                // Main.LOG.error(errorMessage2);
            }
        }
    }

    static boolean isOSWindows() {

        return Main.OS_NAME_LC.contains("win");
    }
}
