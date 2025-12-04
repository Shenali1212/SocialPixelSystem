import javax.swing.SwingUtilities;

public class Main {
    // The main method, executed when the program starts
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure GUI operations run on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> 
            // Create an instance of LoginWindow and make it visible
            new LoginWindow().setVisible(true)
        );
    }
}
