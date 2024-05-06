package gui;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import algo.Server;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class ServerInfo extends JPanel {
    private JLabel ipAddress, domainName;
    private JCheckBox isUp;
    private JPanel reachable;
    private Server server;

    public ServerInfo(){
        ipAddress = new JLabel("Ip adress: ");
        domainName = new JLabel("Domain name: ");
        isUp = new JCheckBox("Up");
        reachable = new JPanel();

        JScrollPane reachableServer = new JScrollPane (reachable);

        add(ipAddress);
        add(domainName);
        add(isUp);
        add(reachableServer);


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        reachableServer.setBorder (BorderFactory.createTitledBorder("Reachable Servers"));
    }

    public void selected(Server server){
        this.server = server;

        ipAddress.setText(String.format("Ip adress: %s",server.getIpAddress()));
        domainName.setText(String.format("Domain name: %s", server.getDomainName()));
        isUp.setSelected(server.getIsUp());

        reachable.removeAll();

        for(Server neighbor : server.getAllReachableServer())
            reachable.add(new JLabel(neighbor.getIpAddress()));
        
    }
}
