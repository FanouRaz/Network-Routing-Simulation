import javax.swing.UIManager;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import gui.Fenetre;

public class Main {
     public static void main(String[] args) throws Exception{
        UIManager.setLookAndFeel(new FlatMacLightLaf());

        
        new Fenetre();
    }
}
