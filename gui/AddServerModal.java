package gui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BoxLayout;

import java.awt.FlowLayout;
import java.awt.Dimension;

public class AddServerModal extends JDialog{
    public AddServerModal(Fenetre parent, String title){
        super(parent,title,false);

        JLabel ipFieldLabel = new JLabel("IP Adresse:");
        JTextField ipField = new JTextField(15);
        JLabel dnFieldLabel = new JLabel("Domain name:");
        JTextField dnField = new JTextField(15);

        JButton confirm = new JButton("Confirm");

        JPanel pane = (JPanel) getContentPane();

        pane.setLayout(new javax.swing.BoxLayout(pane,BoxLayout.Y_AXIS));

        pane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel ipForm = new JPanel(new FlowLayout());
        JPanel dnForm = new JPanel(new FlowLayout());
        JPanel btnPane = new JPanel();
        
        ipForm.add(ipFieldLabel);
        ipForm.add(ipField);
        
        dnForm.add(dnFieldLabel);
        dnForm.add(dnField);

        btnPane.add(confirm);

        pane.add(ipForm);
        pane.add(dnForm);
        
        pane.add(btnPane);
 
        ipForm.setMaximumSize(new Dimension(220,60));
        dnForm.setMaximumSize(new Dimension(220,60));
    
        btnPane.setMaximumSize(new Dimension(220,60));
        
        setSize(300,300);
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        confirm.addActionListener(e -> {
            //System.out.printf("{\n\t'ipAdress' : '%s',\n\t'DomainName' : '%s'\n}\n",ipField.getText(),dnField.getText());
            
            ((Fenetre) parent).addNode(ipField.getText(), dnField.getText());
            setVisible(false);
        });
    }
}