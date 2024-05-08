package gui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import java.awt.FlowLayout;
import java.awt.Dimension;


public class FindPathModal extends JDialog{
    public FindPathModal(Fenetre parent, String title){
        super(parent,title,false);

        JLabel sourceLabel = new JLabel(" Path from: " + parent.getSelectedServer().getIpAddress());

        JLabel destLabel = new JLabel("To the server:");
        JComboBox<String> destField = new JComboBox<>(parent.getServersIp());

        JButton confirm = new JButton("Confirm");

        JPanel pane = (JPanel) getContentPane();

        pane.setLayout(new javax.swing.BoxLayout(pane,BoxLayout.Y_AXIS));

        pane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel sourceForm = new JPanel(new FlowLayout());
        JPanel destForm = new JPanel(new FlowLayout());

        
        JPanel btnPane = new JPanel();

        sourceForm.add(sourceLabel);

        
        destForm.add(destLabel);
        destForm.add(destField);

        btnPane.add(confirm);

        pane.add(sourceForm);
        pane.add(destForm);
        pane.add(btnPane);
 
        sourceForm.setMaximumSize(new Dimension(220,60));
        destForm.setMaximumSize(new Dimension(220,60));
        btnPane.setMaximumSize(new Dimension(220,60));
        
        setSize(300,300);
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        confirm.addActionListener(e -> {
            parent.showPathToIP(parent.getSelectedServer().getIpAddress(),(String)destField.getSelectedItem());
            parent.deselect();
            setVisible(false);
        });
    }
}
