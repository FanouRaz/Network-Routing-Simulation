import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import gui.Fenetre;

public class Main {
     public static void main(String[] args) throws Exception{
        UIManager.setLookAndFeel(new NimbusLookAndFeel());

        new Fenetre();
    }
}
