package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

import algo.Response;


public class NavigateModal extends JDialog {
    public NavigateModal(Fenetre parent, String title, String doc){
        super(parent,title,false);

        JToolBar searchBar = new JToolBar();

        JTextField searchForm = new JTextField("http://");
        JButton searchBtn = new JButton(new ImageIcon("assets/find.png"));

        JTextPane page = new JTextPane();

        String spinnerPath = getClass().getClassLoader()
                                       .getResource("assets/spinner.gif")
                                       .toString();
    
        searchForm.setPreferredSize(new Dimension(200,30));
        searchBar.add(searchForm);  
        searchBar.add(searchBtn);

        page.setContentType("text/html");
        page.setText(String.format("<html>Welcome to %s navigator</html>",parent.getSelectedServer().getIpAddress()));
        
        page.setEditable(false);


        searchBtn.addActionListener(evt -> {
            String domainName = searchForm.getText().startsWith("http://") ? searchForm.getText().substring(7) : searchForm.getText();
             
            ArrayList<String> pathToSite = parent.navigatorSearch(parent.getSelectedServer().getIpAddress(),domainName);
            
            page.setText(String.format("<html><div style='margin-left:50px; margin-top: 20px'><img src='%s'></div> </html>",spinnerPath)); 
            
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                String htmlDoc = 
                    "<!DOCTYPE html>"
                    +"<html>"
                    +  "<head>"
                    +     "<title>Not Found</title>"
                    +  "</head>"
                    +  "<body>"
                    +     "<p style='margin-left:15px;'><strong>404 NOT FOUND</strong></p>"
                    +  "</body>"
                    +"</html>" ;  

                if(pathToSite != null){                    
                    pathToSite.removeFirst();


                    parent.getSelectedServer()
                          .sendRequest(parent.getGraphServer()
                                             .findServerByIpAdress(pathToSite.get(0))
                                             .orElse(null), pathToSite);
                    try{
                        ObjectInputStream input = new ObjectInputStream(parent.getSelectedServer().getInputStream());   
                        Response res =(Response) input.readObject();

                        System.out.printf("[%s] Response Received: %s\n",res.getIpDest(), res);

                        htmlDoc = res.getResponseBody();
                    }catch(IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    }
                }
                
                page.setText(htmlDoc);
            }).start();
        });
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(searchBar, BorderLayout.NORTH);
        getContentPane().add(page, BorderLayout.CENTER);

        setSize(350,350);
        setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
