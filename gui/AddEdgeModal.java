package gui;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import java.awt.FlowLayout;
import java.awt.Dimension;


public class AddEdgeModal extends JDialog{
    public AddEdgeModal(Fenetre parent, String title){
        super(parent,title,false);

        JLabel sourceLabel = new JLabel("From:");
        JComboBox<String> sourceField = new JComboBox<>(parent.getServersIp());

        JLabel destLabel = new JLabel("To:");
        JComboBox<String> destField = new JComboBox<>(parent.getServersIp());

        JLabel weightFieldLabel = new JLabel("Weight:");
        JTextField weightField = new JTextField(15);
        

        JButton confirm = new JButton("Confirm");

        JPanel pane = (JPanel) getContentPane();

        pane.setLayout(new javax.swing.BoxLayout(pane,BoxLayout.Y_AXIS));

        pane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel sourceForm = new JPanel(new FlowLayout());
        JPanel destForm = new JPanel(new FlowLayout());
        JPanel weightForm = new JPanel(new FlowLayout());
        
        JPanel btnPane = new JPanel();

        sourceForm.add(sourceLabel);
        sourceForm.add(sourceField);
        
        destForm.add(destLabel);
        destForm.add(destField);

        weightForm.add(weightFieldLabel);
        weightForm.add(weightField);

        btnPane.add(confirm);

        pane.add(sourceForm);
        pane.add(destForm);
        pane.add(weightForm);
        pane.add(btnPane);
 
        sourceForm.setMaximumSize(new Dimension(220,60));
        destForm.setMaximumSize(new Dimension(220,60));
        weightForm.setMaximumSize(new Dimension(220,80));
        btnPane.setMaximumSize(new Dimension(220,60));
        
        setSize(300,300);
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        confirm.addActionListener(e -> {
            //System.out.printf("{\n\t'ipAdress' : '%s',\n\t'DomainName' : '%s'\n}\n",ipField.getText(),dnField.getText());
            
           // ((Fenetre) parent).addNode(ipField.getText(), dnField.getText());
            parent.addEdge((String) sourceField.getSelectedItem(), (String) destField.getSelectedItem(), Integer.valueOf(weightField.getText()));
            setVisible(false);
        });
    }
}
